package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.state.MultiblockState;
import org.bukkit.event.player.PlayerInteractEvent;

public interface StateCreator<T extends MultiblockState> {
  T createFrom(Multiblock<T> multiblock, LocationInfo locationInfo, PlayerInteractEvent event);
}
