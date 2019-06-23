package com.github.yona168.multiblockapi.registry.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

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


  abstract Collection<MultiblockState> initGetFromAfar(Chunk chunk);

  abstract void initRemoveFromAfar(MultiblockState state);

  abstract void initStoreAway(MultiblockState state);

  @Override
  public void storeAway(MultiblockState state){
    withWriteLockFor(state.getTriggerChunk(), ()->initStoreAway(state));
  }

  @Override
  public CompletableFuture<Void> storeAwayAsync(MultiblockState state) {
    CompletableFuture<Void> returning = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      withWriteLockFor(state.getTriggerChunk(), () -> {
        initStoreAway(state);
        returning.complete(null);
      });
    });
    return returning;
  }

  @Override
  public void storeAllAway() {
    stateByWorld.values().forEach(this::storeAway);
  }

  @Override
  public CompletableFuture<Void> storeAllAwayAsync() {
    CompletableFuture<Void> returning = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      stateByWorld.values().stream().map(this::storeAwayAsync).forEach(CompletableFuture::join);
      returning.complete(null);
    });
    return returning;
  }

  @Override
  public boolean isGettingFromAfar(Chunk chunk) {
    return processingChunks.containsKey(chunk);
  }

  @Override
  public Collection<MultiblockState> getFromAfar(Chunk chunk) {
    return withReadLockFor(chunk, $ -> initGetFromAfar(chunk));
  }

  @Override
  public CompletableFuture<Collection<MultiblockState>> getFromAfarAsync(Chunk chunk) {
    CompletableFuture<Collection<MultiblockState>> returning = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            returning.complete(withReadLockFor(chunk, $ -> initGetFromAfar(chunk))));
    return returning;
  }


  @Override
  public void storeHere(MultiblockState state) {
    state.getAllBlocksLocs().stream().map(AbstractLockedCachedStateStorer::normalize).forEach(loc -> stateByLocation.put(loc, state));
    state.getOccupiedChunks().forEach(loc -> stateByChunk.put(loc, state));
    stateByWorld.put(state.getWorld(), state);
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
  public Collection<MultiblockState> getHere(World world) {
    return stateByWorld.get(world);
  }

  @Override
  public void removeFromHere(MultiblockState state) {
    state.getAllBlocksLocs().forEach(stateByLocation::remove);
    state.getOccupiedChunks().forEach(chunk -> stateByChunk.remove(chunk, state));
    stateByWorld.remove(state.getWorld(), state);
  }

  @Override
  public void removeFromAfar(MultiblockState state){
    withWriteLockFor(state.getTriggerChunk(), ()->initRemoveFromAfar(state));
  }

  @Override
  public CompletableFuture<Void> removeFromAfarAsync(MultiblockState state) {
    CompletableFuture<Void> returning = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      withWriteLockFor(state.getTriggerChunk(), () -> {
        initRemoveFromAfar(state);
        returning.complete(null);
      });
    });
    return returning;
  }

  @Override
  public void clearHere(){
    stateByChunk.clear();
    stateByLocation.clear();
    stateByWorld.clear();
  }

  @Override
  public void waitUntilDone() {
    while (!processingChunks.isEmpty()) {

    }
  }

  private ReadWriteLock lockFor(Chunk chunk) {
    return lockMap.compute(chunk, ($, lock) -> lock == null ? new ReentrantReadWriteLock(true) : lock);
  }

  private void withWriteLockFor(Chunk chunk, Runnable runnable) {
    withLockAndChunk(lockFor(chunk).writeLock(), chunk, runnable);
  }

  private <T> T withReadLockFor(Chunk chunk, Function<Chunk, T> function) {
    return withLockAndChunk(lockFor(chunk).readLock(), chunk, function);
  }

  private <T> T withLockAndChunk(Lock lock, Chunk chunk, Function<Chunk, T> function) {
    lock.lock();
    processingChunks.put(chunk, chunk.getWorld());
    final T result = function.apply(chunk);
    processingChunks.remove(chunk);
    lock.unlock();
    return result;
  }

  private void withLockAndChunk(Lock lock, Chunk chunk, Runnable runnable) {
    lock.lock();
    processingChunks.put(chunk, chunk.getWorld());
    runnable.run();
    processingChunks.remove(chunk);
    lock.unlock();
  }

  private static Location normalize(Location loc) {
    loc.setYaw(0);
    loc.setPitch(0);
    return loc;
  }
}
