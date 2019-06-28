package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import com.gitlab.avelyn.architecture.base.Toggleable;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public interface MultiblockState extends Toggleable {

  Block getBlockByPattern(int level, int row, int column);

  Location getTriggerBlockLoc();

  Set<Location> getStructureBlocksLocs();

  Multiblock<MultiblockState> getMultiblock();

  Set<Location> getAllBlocksLocs();

  Set<ChunkCoords> getOccupiedChunks();

  Chunk getTriggerChunk();

  Orientation getOrientation();

  UUID getUniqueid();

  default World getWorld() {
    return getTriggerBlockLoc().getWorld();
  }

  void onEnable();
  void onDisable();

  void destroy();
  void onDestroy();

  boolean isDestroyed();

  enum Orientation {
    NORTH {
      @Override
      Block getBlock(int level, int row, int column, Block bottomLeftCorner) {
        return bottomLeftCorner.getRelative(column, level, row);
      }
    },
    SOUTH {
      @Override
      Block getBlock(int level, int row, int column, Block bottomLeftCorner) {
        return bottomLeftCorner.getRelative(-column, level, -row);
      }
    },
    EAST {
      @Override
      Block getBlock(int level, int row, int column, Block bottomLeftCorner) {
        return bottomLeftCorner.getRelative(-row, level, column);
      }
    },
    WEST {
      @Override
      Block getBlock(int level, int row, int column, Block bottomLeftCorner) {
        return bottomLeftCorner.getRelative(row, level, -column);
      }
    };
    private static Map<Integer, Orientation> intMap = new HashMap<>();

    abstract Block getBlock(int level, int row, int column, Block bottomLeftCorner);
  }
}
