package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface StateCache {

  void store(MultiblockState multiblockState);

  MultiblockState getAt(Location location);

  Collection<MultiblockState> getAt(Chunk chunk);

  Collection<MultiblockState> getAt(World world);

  Collection<MultiblockState> getAll();

  void remove(MultiblockState state);

  void clear();
}
