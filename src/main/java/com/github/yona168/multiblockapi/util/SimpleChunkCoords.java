package com.github.yona168.multiblockapi.util;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Objects;

public class SimpleChunkCoords implements ChunkCoords {
  private final int x;
  private final int z;
  private final World world;

  public SimpleChunkCoords(int x, int z, World world) {
    this.x = x;
    this.z = z;
    this.world = world;
  }

  public SimpleChunkCoords(Chunk chunk) {
    this(chunk.getX(), chunk.getZ(), chunk.getWorld());
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getZ() {
    return z;
  }

  @Override
  public World getWorld() {
    return world;
  }

  @Override
  public boolean equals(Object o) {
    {
      if (!(o instanceof ChunkCoords)) {
        return false;
      }
      ChunkCoords other = (ChunkCoords) o;
      return other.getX() == getX() && other.getZ() == getZ() && getWorld().equals(other.getWorld());
    }
  }

  @Override
  public int hashCode(){
    return Objects.hash(world,x,z);
  }
}
