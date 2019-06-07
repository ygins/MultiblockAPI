package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public interface MultiblockState {
  Block getBlockByPattern(int level, int row, int column);

  Location getTriggerBlockLoc();

  Set<Location> getStructureBlocksLocs();

  Multiblock getMultiblock();

  default Set<Location> getAllBlocksLocs() {
    final Set<Location> blocks = new HashSet<>(getStructureBlocksLocs());
    blocks.add(getTriggerBlockLoc());
    return blocks;
  }

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

    abstract Block getBlock(int level, int row, int column, Block bottomLeftCorner);
  }
}
