package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.util.NamespacedKey;
import com.gitlab.avelyn.architecture.base.Toggleable;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public interface MultiblockRegistry extends Toggleable {
  void register(Multiblock<?> item, Plugin plugin);

  Collection<Multiblock<?>> getForPlugin(Plugin plugin);

  Multiblock<?> get(NamespacedKey name);

  Collection<Multiblock<?>> getAllMultiblocks();

}
