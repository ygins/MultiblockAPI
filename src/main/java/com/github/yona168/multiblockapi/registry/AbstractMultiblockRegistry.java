package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.registry.storage.StateStorer;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.plugin.Plugin;

import java.util.*;

public abstract class AbstractMultiblockRegistry implements MultiblockRegistry {
  private final Map<String, Multiblock<?>> registryMapById = new HashMap<>();
  private final Map<Plugin, Set<Multiblock<?>>> registryMapByPlugin = new HashMap<>();
  private final List<Multiblock<?>> allMultiblocks = new ArrayList<>();
  private final StateStorer stateStorer;

  public AbstractMultiblockRegistry(StateStorer stateStorer){
    this.stateStorer=stateStorer;
  }
  @Override
  public void register(Multiblock<?> multiblock, Plugin plugin) {
    registryMapById.put(multiblock.getId(), multiblock);
    allMultiblocks.add(multiblock);
    Set<Multiblock<?>> byPluginMultiblocks=registryMapByPlugin.getOrDefault(plugin, new HashSet<>());
    byPluginMultiblocks.add(multiblock);
    registryMapByPlugin.put(plugin, byPluginMultiblocks);
    stateStorer.onRegister(multiblock);
  }

  @Override
  public Collection<Multiblock<?>> getForPlugin(Plugin plugin) {
    Set<Multiblock<?>> multiblocks = registryMapByPlugin.get(plugin);
    if (multiblocks == null) {
      return null;
    }
    return multiblocks;
  }

  @Override
  public Multiblock<?> get(String id) {
    return registryMapById.get(id);
  }

  @Override
  public Collection<Multiblock<?>> getAllMultiblocks() {
    return allMultiblocks;
  }

  @Override
  public void disable(){
    stateStorer.storeAllAway();
    stateStorer.waitUntilDone();
    stateStorer.clearHere();
  }
}
