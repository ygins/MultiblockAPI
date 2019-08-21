package com.github.yona168.multiblockapi.util;

import com.gitlab.avelyn.architecture.base.Toggleable;
import com.gitlab.avelyn.core.base.Events;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;

public interface AvelynUtils {
  static <T extends Event> Toggleable listen(Class<T> clazz, Consumer<T> handler){
    return Events.listen(clazz, EventPriority.NORMAL, handler);
  }
}
