package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.structure.SimpleMultiblock;

public class IntState extends SimpleMultiblockState {
  private int x = 1;

  public IntState(Multiblock multiblock, SimpleMultiblock.LocationInfo locInfo) {
    super(multiblock, locInfo);
  }

  public void toggle() {
    x++;
  }

  public int getInt() {
    return x;
  }
}
