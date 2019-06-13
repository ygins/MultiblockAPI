package com.github.yona168.multiblockapi.registry.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.lang.System.currentTimeMillis;
import static org.bukkit.Bukkit.broadcastMessage;

public abstract class AbstractCachedStateStorer implements StateStorer {
    private final Map<Location, MultiblockState> stateByLocation = new HashMap<>();
    private final Multimap<Chunk, MultiblockState> stateByChunk = HashMultimap.create();
    private final Multimap<World, MultiblockState> stateByWorld = HashMultimap.create();
    private final Map<Chunk, World> processingChunks = new HashMap<>();
    private final Plugin plugin;

    public AbstractCachedStateStorer(Plugin plugin){
        this.plugin=plugin;
    }

    @Override
    public boolean isGettingFromAfar(Chunk chunk){
        return processingChunks.containsKey(chunk);
    }

    @Override
    public void waitUntilDone(){
        while(processingChunks.size()!=0){

        }
    }

    @Override
    public void waitUntilDone(Chunk chunk){
        while(isGettingFromAfar(chunk)){

        }
    }

    @Override
    public void waitUntilDone(World world){
        while(processingChunks.containsValue(world)){

        }
    }

    @Override
    public CompletableFuture<Collection<MultiblockState>> getFromAfar(Chunk chunk) {
        CompletableFuture<Collection<MultiblockState>> returning = new CompletableFuture<>();
        processingChunks.put(chunk, chunk.getWorld());
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->returning.complete(initGetFromAfar(chunk)));
        processingChunks.remove(chunk);
        return returning;
    }

    protected abstract Collection<MultiblockState> initGetFromAfar(Chunk chunk);

    @Override
    public void storeHere(MultiblockState state) {
        state.getAllBlocksLocs().stream().map(AbstractCachedStateStorer::normalize).forEach(loc -> stateByLocation.put(loc, state));
        state.getOccupiedChunks().forEach(loc -> stateByChunk.put(loc, state));
        stateByWorld.put(state.getWorld(), state);
    }
    private static Location normalize(Location loc){
        loc.setYaw(0);
        loc.setPitch(0);
        return loc;
    }

    @Override
    public MultiblockState getHere(Location location) {
        return stateByLocation.get(location);
    }

    @Override
    public Collection<MultiblockState> getHere(Chunk chunk) {
        return stateByChunk.get(chunk);
    }

    @Override
    public void removeFromHere(MultiblockState state) {
        state.getAllBlocksLocs().forEach(stateByLocation::remove);
        state.getOccupiedChunks().forEach(chunk -> stateByChunk.remove(chunk, state));
        stateByWorld.remove(state.getWorld(), state);
    }


    @Override
    public Collection<MultiblockState> getHere(World world) {
        return stateByWorld.get(world);
    }
}
