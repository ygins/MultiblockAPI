package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.storage.kryo.Kryogenic;
import com.gitlab.avelyn.architecture.base.Component;
import com.gitlab.avelyn.architecture.base.Toggleable;
import org.bukkit.plugin.Plugin;

public class StateDataTunnels {
  private static StateDataTunnel kryo;

  public static Toggleable component(Plugin plugin, MultiblockRegistry multiblockRegistry) {
    Component component = new Component()
            .onEnable(() -> kryo = new KryoDataTunnel(plugin.getDataFolder().toPath().resolve("chunks"), plugin))
            .onDisable(() -> kryo = null);
    component.addChild(Kryogenic.toggleable(multiblockRegistry));
    return component;
  }

  public static StateDataTunnel kryo(){
    return kryo;
  }
}
