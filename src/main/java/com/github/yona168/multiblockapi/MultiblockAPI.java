package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.registry.SimpleMultiblockRegistry;
import com.github.yona168.multiblockapi.storage.DataTunnelRegistry;
import com.github.yona168.multiblockapi.storage.SimpleDataTunnelRegistry;
import com.github.yona168.multiblockapi.storage.SimpleStateCache;
import com.github.yona168.multiblockapi.storage.StateCache;
import com.gitlab.avelyn.core.components.ComponentPlugin;

import static org.bukkit.Bukkit.broadcastMessage;

//Legit everything in here is garbage test as of now
public class MultiblockAPI extends ComponentPlugin {
  private final MultiblockRegistry multiblockRegistry;
  private final DataTunnelRegistry dataTunnelRegistry;
  private final StateCache stateCache;

  public MultiblockAPI() {
    multiblockRegistry = new SimpleMultiblockRegistry();
    stateCache = new SimpleStateCache();
    dataTunnelRegistry = new SimpleDataTunnelRegistry();
    addChild(new StateLoaderListeners(stateCache, multiblockRegistry, dataTunnelRegistry, this, null));
  }

}
