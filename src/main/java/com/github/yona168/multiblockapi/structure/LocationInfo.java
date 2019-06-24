package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.state.MultiblockState;
import org.bukkit.block.Block;

public class LocationInfo {
  private final Block bottomLeftCorner;
  private final MultiblockState.Orientation orientation;

  public LocationInfo(Block bottomLeftCorner, MultiblockState.Orientation orientation) {
    this.bottomLeftCorner = bottomLeftCorner;
    this.orientation = orientation;
  }

  public Block getBottomLeftCorner() {
    return bottomLeftCorner;
  }

  public MultiblockState.Orientation getOrientation() {
    return orientation;
  }
}
