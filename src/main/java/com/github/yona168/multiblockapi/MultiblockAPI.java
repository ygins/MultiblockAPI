package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.registry.SimpleMultiblockRegistry;
import com.github.yona168.multiblockapi.storage.DataTunnelRegistry;
import com.github.yona168.multiblockapi.storage.SimpleDataTunnelRegistry;
import com.github.yona168.multiblockapi.storage.SimpleStateCache;
import com.github.yona168.multiblockapi.storage.StateCache;
import com.gitlab.avelyn.core.components.ComponentPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;


public class MultiblockAPI extends ComponentPlugin implements API {
  private final MultiblockRegistry multiblockRegistry;
  private final DataTunnelRegistry dataTunnelRegistry;
  private static API api;

  public MultiblockAPI() {
    multiblockRegistry = new SimpleMultiblockRegistry();
    final StateCache stateCache = new SimpleStateCache();
    dataTunnelRegistry = new SimpleDataTunnelRegistry();
    final MultiblockAPI ref = this;
    onEnable(() -> api = new API() {
      @Override
      public DataTunnelRegistry getDataTunnelRegistry() {
        return ref.getDataTunnelRegistry();
      }

      @Override
      public MultiblockRegistry getMultiblockRegistry() {
        return ref.getMultiblockRegistry();
      }
    });
    onDisable(() -> api = null);

    addChild(new StateLoaderListeners(stateCache, multiblockRegistry, dataTunnelRegistry, this, null));
    onEnable(()->{
      getCommand("mbapi").setExecutor(new CommandExecutor() {
        private final ChatColor MAIN_COLOR=ChatColor.GRAY;
        private final ChatColor SECONDARY_COLOR=ChatColor.LIGHT_PURPLE;
        private final Supplier<String> tag=()->SECONDARY_COLOR+"["+MAIN_COLOR+"MBAPI"+SECONDARY_COLOR+"]";
        private final Function<String, String> msg=(msg)->tag.get()+ChatColor.RESET+" "+msg;
        private final Function<String, String> err=(msg)->tag.get()+ChatColor.RED+" "+msg;
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
          if(sender.hasPermission("mbapi.backup")){
            sender.sendMessage(msg.apply("Backup starting..."));
            stateCache.backup().thenAccept($->sender.sendMessage(msg.apply("Backup complete!")));
          }else{
            sender.sendMessage(err.apply("You do not have permission to use this command!"));
          }
          return true;
        }
      });
    });

  }

  @Override
  public DataTunnelRegistry getDataTunnelRegistry() {
    return dataTunnelRegistry;
  }

  @Override
  public MultiblockRegistry getMultiblockRegistry() {
    return multiblockRegistry;
  }

  public static API getAPI() {
    return api;
  }
}
