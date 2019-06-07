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
}
