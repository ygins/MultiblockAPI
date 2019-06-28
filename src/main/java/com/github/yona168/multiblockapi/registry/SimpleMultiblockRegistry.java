package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.structure.Multiblock;
import com.gitlab.avelyn.architecture.base.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class SimpleMultiblockRegistry extends Component implements MultiblockRegistry {
  private final Map<NamespacedKey, Multiblock<?>> registryMapById;
  private final Map<Plugin, Set<Multiblock<?>>> registryMapByPlugin;
  private final List<Multiblock<?>> allMultiblocks;

  public SimpleMultiblockRegistry(){
     registryMapById= new HashMap<>();
     registryMapByPlugin=new HashMap<>();
     allMultiblocks=new ArrayList<>();
    onDisable(()->{
      registryMapById.clear();
      registryMapByPlugin.clear();
      allMultiblocks.clear();
    });
  }

  @Override
  public void register(Multiblock<?> multiblock,Plugin plugin) {
    registryMapById.put(multiblock.getId(), multiblock);
    allMultiblocks.add(multiblock);
    Set<Multiblock<?>> byPluginMultiblocks = registryMapByPlugin.getOrDefault(plugin, new HashSet<>());
    byPluginMultiblocks.add(multiblock);
    registryMapByPlugin.put(plugin, byPluginMultiblocks);
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
  public Multiblock<?> get(NamespacedKey id) {
    return registryMapById.get(id);
  }

  @Override
  public Collection<Multiblock<?>> getAllMultiblocks() {
    return allMultiblocks;
  }

}
