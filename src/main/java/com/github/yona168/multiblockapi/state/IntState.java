package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.LocationInfo;
import com.github.yona168.multiblockapi.structure.Multiblock;

import static org.bukkit.Bukkit.broadcastMessage;

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

  @Override
  public void onEnable() {
    broadcastMessage("This state is enabled!");
  }

  @Override
  public void onDisable() {
    broadcastMessage("This state is disabled!");
  }

  @Override
  public void onDestroy() {
    broadcastMessage("This state is destroyed!");
  }

}
