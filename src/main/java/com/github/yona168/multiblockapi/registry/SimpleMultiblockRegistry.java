package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.MultiblockAPI;
import com.github.yona168.multiblockapi.registry.storage.StateStorer;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.gitlab.avelyn.architecture.base.Toggleable;
import com.gitlab.avelyn.core.base.Events;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.gitlab.avelyn.core.base.Events.listen;
import static java.lang.System.currentTimeMillis;
import static org.bukkit.Bukkit.*;

public class SimpleMultiblockRegistry extends AbstractMultiblockRegistry {
  private final BiConsumer<CommandSender, String> debug;
  private final StateStorer stateStorer;

  public SimpleMultiblockRegistry(StateStorer stateStorer, BiConsumer<CommandSender, String> debug) {
    super(stateStorer);
    this.debug = debug;
    this.stateStorer = stateStorer;
    addChild(listen(PlayerInteractEvent.class, this::handleInteract));
    addChild(listen(BlockBreakEvent.class, this::handleBlockBreak));
    addChild(listen(ChunkUnloadEvent.class, this::handleChunkUnload));
    addChild(listen(WorldUnloadEvent.class, this::handleWorldUnload));
    addChild(listen(ChunkLoadEvent.class, this::handleChunkLoad));
    onEnable(() ->
            Bukkit.getWorlds().stream().map(World::getLoadedChunks)
                    .flatMap(Arrays::stream).forEach(chunk->proccessLoadingChunk(chunk, false))
    );

    onDisable(()->Bukkit.getWorlds().forEach(world->processUnloadingWorld(world, false)));
  }

  public SimpleMultiblockRegistry(StateStorer stateStorer) {
    this(stateStorer, null);
  }


  private void handleInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    final Location clickedLoc = event.getClickedBlock().getLocation();
    if (stateStorer.isGettingFromAfar(clickedLoc.getChunk())) {
      return;
    }
    MultiblockState existingState = stateStorer.getHere(clickedLoc);
    long before = currentTimeMillis();
    if (existingState == null) {
      getAllMultiblocks().stream().map(multiblock -> multiblock.generateStateFrom(event)).filter(Optional::isPresent).map(Optional::get).findFirst().ifPresent(multiblockState -> {
        stateStorer.storeHere(multiblockState);
        multiblockState.getMultiblock().doClickActions(event, multiblockState);
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
    if (stateStorer.isGettingFromAfar(event.getBlock().getLocation().getChunk())) {
      event.getPlayer().sendMessage("Multiblocks are being updated for this chunk, please wait...");
      event.setCancelled(true);
      return;
    }
    long removeTime = 0;
    if (debug != null) {
      removeTime = currentTimeMillis();
    }
    final Location broken = event.getBlock().getLocation();
    final MultiblockState brokenState = stateStorer.getHere(broken);
    if (brokenState != null) {
      stateStorer.removeFromHere(brokenState);
      stateStorer.removeFromAfar(brokenState);
      if (debug != null) {
        debug.accept(event.getPlayer(), removeTimeMsg(removeTime));
      }
    }
  }

  private void handleChunkUnload(ChunkUnloadEvent event) {
    final Chunk chunk = event.getChunk();
    if (isTestChunk(chunk)) {
      broadcastMessage("test chunk unloaading.");
    }
    long removeTime = 0;
    if (debug != null) {
      removeTime = currentTimeMillis();
    }
    final Collection<MultiblockState> statesInChunk = stateStorer.getHere(chunk);
    if (statesInChunk != null && !statesInChunk.isEmpty()) {
      statesInChunk.forEach(state -> {
        stateStorer.removeFromHere(state);
        stateStorer.storeAwayAsync(state);
      });
      if (debug != null) {
        debug.accept(getConsoleSender(), statesInChunk.size() + "states were removed from chunk " + ChatColor.RED + chunk.getX() + ", " + chunk.getZ() + " in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms.");
      }
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
    if (stateStorer.isGettingFromAfar(chunk)) {
      return;
    }
    long removeTime = currentTimeMillis();
    stateStorer.getFromAfarAsync(chunk).thenAccept(statesToBeAdded -> {
      if (statesToBeAdded == null || statesToBeAdded.isEmpty()) {
        return;
      }
      statesToBeAdded.forEach(state -> {
        stateStorer.storeHere(state);
        if (async) stateStorer.removeFromAfarAsync(state);
        else stateStorer.removeFromAfar(state);
      });
      if (debug != null) {
        debug.accept(getConsoleSender(), statesToBeAdded.size() + " states were pulled from afar into chunk " + ChatColor.RED + chunk.getX() + ", " + chunk.getZ() + " in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms.");
      }
    });
  }

  private void handleWorldUnload(WorldUnloadEvent event) {
    processUnloadingWorld(event.getWorld(), true);
  }

  private void processUnloadingWorld(World world, boolean async) {
    long removeTime = 0;
    if (debug != null) {
      removeTime = currentTimeMillis();
    }
    final Collection<MultiblockState> unloadedStates = stateStorer.getHere(world);
    if (unloadedStates == null) {
      return;
    }
    unloadedStates.forEach(state -> {
      stateStorer.removeFromHere(state);
      if (async) stateStorer.storeAwayAsync(state);
      else stateStorer.storeAway(state);
    });
    if (debug != null) {
      debug.accept(getConsoleSender(), unloadedStates.size() + " states were removed from world " + ChatColor.RED + world + " in " + (currentTimeMillis() - removeTime) + " ms.");
    }
  }

  private static String removeTimeMsg(long removeTime) {
    return "Multiblock removed in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms";
  }

  private boolean isTestChunk(Chunk chunk) {
    return chunk.getX() == MultiblockAPI.garbageChunkX && chunk.getZ() == MultiblockAPI.garbageChunkZ;
  }

}
