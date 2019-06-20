package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public interface MultiblockRegistry {
  void register(Multiblock<?> item, Plugin pLugin);

  Collection<Multiblock<?>> getForPlugin(Plugin plugin);

  Multiblock<?> get(String name);

  Collection<Multiblock<?>> getAllMultiblocks();

  void disable();
}
