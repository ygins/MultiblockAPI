package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Multiblock<T extends MultiblockState> {
  Optional<T> generateStateFrom(PlayerInteractEvent event);

  void onClick(BiConsumer<PlayerInteractEvent, T> eventConsumer);

  void doClickActions(PlayerInteractEvent event, T state);

  Material[][][] getPattern();

  ThreeDimensionalArrayCoords getTriggerCoords();

  String getId();
}
