package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.pattern.Pattern;
import com.github.yona168.multiblockapi.pattern.PatternCreator;
import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.registry.SimpleMultiblockRegistry;
import com.github.yona168.multiblockapi.storage.*;
import com.github.yona168.multiblockapi.storage.kryo.Kryogenic;
import com.github.yona168.multiblockapi.state.IntState;
import com.github.yona168.multiblockapi.state.SimpleMultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.structure.SimpleMultiblock;
import com.github.yona168.multiblockapi.structure.StateCreator;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import com.gitlab.avelyn.core.components.ComponentPlugin;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.bukkit.Bukkit.broadcastMessage;

//Legit everything in here is garbage test as of now
public class MultiblockAPI extends ComponentPlugin {
  public static int garbageChunkX;
  public static int garbageChunkZ;
  private Location tempLoc;
  private final MultiblockRegistry multiblockRegistry;
  private final DataTunnelRegistry dataTunnelRegistry;
  private final StateCache stateCache;

  public MultiblockAPI() {
    multiblockRegistry = new SimpleMultiblockRegistry();
    stateCache = new SimpleStateCache();
    dataTunnelRegistry = new SimpleDataTunnelRegistry();
    addChild(new StateLoaderListeners(stateCache, multiblockRegistry, dataTunnelRegistry, this, ($, str) -> broadcastMessage(str)));
    onEnable(() -> {
      final Pattern pattern = new PatternCreator(2, 3, 2).level(0)
              .set(0, 0, Material.OAK_PLANKS).set(1, 0, Material.BIRCH_PLANKS).set(1, 1, Material.OAK_PLANKS)
              .level(1).set(1, 1, Material.OBSIDIAN).triggerCoords(1,1,1);
      final NamespacedKey multiblockId = new NamespacedKey(this, "testOne");
      StateCreator<IntState> intStateStateCreator=(multiblock,locInfo,event)->new IntState(multiblock,locInfo);
      final Multiblock<IntState> testMultiblock = new SimpleMultiblock<>(pattern, multiblockId, StateDataTunnels.kryo(), intStateStateCreator);
      testMultiblock.onClick((event, state) -> {
        state.toggle();
        broadcastMessage("State toggled to " + state.getInt());
      });
      testMultiblock.preStateGenCheck((event, multiblock)->
        event.getPlayer().getGameMode()== GameMode.CREATIVE
      );

      testMultiblock.postStateGenCheck((event, state)->
        state.getClass()==IntState.class && event.getPlayer().getInventory().getItemInMainHand().getType()==Material.DIAMOND_HOE
      );

      final Pattern patternTwo = new PatternCreator(5, 5, 5).level(0).fillLevel(Material.OAK_PLANKS).level(1)
              .fillLevel(Material.OAK_PLANKS).level(2).fillLevel(Material.OAK_PLANKS).level(3).fillLevel(Material.OAK_PLANKS).level(4)
              .set(4, 2, Material.BIRCH_PLANKS).triggerCoords(4,4,2);
      final NamespacedKey namespacedKey = new NamespacedKey(this, "testTwo");
      StateCreator<SimpleMultiblockState> creator=(multiblock,locInfo,event)->new SimpleMultiblockState(multiblock,locInfo);
      final Multiblock<SimpleMultiblockState> testMultiblockTwo = new SimpleMultiblock<>(patternTwo,namespacedKey, StateDataTunnels.kryo(), creator);
      testMultiblockTwo.onClick((event, state)->broadcastMessage("CLICKED"));
      multiblockRegistry.register(testMultiblock, this);
      multiblockRegistry.register(testMultiblockTwo, this);

      getCommand("chunk").setExecutor(new ChunkUnloader());
      getCommand("clearchunks").setExecutor(new ClearChunks());
      getCommand("myhome").setExecutor(new MyHome());
    });

    onDisable(()->getLogger().info("DISABLING"));


  }
  private class ChunkUnloader implements CommandExecutor {
    private Chunk chunk;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
      if (strings.length != 0) {
        if (strings[0].equals("set")) {
          Location loc = ((Player) commandSender).getLocation();
          this.chunk = loc.getChunk();
          tempLoc = loc;
          garbageChunkX = chunk.getX();
          garbageChunkZ = chunk.getZ();
          commandSender.sendMessage("chunk set");
        } else if (strings[0].equals("unload")) {
          boolean success = chunk.unload();
          commandSender.sendMessage(success ? "chunk unloaded" : "chunk could not be unloaded");
        }
      }
      return true;
    }
  }

  private class ClearChunks implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
      Path chunks = getDataFolder().toPath().resolve("chunks");
      try {
        Files.walk(chunks).filter(Files::isRegularFile).forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
      return true;
    }
  }

  private class MyHome implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
      ((Player) commandSender).teleport(tempLoc);
      return true;
    }
  }

}
