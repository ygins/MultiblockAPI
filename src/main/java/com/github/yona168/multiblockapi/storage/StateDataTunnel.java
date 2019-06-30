package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Chunk;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface StateDataTunnel {

  void storeAway(MultiblockState state);

  CompletableFuture<Void> storeAwayAsync(MultiblockState t);

  Collection<MultiblockState> getFromAfar(Chunk chunk);

  CompletableFuture<Collection<MultiblockState>> getFromAfarAsync(Chunk chunk);

  boolean isProcessingInDB(Chunk chunk);

  void removeFromAfar(MultiblockState state);

  CompletableFuture<Void> removeFromAfarAsync(MultiblockState state);

  void blockUntilAsyncsDone();
}
