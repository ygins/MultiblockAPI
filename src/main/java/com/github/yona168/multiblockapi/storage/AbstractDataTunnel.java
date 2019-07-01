package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.gitlab.avelyn.architecture.base.Component;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.newSetFromMap;
import static org.bukkit.Bukkit.broadcastMessage;

public abstract class AbstractDataTunnel extends Component implements StateDataTunnel {
  private final Plugin plugin;
  private final Map<Chunk, World> processingChunks;
  private final Map<Chunk, ReentrantReadWriteLock> lockMap;
  private final Set<Integer> runningAsyncTasks;

  public AbstractDataTunnel(Plugin plugin) {
    this.plugin = plugin;
    lockMap = new ConcurrentHashMap<>();
    processingChunks = new ConcurrentHashMap<>();
    runningAsyncTasks = newSetFromMap(new ConcurrentHashMap<>());
    onDisable(() -> {
      lockMap.clear();
      processingChunks.clear();
      runningAsyncTasks.clear();
    });
  }


  abstract Collection<MultiblockState> initGetFromAfar(Chunk chunk);

  abstract void initRemoveFromAfar(MultiblockState state);

  abstract void initStoreAway(MultiblockState state);

  @Override
  public void storeAway(MultiblockState state) {
    withWriteLockFor(state.getTriggerChunk(), () -> initStoreAway(state));
  }

  @Override
  public CompletableFuture<Void> storeAwayAsync(MultiblockState state) {
    CompletableFuture<Void> returning = new CompletableFuture<>();
    asyncProccess(() -> {
      withWriteLockFor(state.getTriggerChunk(), () -> {
        initStoreAway(state);
        returning.complete(null);
      });
    });
    return returning;
  }

  @Override
  public boolean isProcessingInDB(Chunk chunk) {
    return processingChunks.containsKey(chunk);
  }

  @Override
  public Collection<MultiblockState> getFromAfar(Chunk chunk) {
    return withReadLockFor(chunk, $ -> initGetFromAfar(chunk));
  }

  @Override
  public CompletableFuture<Collection<MultiblockState>> getFromAfarAsync(Chunk chunk) {
    CompletableFuture<Collection<MultiblockState>> returning = new CompletableFuture<>();
    asyncProccess(() ->
            returning.complete(withReadLockFor(chunk, $ -> initGetFromAfar(chunk))));
    return returning;
  }

  @Override
  public void removeFromAfar(MultiblockState state) {
    withWriteLockFor(state.getTriggerChunk(), () -> initRemoveFromAfar(state));
  }

  @Override
  public CompletableFuture<Void> removeFromAfarAsync(MultiblockState state) {
    CompletableFuture<Void> returning = new CompletableFuture<>();
    asyncProccess(()-> {
      withWriteLockFor(state.getTriggerChunk(), () -> {
        initRemoveFromAfar(state);
        returning.complete(null);
      });
    });
    return returning;
  }

  @Override
  public void blockUntilAsyncsDone() {
    while (!runningAsyncTasks.isEmpty()) {

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

  private void asyncProccess(Runnable action) {
    new BukkitRunnable() {
      @Override
      public void run() {
        runningAsyncTasks.add(this.getTaskId());
        action.run();
        runningAsyncTasks.remove(this.getTaskId());
      }
    }.runTaskAsynchronously(plugin);
  }
}
