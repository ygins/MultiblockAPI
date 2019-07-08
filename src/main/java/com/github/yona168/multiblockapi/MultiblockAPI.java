package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.registry.SimpleMultiblockRegistry;
import com.github.yona168.multiblockapi.storage.DataTunnelRegistry;
import com.github.yona168.multiblockapi.storage.SimpleDataTunnelRegistry;
import com.github.yona168.multiblockapi.storage.SimpleStateCache;
import com.github.yona168.multiblockapi.storage.StateCache;
import com.gitlab.avelyn.core.components.ComponentPlugin;


public class MultiblockAPI extends ComponentPlugin implements API {
  private final MultiblockRegistry multiblockRegistry;
  private final DataTunnelRegistry dataTunnelRegistry;
  private static API api;

  public MultiblockAPI() {
    multiblockRegistry = new SimpleMultiblockRegistry();
    final StateCache stateCache = new SimpleStateCache();
    dataTunnelRegistry = new SimpleDataTunnelRegistry();
    onEnable(()->api=this);
    onDisable(()->api=null);
    addChild(new StateLoaderListeners(stateCache, multiblockRegistry, dataTunnelRegistry, this, null));
  }

  @Override
  public DataTunnelRegistry getDataTunnelRegistry() {
    return dataTunnelRegistry;
  }

  @Override
  public MultiblockRegistry getMultiblockRegistry() {
    return multiblockRegistry;
  }

  public static API getAPI() {
    return api;
  }
}
