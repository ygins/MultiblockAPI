package com.github.yona168.multiblockapi.util;

import com.gitlab.avelyn.architecture.base.Toggleable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Function;

import static org.bukkit.Bukkit.getScheduler;

public class ToggleableTasks {
  private static Toggleable toggleableTask(Runnable runnable, Function<Runnable, BukkitTask> function) {
    return new Toggleable() {
      private BukkitTask task;
      private boolean isEnabled = false;

      @Override
      public Toggleable enable() {
        if (!isEnabled()) {
          this.task = function.apply(runnable);
          isEnabled = true;
        }
        return this;
      }

      @Override
      public Toggleable disable() {
        if (isEnabled()) {
          task.cancel();
          isEnabled = false;
        }
        return this;
      }

      @Override
      public boolean isEnabled() {
        return isEnabled;
      }
    };
  }

  public static Toggleable syncRepeating(Plugin plugin, long interval, Runnable runnable) {
    return toggleableTask(runnable, run -> getScheduler().runTaskTimer(plugin, run, 0, interval));
  }

  public static Toggleable asyncRepeating(Plugin plugin, long interval, Runnable runnable) {
    return toggleableTask(runnable, run -> getScheduler().runTaskTimerAsynchronously(plugin, run, 0, interval));
  }
}
