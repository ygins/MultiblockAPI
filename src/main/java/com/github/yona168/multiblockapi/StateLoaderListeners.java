package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.storage.StateCache;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.storage.DataTunnelRegistry;
import com.github.yona168.multiblockapi.storage.StateDataTunnel;
import com.github.yona168.multiblockapi.storage.StateDataTunnels;
import com.gitlab.avelyn.architecture.base.Component;
import com.gitlab.avelyn.architecture.base.Toggleable;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.github.yona168.multiblockapi.util.ToggleableTasks.syncLater;
import static com.gitlab.avelyn.core.base.Events.listen;
import static java.lang.System.currentTimeMillis;
import static org.bukkit.Bukkit.*;

public class StateLoaderListeners extends Component {
  private final BiConsumer<CommandSender, String> debug;
  private final StateCache stateCache;
  private final DataTunnelRegistry dataTunnelRegistry;
  private final MultiblockRegistry multiblockRegistry;
  private final Plugin plugin;

  public StateLoaderListeners(StateCache stateCache, MultiblockRegistry multiblockRegistry, DataTunnelRegistry storageMethodRegistry, Plugin plugin, BiConsumer<CommandSender, String> debug) {
    this.plugin = plugin;
    this.debug = debug;
    this.stateCache = stateCache;
    this.dataTunnelRegistry = storageMethodRegistry;
    this.multiblockRegistry = multiblockRegistry;
    addChild(multiblockRegistry);
    addChild(StateDataTunnels.enabler(plugin, multiblockRegistry));
    addChild(listen(PlayerInteractEvent.class, this::handleInteract));
    addChild(listen(BlockBreakEvent.class, EventPriority.MONITOR, this::handleBlockBreak));
    addChild(listen(EntityExplodeEvent.class, EventPriority.MONITOR, this::handleEntityExplode));
    addChild(listen(ChunkUnloadEvent.class, this::handleChunkUnload));
    addChild(listen(WorldUnloadEvent.class, this::handleWorldUnload));
    addChild(listen(ChunkLoadEvent.class, this::handleChunkLoad));
    onEnable(()->dataTunnelRegistry.register(new NamespacedKey(plugin,"KRYO"), StateDataTunnels.kryo()));
    onEnable(() ->
            getScheduler().runTask(plugin, ()->Bukkit.getWorlds().stream().map(World::getLoadedChunks)
                    .flatMap(Arrays::stream).forEach(chunk -> proccessLoadingChunk(chunk, false)))
    );
    onDisable(() -> Bukkit.getWorlds().forEach(world -> processUnloadingWorld(world, false)));
    onDisable(() -> {
      storageMethodRegistry.waitForAllAsyncsDone();
      stateCache.clear();
    });
  }

  public StateLoaderListeners(StateCache stateCache, MultiblockRegistry registry, DataTunnelRegistry storageMethodRegistry, Plugin plugin) {
    this(stateCache, registry, storageMethodRegistry, plugin, null);
  }


