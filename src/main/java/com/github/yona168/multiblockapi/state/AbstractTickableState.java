package com.github.yona168.multiblockapi.state;

import com.github.yona168.multiblockapi.structure.LocationInfo;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.UUID;

import static org.bukkit.Bukkit.getScheduler;

public abstract class AbstractTickableState extends SimpleMultiblockState implements Tickable {
  private final Plugin plugin;
  private int taskId;

  public AbstractTickableState(Multiblock multiblock, LocationInfo locInfo, Plugin plugin) {
    this(UUID.randomUUID(), multiblock, locInfo, plugin);
  }

  protected AbstractTickableState(UUID uuid, Multiblock multiblock, LocationInfo locationInfo, Plugin plugin) {
    super(uuid, multiblock, locationInfo);
    this.plugin = plugin;
  }

  @Override
  public final void onEnable() {
    BukkitScheduler scheduler = getScheduler();
    taskId = isAsync() ? scheduler.runTaskTimerAsynchronously(plugin, this, 0, getPeriod()).getTaskId() :
            scheduler.runTaskTimer(plugin, this, 0, getPeriod()).getTaskId();
    postTaskEnable();
  }

  @Override
  public final void onDisable() {
    getScheduler().cancelTask(taskId);
    postTaskDisable();
  }

  public void postTaskEnable() {

  }

  public void postTaskDisable() {

  }
}
