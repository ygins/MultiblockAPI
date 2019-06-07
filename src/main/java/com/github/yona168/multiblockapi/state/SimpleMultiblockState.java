package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class SimpleMultiblockState implements MultiblockState {
  private final Orientation orientation;
  private final Block bottomLeftBlock;
  private final Location triggerLoc;
  private final Set<Location> structureBlocks = new HashSet<>();
  private final Multiblock multiblock;
  public SimpleMultiblockState(Multiblock multiblock, Orientation orientation, Block bottomLeftBlock, ThreeDimensionalArrayCoords triggerCoords, Material[][][] pattern) {
    this.multiblock=multiblock;
    this.orientation = orientation;
    this.bottomLeftBlock = bottomLeftBlock;
    this.triggerLoc=orientation.getBlock(triggerCoords.getY(), triggerCoords.getRow(), triggerCoords.getColumn(), bottomLeftBlock).getLocation();
    for (int level = 0; level < pattern.length; level++) {
      for (int row = 0; row < pattern[0].length; row++) {
        for (int column = 0; column < pattern[0][0].length; column++) {
          final Block block = orientation.getBlock(level, row, column, bottomLeftBlock);
          if (!(triggerCoords.getY()==level&&triggerCoords.getRow()==row&&triggerCoords.getColumn()==column)) {
            structureBlocks.add(block.getLocation());
          }
        }
      }
    }
  }

  @Override
  public Location getTriggerBlockLoc() {
    return triggerLoc;
  }

  @Override
  public Set<Location> getStructureBlocksLocs() {
    return structureBlocks;
  }

  @Override
  public Block getBlockByPattern(int level, int row, int column) {
    return orientation.getBlock(level, row, column, bottomLeftBlock);
  }

  @Override
  public Multiblock getMultiblock(){
    return multiblock;
  }

}