  private void handleInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    final Location clickedLoc = event.getClickedBlock().getLocation();
    if (dataTunnelRegistry.isProcessing(clickedLoc.getChunk())) {
      return;
    }
    MultiblockState existingState = stateCache.getAt(clickedLoc);
    long before = currentTimeMillis();
    if (existingState == null) {
      multiblockRegistry.getAllMultiblocks().stream().map(multiblock -> multiblock.generateStateFrom(event)).filter(Optional::isPresent).map(Optional::get).findFirst().ifPresent(multiblockState -> {
        boolean wouldOverwriteExistingState=multiblockState.getAllBlocksLocs().stream().anyMatch(loc->stateCache.getAt(loc)!=null);
        if(wouldOverwriteExistingState){
          return;
        }
        stateCache.store(multiblockState);
        (multiblockState).enable();
        multiblockState.getMultiblock().doClickActions(event, multiblockState);
        multiblockState.getMultiblock().getDataTunnel().storeAwayAsync(multiblockState);
        if (debug != null) {
          long difference = currentTimeMillis() - before;
          debug.accept(event.getPlayer(), "Multiblock has been registered, and the whole process took " + ChatColor.LIGHT_PURPLE + difference + ChatColor.RESET + " millis");
        }
      });
    } else {
      existingState.getMultiblock().doClickActions(event, existingState);
      long difference = currentTimeMillis() - before;
      debug.accept(event.getPlayer(), "Multiblock was detected as registered, and the whole process took " + ChatColor.LIGHT_PURPLE + difference + ChatColor.RESET + " millis");
    }
  }

  private void handleBlockBreak(BlockBreakEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (dataTunnelRegistry.isProcessing(event.getBlock().getLocation().getChunk())) {
      event.getPlayer().sendMessage("Multiblocks are being updated for this chunk, please wait...");
      event.setCancelled(true);
    } else {
      processBrokenBlock(event.getBlock());
    }
  }

  private void handleEntityExplode(EntityExplodeEvent event) {
    if (event.isCancelled()) {
    } else {
      event.blockList().forEach(this::processBrokenBlock);
    }
  }

  private void processBrokenBlock(Block block) {
    long removeTime = 0;
    if (debug != null) {
      removeTime = currentTimeMillis();
    }
    final Location broken = block.getLocation();
    final MultiblockState brokenState = stateCache.getAt(broken);
    if (brokenState != null) {
      brokenState.disable();
      brokenState.destroy();
      stateCache.remove(brokenState);
      brokenState.getMultiblock().getDataTunnel().removeFromAfarAsync(brokenState);
      if (debug != null) {
        debug.accept(getConsoleSender(), removeTimeMsg(removeTime));
      }
    }
  }

  private void handleChunkUnload(ChunkUnloadEvent event) {
    final Chunk chunk = event.getChunk();
    if (isTestChunk(chunk)) {
      broadcastMessage("test chunk unloaading.");
    }
    long removeTimeU = 0;
    if (debug != null) {
      removeTimeU = currentTimeMillis();
    }
    final long removeTime = removeTimeU;
    final Collection<MultiblockState> statesInChunk = new HashSet<>(stateCache.getAt(chunk));
    if (!statesInChunk.isEmpty()) {
      Toggleable unloadTask = syncLater(plugin, 100L, () -> {
        if (chunk.isLoaded()) {
          return;
        }
        int size = statesInChunk.size();
        statesInChunk.forEach(state -> {
          state.disable();
          stateCache.remove(state);
          state.getMultiblock().getDataTunnel().storeAwayAsync(state);
        });
        if (debug != null) {
          debug.accept(getConsoleSender(), size + "states were removed from chunk " + ChatColor.RED + chunk.getX() + ", " + chunk.getZ() + " in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms.");
        }
      });
      unloadTask.enable();
    }
  }

  private void handleChunkLoad(ChunkLoadEvent event) {
    proccessLoadingChunk(event.getChunk(), true);
  }

  private void proccessLoadingChunk(Chunk chunk, boolean async) {
    if (isTestChunk(chunk)) {
      //printStackTrace();
      broadcastMessage("Test chunk loading");
    }
    Collection<MultiblockState> alreadyLoadedStates = stateCache.getAt(chunk);
    if (alreadyLoadedStates == null || (alreadyLoadedStates.size() != 0) || dataTunnelRegistry.isProcessing(chunk)) {
      return;
    }
    long removeTime = currentTimeMillis();
    if (async) {
      dataTunnelRegistry.getAllStorageMethods().forEach(dataTunnel -> {
        dataTunnel.getFromAfarAsync(chunk).thenAccept(multiblockStates -> {
          int size = multiblockStates.size();
          multiblockStates.forEach(state -> {
            sync(()->stateCache.store(state));
            sync(state::enable);
          });
          if (debug != null && multiblockStates.size() != 0) {
            debug.accept(getConsoleSender(), size + " states were pulled from afar into chunk " + ChatColor.RED + chunk.getX() + ", " + chunk.getZ() + " in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms.");
          }
        });
      });
    } else {
      List<MultiblockState> multiblockStates = dataTunnelRegistry.getAllStorageMethods()
              .stream().map(dataTunnel -> dataTunnel.getFromAfar(chunk))
              .flatMap(Collection::stream).collect(Collectors.toList());
      int size = multiblockStates.size();
      multiblockStates.forEach(state -> {
        stateCache.store(state);
        state.enable();
      });
      if (debug != null && multiblockStates.size() != 0) {
        debug.accept(getConsoleSender(), size + " states were pulled from afar into chunk " + ChatColor.RED + chunk.getX() + ", " + chunk.getZ() + " in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms.");
      }
    }
  }

  private void handleWorldUnload(WorldUnloadEvent event) {
    processUnloadingWorld(event.getWorld(), true);
  }

  private void processUnloadingWorld(World world, boolean async) {
    long removeTime = 0;
    if (debug != null) {
      removeTime = currentTimeMillis();
    }
    final Collection<MultiblockState> unloadedStates = new HashSet<>(stateCache.getAt(world));
    if (unloadedStates.isEmpty()) {
      return;
    }
    int size = unloadedStates.size();
    unloadedStates.forEach(state -> {
      stateCache.remove(state);
      state.disable();
      final StateDataTunnel dataTunnel=state.getMultiblock().getDataTunnel();
      if (async) dataTunnel.storeAwayAsync(state);
      else dataTunnel.storeAway(state);
    });
    if (debug != null) {
      debug.accept(getConsoleSender(), size + " states were removed from world " + ChatColor.RED + world + " in " + (currentTimeMillis() - removeTime) + " ms.");
    }
  }

  private static String removeTimeMsg(long removeTime) {
    return "Multiblock removed in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms";
  }

  private boolean isTestChunk(Chunk chunk) {
    return chunk.getX() == MultiblockAPI.garbageChunkX && chunk.getZ() == MultiblockAPI.garbageChunkZ;
  }

  private void sync(Runnable runnable) {
    getScheduler().runTask(this.plugin, runnable);
  }
}
