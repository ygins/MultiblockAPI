package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.pattern.Pattern;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.storage.StateDataTunnel;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public interface Multiblock<T extends MultiblockState> {
  Optional<T> generateStateFrom(PlayerInteractEvent event);

  void onClick(BiConsumer<PlayerInteractEvent, T> eventConsumer);

  void preStateGenCheck(BiPredicate<PlayerInteractEvent,Multiblock<T>> check);

  void postStateGenCheck(BiPredicate<PlayerInteractEvent,T> check);

  void doClickActions(PlayerInteractEvent event, T state);

  Pattern getPattern();

  NamespacedKey getId();

  StateDataTunnel getDataTunnel();
}
