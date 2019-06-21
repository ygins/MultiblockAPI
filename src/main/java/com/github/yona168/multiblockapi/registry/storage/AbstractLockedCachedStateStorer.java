package com.github.yona168.multiblockapi.registry.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractLockedCachedStateStorer implements StateStorer {
  private final Map<Location, MultiblockState> stateByLocation = new HashMap<>();
  private final Multimap<ChunkCoords, MultiblockState> stateByChunk = HashMultimap.create();
  private final Multimap<World, MultiblockState> stateByWorld = HashMultimap.create();
  private final Map<Chunk, World> processingChunks = new ConcurrentHashMap<>();
  private final Plugin plugin;
  private final Map<Chunk, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

  public AbstractLockedCachedStateStorer(Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean isGettingFromAfar(Chunk chunk) {
    return processingChunks.containsKey(chunk);
  }

  @Override
  public void waitUntilDone() {
    while (!processingChunks.isEmpty()) {

    }
  }

  @Override
  public CompletableFuture<Collection<MultiblockState>> getFromAfar(Chunk chunk) {
    CompletableFuture<Collection<MultiblockState>> returning = new CompletableFuture<>();
    processingChunks.put(chunk, chunk.getWorld());
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      Lock readLock = lockFor(chunk).readLock();
      readLock.lock();
      returning.complete(initGetFromAfar(chunk));
      processingChunks.remove(chunk);
      readLock.unlock();
    });
    return returning;
  }

  @Override
  public CompletableFuture<Void> storeAway(MultiblockState state) {
    CompletableFuture<Void> returning = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      Lock writeLock = lockFor(state.getTriggerChunk()).writeLock();
      writeLock.lock();
      initStoreAway(state);
      returning.complete(null);
      writeLock.unlock();
    });
    return returning;
  }

  protected abstract Collection<MultiblockState> initGetFromAfar(Chunk chunk);

  protected abstract void initStoreAway(MultiblockState state);

  @Override
  public void storeHere(MultiblockState state) {
    state.getAllBlocksLocs().stream().map(AbstractLockedCachedStateStorer::normalize).forEach(loc -> stateByLocation.put(loc, state));
    state.getOccupiedChunks().forEach(loc -> stateByChunk.put(loc, state));
    stateByWorld.put(state.getWorld(), state);
  }

  private static Location normalize(Location loc) {
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
    return stateByChunk.get(ChunkCoords.fromChunk(chunk));
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

  @Override
  public void storeAllAway() {
    stateByWorld.values().forEach(this::storeAway);
  }

  private ReadWriteLock lockFor(Chunk chunk) {
    return lockMap.compute(chunk, ($, lock) -> lock == null ? new ReentrantReadWriteLock(true) : lock);
  }
}
