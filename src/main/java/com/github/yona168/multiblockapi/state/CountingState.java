package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.LocationInfo;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.broadcastMessage;

public class CountingState extends AbstractTickableState {
  private final int interval;
  private int num=0;
  public CountingState(Multiblock multiblock, LocationInfo locInfo, Plugin plugin, int interval) {
    super(multiblock, locInfo, plugin);
    this.interval=interval;
  }

  @Override
  public int getPeriod() {
    return interval;
  }

  @Override
  public boolean isAsync() {
    return false;
  }

  @Override
  public void postTaskEnable(){
    broadcastMessage("Tickable enabled!");
  }
  @Override
  public void postTaskDisable(){
    broadcastMessage("Tickable disabled!");
  }
  @Override
  public void onDestroy(){
    broadcastMessage("Tickable destroyed!");
  }
  @Override
  public void run() {
    num++;
  }
  public int getNum(){
    return this.num;
  }
}
