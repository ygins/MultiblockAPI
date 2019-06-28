package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface DataTunnelRegistry {
  void register(NamespacedKey namespacedKey, StateDataTunnel storageMethod);

  Collection<StateDataTunnel> getAllStorageMethods();

  StateDataTunnel getStorageMethod(NamespacedKey namespacedKey);

  default void waitForAllAsyncsDone() {
    getAllStorageMethods().forEach(StateDataTunnel::blockUntilAsyncsDone);
  }

  default boolean isProcessing(Chunk chunk){
    return getAllStorageMethods().stream().anyMatch(tunnel->tunnel.isProcessingInDB(chunk));
  }
}
