package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.util.NamespacedKey;
import com.gitlab.avelyn.architecture.base.Component;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class SimpleMultiblockRegistry extends Component implements MultiblockRegistry {
  private final Map<NamespacedKey, Multiblock<?>> registryMapById;
  private final Multimap<Plugin, Multiblock<?>> registryMapByPlugin;
  private final List<Multiblock<?>> allMultiblocks;

  public SimpleMultiblockRegistry() {
    registryMapById = new HashMap<>();
    registryMapByPlugin = HashMultimap.create();
    allMultiblocks = new ArrayList<>();
    onDisable(() -> {
      registryMapById.clear();
      registryMapByPlugin.clear();
      allMultiblocks.clear();
    });
  }

  @Override
  public void register(Multiblock<?> multiblock, Plugin plugin) {
    registryMapById.put(multiblock.getId(), multiblock);
    allMultiblocks.add(multiblock);
    registryMapByPlugin.put(plugin, multiblock);
  }

  @Override
  public Collection<Multiblock<?>> getForPlugin(Plugin plugin) {
    Collection<Multiblock<?>> multiblocks = registryMapByPlugin.get(plugin);
    if (multiblocks == null) {
      return null;
    }
    return multiblocks;
  }

  @Override
  public Multiblock<?> get(NamespacedKey id) {
    return registryMapById.get(id);
  }

  @Override
  public Collection<Multiblock<?>> getAllMultiblocks() {
    return allMultiblocks;
  }

}
