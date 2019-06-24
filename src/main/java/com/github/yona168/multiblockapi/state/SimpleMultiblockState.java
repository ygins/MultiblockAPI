package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.LocationInfo;
import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import com.gitlab.avelyn.architecture.base.Component;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;

public class SimpleMultiblockState extends Component implements MultiblockState {
  private final Orientation orientation;
  private final Block bottomLeftBlock;
  private final Location triggerLoc;
  private final Set<Location> structureBlocks = new HashSet<>();
  private final Set<Location> allBlocks;
  private final Set<ChunkCoords> occupiedChunks;
  private final Chunk triggerChunk;
  private final Multiblock multiblock;
  private final UUID uuid;

  public SimpleMultiblockState(Multiblock multiblock, Orientation orientation, Block bottomLeftBlock, ThreeDimensionalArrayCoords triggerCoords, Material[][][] pattern) {
    this.uuid = randomUUID();
    this.multiblock = multiblock;
    this.orientation = orientation;
    this.bottomLeftBlock = bottomLeftBlock;
    this.triggerLoc = orientation.getBlock(triggerCoords.getY(), triggerCoords.getRow(), triggerCoords.getColumn(), bottomLeftBlock).getLocation();
    for (int level = 0; level < pattern.length; level++) {
      for (int row = 0; row < pattern[0].length; row++) {
        for (int column = 0; column < pattern[0][0].length; column++) {
          final Block block = orientation.getBlock(level, row, column, bottomLeftBlock);
          if (pattern[level][row][column] != null && !(triggerCoords.getY() == level && triggerCoords.getRow() == row && triggerCoords.getColumn() == column)) {
            structureBlocks.add(block.getLocation());
          }
        }
      }
    }
    allBlocks = new HashSet<>(structureBlocks);
    allBlocks.add(triggerLoc);
    occupiedChunks = allBlocks.stream().map(Location::getChunk).map(ChunkCoords::fromChunk).collect(Collectors.toSet());
    this.triggerChunk = triggerLoc.getChunk();
  }

  public SimpleMultiblockState(Multiblock multiblock, LocationInfo locInfo) {
    this(multiblock, locInfo.getOrientation(), locInfo.getBottomLeftCorner(), multiblock.getTriggerCoords(), multiblock.getPattern());
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
  public Multiblock getMultiblock() {
    return multiblock;
  }

  @Override
  public Set<Location> getAllBlocksLocs() {
    return allBlocks;
  }

  @Override
  public Set<ChunkCoords> getOccupiedChunks() {
    return this.occupiedChunks;
  }

  @Override
  public Chunk getTriggerChunk() {
    return triggerChunk;
  }

  @Override
  public Orientation getOrientation() {
    return orientation;
  }

  @Override
  public UUID getUniqueid() {
    return uuid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(orientation, triggerLoc, bottomLeftBlock, allBlocks, multiblock);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SimpleMultiblockState)) {
      return false;
    }
    SimpleMultiblockState other = (SimpleMultiblockState) obj;
    if (orientation != other.orientation || this.multiblock != other.multiblock || (!(this.allBlocks.equals(other.allBlocks)))) {
      return false;
    }
    return true;
  }

}
