package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.pattern.PatternCreator;
import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.registry.SimpleMultiblockRegistry;
import com.github.yona168.multiblockapi.registry.storage.KryoStateStorer;
import com.github.yona168.multiblockapi.registry.storage.kryo.Kryogenic;
import com.github.yona168.multiblockapi.state.IntState;
import com.github.yona168.multiblockapi.state.SimpleMultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.structure.SimpleMultiblock;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import com.gitlab.avelyn.core.components.ComponentPlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
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
  private MultiblockRegistry multiblockRegistry;

  public MultiblockAPI() {
    multiblockRegistry = new SimpleMultiblockRegistry(new KryoStateStorer(getDataFolder().toPath().resolve("chunks"), this), (sender, str) -> broadcastMessage(str));
    addChild(multiblockRegistry);
    onEnable(() -> {
      Kryogenic.init(multiblockRegistry);
      final Material[][][] pattern = new PatternCreator(2, 3, 2).level(0)
              .set(0, 0, Material.OAK_PLANKS).set(1, 0, Material.BIRCH_PLANKS).set(1, 1, Material.OAK_PLANKS)
              .level(1).set(1, 1, Material.OBSIDIAN).getPattern();
      final ThreeDimensionalArrayCoords triggerCoords = new ThreeDimensionalArrayCoords(1, 1, 1);
      final Multiblock<IntState> testMultiblock = new SimpleMultiblock<>("one", pattern, triggerCoords, IntState::new);
      testMultiblock.onClick((event, state) -> {
        state.toggle();
        broadcastMessage("State toggled to " + state.getInt());
      });

      final Material[][][] patternTwo = new PatternCreator(5, 5, 5).level(0).fillLevel(Material.OAK_PLANKS).level(1)
              .fillLevel(Material.OAK_PLANKS).level(2).fillLevel(Material.OAK_PLANKS).level(3).fillLevel(Material.OAK_PLANKS).level(4)
              .set(4, 2, Material.BIRCH_PLANKS).getPattern();
      final ThreeDimensionalArrayCoords triggerCoordsTwo = new ThreeDimensionalArrayCoords(4, 4, 2);
      final Multiblock<SimpleMultiblockState> testMultiblockTwo = new SimpleMultiblock<>("two", patternTwo, triggerCoordsTwo, SimpleMultiblockState::new);
      multiblockRegistry.register(testMultiblock, this);
      multiblockRegistry.register(testMultiblockTwo, this);

      getCommand("chunk").setExecutor(new ChunkUnloader());
      getCommand("clearchunks").setExecutor(new ClearChunks());
      getCommand("myhome").setExecutor(new MyHome());
    });


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
