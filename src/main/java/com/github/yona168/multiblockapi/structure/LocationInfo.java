package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.state.MultiblockState;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Set;

public class LocationInfo {
  private final Block bottomLeftCorner;
  private final MultiblockState.Orientation orientation;
  private final Set<Location> allBlockLocations;

  public LocationInfo(Block bottomLeftCorner, Set<Location> allBlockLocations, MultiblockState.Orientation orientation) {
    this.bottomLeftCorner = bottomLeftCorner;
    this.orientation = orientation;
    this.allBlockLocations=allBlockLocations;
  }

  public Block getBottomLeftCorner() {
    return bottomLeftCorner;
  }

  public MultiblockState.Orientation getOrientation() {
    return orientation;
  }

  public Set<Location> getAllBlockLocations(){
    return allBlockLocations;
  }
}
