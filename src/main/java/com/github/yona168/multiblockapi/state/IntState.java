package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.LocationInfo;
import com.github.yona168.multiblockapi.structure.Multiblock;

public class IntState extends SimpleMultiblockState {
  private int x = 0;

  public IntState(Multiblock multiblock, LocationInfo locInfo) {
    super(multiblock, locInfo);
  }

  public void toggle() {
    x++;
  }

  public int getInt() {
    return x;
  }

}
