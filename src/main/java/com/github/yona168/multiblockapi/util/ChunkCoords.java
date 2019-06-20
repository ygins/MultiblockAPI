package com.github.yona168.multiblockapi.util;

import org.bukkit.Chunk;
import org.bukkit.World;

public interface ChunkCoords {

  int getX();

  int getZ();

  World getWorld();

  default Chunk toChunk() {
    return getWorld().getChunkAt(getX(), getZ());
  }

  static ChunkCoords fromData(World world, int x, int z) {
    return new SimpleChunkCoords(x, z, world);
  }

  static ChunkCoords fromChunk(Chunk chunk) {
    return new SimpleChunkCoords(chunk);
  }
}
