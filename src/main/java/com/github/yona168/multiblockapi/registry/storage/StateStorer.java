package com.github.yona168.multiblockapi.registry.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Location;

public interface StateStorer<T extends MultiblockState> {
  void store(T t);
  T retrieve(MultiblockState.Orientation orientation, Location triggerLoc, Multiblock multiblock);
}
