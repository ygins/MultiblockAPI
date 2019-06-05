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
  private final MultiblockBlock trigger;
  private final Set<MultiblockBlock> structureBlocks = new HashSet<>();
  private final Multiblock multiblock;
  public SimpleMultiblockState(Multiblock multiblock, Orientation orientation, Block bottomLeftBlock, ThreeDimensionalArrayCoords triggerCoords, Material[][][] pattern) {
    this.multiblock=multiblock;
    this.orientation = orientation;
    this.bottomLeftBlock = bottomLeftBlock;
    final Location triggerLoc=orientation.getBlock(triggerCoords.getY(), triggerCoords.getRow(), triggerCoords.getColumn(), bottomLeftBlock).getLocation();
    this.trigger=new MultiblockBlock() {
      @Override
      public Location getLocation() {
        return triggerLoc;
      }

      @Override
      public Function getFunction() {
        return Function.TRIGGER;
      }
    };
    for (int level = 0; level < pattern.length; level++) {
      for (int row = 0; row < pattern[0].length; row++) {
        for (int column = 0; column < pattern[0][0].length; column++) {
          final Block block = orientation.getBlock(level, row, column, bottomLeftBlock);
          if (!(triggerCoords.getY()==level&&triggerCoords.getRow()==row&&triggerCoords.getColumn()==column)) {
            structureBlocks.add(new MultiblockBlock() {
              @Override
              public Location getLocation() {
                return block.getLocation();
              }

              @Override
              public Function getFunction() {
                return Function.STRUCTURE;
              }
            });
          }
        }
      }
    }
  }

  @Override
  public MultiblockBlock getTriggerBlock() {
    return trigger;
  }

  @Override
  public Set<MultiblockBlock> getStructureBlocks() {
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

  public enum Orientation {
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
