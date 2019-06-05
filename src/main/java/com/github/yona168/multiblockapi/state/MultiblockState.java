package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public interface MultiblockState {
  Block getBlockByPattern(int level, int row, int column);

  MultiblockBlock getTriggerBlock();

  Set<MultiblockBlock> getStructureBlocks();

  Multiblock getMultiblock();

  default Set<MultiblockBlock> getAllBlocks() {
    final Set<MultiblockBlock> blocks = new HashSet<>(getStructureBlocks());
    blocks.add(getTriggerBlock());
    return blocks;
  }
}
