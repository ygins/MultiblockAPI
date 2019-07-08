package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.util.NamespacedKey;
import com.gitlab.avelyn.architecture.base.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimpleDataTunnelRegistry extends Component implements DataTunnelRegistry {
  private final Map<NamespacedKey, StateDataTunnel> storageMethodMap = new HashMap<>();

  public SimpleDataTunnelRegistry() {
    onDisable(storageMethodMap::clear);
  }

  @Override
  public void register(NamespacedKey namespacedKey, StateDataTunnel storageMethod) {
    storageMethodMap.put(namespacedKey, storageMethod);
  }

  @Override
  public Collection<StateDataTunnel> getAllStorageMethods() {
    return storageMethodMap.values();
  }

  @Override
  public StateDataTunnel getStorageMethod(NamespacedKey namespacedKey) {
    return storageMethodMap.get(namespacedKey);
  }

}
