package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.MultiblockAPI;
import com.github.yona168.multiblockapi.registry.storage.StateStorer;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.github.yona168.multiblockapi.util.Stacktrace.printStackTrace;
import static java.lang.System.currentTimeMillis;
import static org.bukkit.Bukkit.*;

public class SimpleMultiblockRegistry extends AbstractMultiblockRegistry {
  private final Plugin plugin;
  private final BiConsumer<CommandSender, String> debug;
  private final StateStorer stateStorer;

  public SimpleMultiblockRegistry(Plugin plugin, StateStorer stateStorer, BiConsumer<CommandSender, String> debug) {
    super(stateStorer);
    this.plugin = plugin;
    this.debug = debug;
    this.stateStorer = stateStorer;
    listen(PlayerInteractEvent.class, this::handleInteract);
    listen(BlockBreakEvent.class, this::handleBlockBreak);
    listen(ChunkUnloadEvent.class, this::handleChunkUnload);
    listen(WorldUnloadEvent.class, this::handleWorldUnload);
    listen(ChunkLoadEvent.class, this::handleChunkLoad);
  }

  public SimpleMultiblockRegistry(Plugin plugin, StateStorer stateStorer) {
    this(plugin, stateStorer, null);
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
        stateStorer.storeHere(multiblockState);
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
      stateStorer.removeFromEverywhere(brokenState);
      if (debug != null) {
        debug.accept(event.getPlayer(), removeTimeMsg(removeTime));
      }
    }
  }

  private void handleChunkUnload(ChunkUnloadEvent event) {
    final Chunk chunk = event.getChunk();
    if(isTestChunk(chunk)){
      broadcastMessage("test chunk unloaading.");
    }
    stateStorer.waitUntilDone(chunk);
    long removeTime = 0;
    if (debug != null) {
      removeTime = currentTimeMillis();
    }
    final Collection<MultiblockState> statesInChunk = stateStorer.getHere(chunk);
    if (statesInChunk != null && !statesInChunk.isEmpty()) {
      statesInChunk.forEach(state -> {
        stateStorer.removeFromHere(state);
        stateStorer.storeAway(state);
      });
      if (debug != null) {
        debug.accept(getConsoleSender(), statesInChunk.size() + "states were removed from chunk " + ChatColor.RED + chunk.getX() + ", " + chunk.getZ() + " in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms.");
      }
    }
  }

  private void handleChunkLoad(ChunkLoadEvent event) {
    final Chunk chunk = event.getChunk();
    if(isTestChunk(chunk)){
      //printStackTrace();
      broadcastMessage("Test chunk loading");
    }
    if (stateStorer.isGettingFromAfar(chunk)) {
      return;
    }
    long removeTime = currentTimeMillis();
    stateStorer.getFromAfar(chunk).thenAccept(statesToBeAdded -> {
      if (statesToBeAdded == null || statesToBeAdded.isEmpty()) {
        return;
      }
      statesToBeAdded.forEach(stateStorer::storeHere);
      if (debug != null) {
        debug.accept(getConsoleSender(), statesToBeAdded.size() + " states were pulled from afar into chunk " + ChatColor.RED + chunk.getX() + ", " + chunk.getZ() + " in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms.");
      }
    });
  }

  private void handleWorldUnload(WorldUnloadEvent event) {
    stateStorer.waitUntilDone(event.getWorld());
    long removeTime = 0;
    if (debug != null) {
      removeTime = currentTimeMillis();
    }
    final Collection<MultiblockState> unloadedStates = stateStorer.getHere(event.getWorld());
    if (unloadedStates == null) {
      return;
    }
    unloadedStates.forEach(state -> {
      stateStorer.removeFromHere(state);
      stateStorer.storeAway(state);
    });
    if (debug != null) {
      debug.accept(getConsoleSender(), unloadedStates.size() + " states were removed from world " + ChatColor.RED + event.getWorld().getName() + " in " + (currentTimeMillis() - removeTime) + " ms.");
    }
  }

  @SuppressWarnings("unchecked cast")
  private <T extends Event> void listen(Class<T> clazz, Consumer<T> handler) {
    getServer().getPluginManager().registerEvent(clazz, new Listener() {
    }, EventPriority.NORMAL, (listener, event) -> handler.accept((T) event), this.plugin);
  }

  private static String removeTimeMsg(long removeTime) {
    return "Multiblock removed in " + ChatColor.LIGHT_PURPLE + (currentTimeMillis() - removeTime) + ChatColor.RESET + " ms";
  }
  private boolean isTestChunk(Chunk chunk){
    return chunk.getX()==MultiblockAPI.garbageChunkX && chunk.getZ()==MultiblockAPI.garbageChunkZ;
  }
}
