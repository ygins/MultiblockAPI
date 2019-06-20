package com.github.yona168.multiblockapi.registry.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface StateStorer {
  void onRegister(Multiblock multiblock);

  void storeAway(MultiblockState t);

  void storeAllHereAway();

  CompletableFuture<Collection<MultiblockState>> getFromAfar(Chunk chunk);

  boolean isGettingFromAfar(Chunk chunk);

  void waitUntilDone();

  void waitUntilDone(Chunk chunk);

  void waitUntilDone(World world);

  void storeHere(MultiblockState multiblockState);

  MultiblockState getHere(Location location);

  Collection<MultiblockState> getHere(Chunk chunk);

  Collection<MultiblockState> getHere(World world);

  void removeFromHere(MultiblockState state);

  void removeFromAfar(MultiblockState state);

  default void removeFromEverywhere(MultiblockState state) {
    removeFromHere(state);
    removeFromAfar(state);
  }

  default void storeEverywhere(MultiblockState state) {
    storeHere(state);
    storeAway(state);
  }
}
