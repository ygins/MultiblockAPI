package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.LocationInfo;
import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
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

public class SimpleMultiblockState implements MultiblockState {
  private final Orientation orientation;
  private final Block bottomLeftBlock;
  private final Location triggerLoc;
  private final Set<Location> structureBlocks;
  private final Set<Location> allBlocks;
  private final Set<ChunkCoords> occupiedChunks;
  private final Chunk triggerChunk;
  private final Multiblock multiblock;
  private final UUID uuid;

  //Whether or not the state exists within the world at the moment (not offloaded)
  private boolean enabled;
  //Whether or not the state is destroyed (Multiblock is destroyed)
  private boolean destroyed;

  public SimpleMultiblockState(Multiblock multiblock, Orientation orientation, Block bottomLeftBlock, Set<Location> allBlocks, ThreeDimensionalArrayCoords triggerCoords, Material[][][] pattern) {
    this.uuid = randomUUID();
    this.enabled = false;
    this.destroyed = false;
    this.multiblock = multiblock;
    this.orientation = orientation;
    this.bottomLeftBlock = bottomLeftBlock;
    this.triggerLoc = orientation.getBlock(triggerCoords.getY(), triggerCoords.getRow(), triggerCoords.getColumn(), bottomLeftBlock).getLocation();
    this.allBlocks=allBlocks;
    Set<Location> allBlocksCopy=new HashSet<>(allBlocks);
    allBlocksCopy.remove(triggerLoc);
    this.structureBlocks=allBlocksCopy;
    occupiedChunks = allBlocks.stream().map(Location::getChunk).map(ChunkCoords::fromChunk).collect(Collectors.toSet());
    this.triggerChunk = triggerLoc.getChunk();
  }

  public SimpleMultiblockState(Multiblock multiblock, LocationInfo locInfo) {
    this(multiblock, locInfo.getOrientation(), locInfo.getBottomLeftCorner(), locInfo.getAllBlockLocations(), multiblock.getTriggerCoords(), multiblock.getPattern());
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
  public boolean isDestroyed() {
    return destroyed;
  }

  @Override
  public void destroy() {
    if (destroyed) {
      throw new IllegalStateException("This State is already destroyed!");
    } else {
      onDestroy();
      destroyed = true;
    }
  }

  @Override
  public void onDestroy() {

  }

  @Override
  public MultiblockState enable() {
    if (isEnabled()) {
      throw new IllegalStateException("This state is already enabled!");
    } else {
      onEnable();
      enabled = true;
      return this;
    }
  }

  @Override
  public MultiblockState disable() {
    if (!isEnabled()) {
      throw new IllegalStateException("This state is already disabled!");
    } else {
      onDisable();
      enabled = false;
      return this;
    }
  }

  @Override
  public void onEnable() {

  }

  @Override
  public void onDisable() {

  }

  @Override
  public boolean isEnabled() {
    return enabled;
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
