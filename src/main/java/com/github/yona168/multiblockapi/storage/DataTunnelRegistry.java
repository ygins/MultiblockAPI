package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.util.NamespacedKey;
import org.bukkit.Chunk;

import java.util.Collection;

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
