package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.pattern.PatternCreator;
import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.registry.Registry;
import com.github.yona168.multiblockapi.registry.storage.KryoStateStorer;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.state.SimpleMultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.structure.SimpleMultiblock;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.broadcastMessage;

public class MultiblockAPI extends JavaPlugin {
  private Registry<Multiblock<? extends MultiblockState>> multiblockRegistry;

  @Override
  public void onEnable() {
    multiblockRegistry = new MultiblockRegistry(this, new KryoStateStorer(getDataFolder().toPath().resolve("chunks"), this), (sender, str) -> broadcastMessage(str));
    final Material[][][] pattern = new PatternCreator(2, 3, 2).level(0)
            .set(0, 0, Material.OAK_PLANKS).set(1, 0, Material.BIRCH_PLANKS).set(1, 1, Material.OAK_PLANKS)
            .level(1).set(1, 1, Material.OBSIDIAN).getPattern();
    final ThreeDimensionalArrayCoords triggerCoords = new ThreeDimensionalArrayCoords(1, 1, 1);
    final Multiblock<SimpleMultiblockState> testMultiblock = new SimpleMultiblock<>(pattern, triggerCoords, SimpleMultiblockState::new);
    testMultiblock.onClick((event, state) -> {
      broadcastMessage("Orientation: " + state.getOrientation().name());
    });

    final Material[][][] patternTwo = new PatternCreator(5, 5, 5).level(0).fillLevel(Material.OAK_PLANKS).level(1)
            .fillLevel(Material.OAK_PLANKS).level(2).fillLevel(Material.OAK_PLANKS).level(3).fillLevel(Material.OAK_PLANKS).level(4)
            .set(4, 2, Material.BIRCH_PLANKS).getPattern();
    final ThreeDimensionalArrayCoords triggerCoordsTwo = new ThreeDimensionalArrayCoords(4, 4, 2);
    final Multiblock<SimpleMultiblockState> testMultiblockTwo = new SimpleMultiblock<>(patternTwo, triggerCoordsTwo, SimpleMultiblockState::new);
    multiblockRegistry.register(testMultiblock);
    multiblockRegistry.register(testMultiblockTwo);

    getCommand("chunk").setExecutor(new ChunkUnloader());
  }

  private class ChunkUnloader implements CommandExecutor {
    private Chunk chunk;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
      if (strings.length != 0) {
        if (strings[0].equals("set")) {
          this.chunk = ((Player) commandSender).getLocation().getChunk();
          commandSender.sendMessage("chunk set");
        } else if (strings[0].equals("unload")) {
          boolean success = chunk.unload();
          commandSender.sendMessage(success ? "chunk unloaded" : "chunk could not be unloaded");
        }
      }
      return true;
    }
  }

}
