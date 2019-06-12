package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.registry.storage.StateStorer;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
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
import java.util.function.Consumer;

import static java.lang.System.currentTimeMillis;
import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getServer;

public class MultiblockRegistry implements Registry<Multiblock<? extends MultiblockState>> {
    private final Set<Multiblock<? extends MultiblockState>> multiblocks = new HashSet<>();
    private final Plugin plugin;
    private final boolean debug;
    private long removeTime = 0;
    private final StateStorer stateStorer;

    public MultiblockRegistry(Plugin plugin, StateStorer stateStorer, boolean debug) {
        this.plugin = plugin;
        this.debug = debug;
        this.stateStorer = stateStorer;
        listen(PlayerInteractEvent.class, this::handleInteract);
        listen(BlockBreakEvent.class, this::handleBlockBreak);
        listen(ChunkUnloadEvent.class, this::handleChunkUnload);
        listen(WorldUnloadEvent.class, this::handleWorldUnload);
        listen(ChunkLoadEvent.class, this::handleChunkLoad);
    }

    public MultiblockRegistry(Plugin plugin, StateStorer stateStorer) {
        this(plugin, stateStorer, false);
    }


    private void handleInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        final Location clickedLoc = event.getClickedBlock().getLocation();
        if(stateStorer.isGettingFromAfar(clickedLoc.getChunk())){
            return;
        }
        MultiblockState existingState = stateStorer.getHere(clickedLoc);
        long before = currentTimeMillis();
        if (existingState == null) {
            multiblocks.stream().map(multiblock -> multiblock.generateStateFrom(event)).filter(Optional::isPresent).map(Optional::get).findFirst().ifPresent(multiblockState -> {
                stateStorer.storeHere(multiblockState);
                multiblockState.getMultiblock().doClickActions(event, multiblockState);
                if (debug) {
                    long difference = currentTimeMillis() - before;
                    broadcastMessage("Multiblock has been registered, and the whole process took " + ChatColor.LIGHT_PURPLE + difference + ChatColor.RESET + " millis");
                }
            });
        } else {
            existingState.getMultiblock().doClickActions(event, existingState);
            long difference = currentTimeMillis() - before;
            broadcastMessage("Multiblock was detected as registered, and the whole process took " + ChatColor.LIGHT_PURPLE + difference + ChatColor.RESET + " millis");
        }
    }

    private void handleBlockBreak(BlockBreakEvent event) {
        if (stateStorer.isGettingFromAfar(event.getBlock().getLocation().getChunk())) {
            return;
        }
        if (debug) {
            removeTime = currentTimeMillis();
        }
        final Location broken = event.getBlock().getLocation();
        final MultiblockState brokenState = stateStorer.getHere(broken);
        if (brokenState != null) {
            stateStorer.removeFromEverywhere(brokenState);
        }
    }

    private void handleChunkUnload(ChunkUnloadEvent event) {
        stateStorer.waitUntilDone(event.getChunk());
        if (debug) {
            removeTime = currentTimeMillis();
        }
        final Collection<MultiblockState> statesInChunk = stateStorer.getHere(event.getChunk());
        if (statesInChunk != null) {
            statesInChunk.forEach(state -> {
                stateStorer.removeFromHere(state);
                stateStorer.storeAway(state);
            });
        }
    }

    private void handleChunkLoad(ChunkLoadEvent event) {
        if (stateStorer.isGettingFromAfar(event.getChunk())) {
            return;
        }
        stateStorer.getFromAfar(event.getChunk()).thenAccept(statesToBeAdded -> {
            if (statesToBeAdded == null) {
                return;
            }
            statesToBeAdded.forEach(state -> {
                stateStorer.storeHere(state);
                stateStorer.removeFromAfar(state);
            });
        });
    }

    private void handleWorldUnload(WorldUnloadEvent event) {
        stateStorer.waitUntilDone(event.getWorld());
        if (debug) {
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
    }

    @Override
    public void register(Multiblock<? extends MultiblockState> item) {
        this.multiblocks.add(item);
    }

    @SuppressWarnings("unchecked cast")
    private <T extends Event> void listen(Class<T> clazz, Consumer<T> handler) {
        getServer().getPluginManager().registerEvent(clazz, new Listener() {
        }, EventPriority.NORMAL, (listener, event) -> handler.accept((T) event), this.plugin);
    }
}
