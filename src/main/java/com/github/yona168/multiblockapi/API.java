package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.storage.DataTunnelRegistry;

public interface API {
  DataTunnelRegistry getDataTunnelRegistry();
  MultiblockRegistry getMultiblockRegistry();
}
