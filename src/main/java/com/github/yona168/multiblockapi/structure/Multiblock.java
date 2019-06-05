package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.state.MultiblockState;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.function.Consumer;

public interface Multiblock<T extends MultiblockState> {
  Optional<T> generateStateFrom(PlayerInteractEvent event);

  void doThisOnClick(Consumer<PlayerInteractEvent> eventConsumer);

  void doClickActions(PlayerInteractEvent event, MultiblockState state);

  Material[][][] getPattern();
}
