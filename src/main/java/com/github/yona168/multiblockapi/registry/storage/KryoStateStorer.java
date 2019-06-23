package com.github.yona168.multiblockapi.registry.storage;

import com.github.yona168.multiblockapi.registry.storage.kryo.Kryogenic;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

import static java.nio.file.Files.*;
import static java.util.stream.Collectors.toSet;

public class KryoStateStorer extends AbstractLockedCachedStateStorer {
  private final Path dataFolder;

  public KryoStateStorer(Path dataFolder, Plugin plugin) {
    super(plugin);
    this.dataFolder = dataFolder;
    createDirIfNotExists(dataFolder);
  }

  @Override
  public void onRegister(Multiblock multiblock) {
    Kryogenic.KRYO.register(multiblock.getClass());
  }

  @Override
  public void initStoreAway(MultiblockState state) {
    removeFromHere(state);
    final Chunk targetChunk = state.getTriggerChunk();
    final Path targetChunkFolder = getFilePathFor(targetChunk);
    createDirIfNotExists(targetChunkFolder);
    final Path targetFile = targetChunkFolder.resolve(state.getUniqueid().toString());
    try {
      Kryogenic.freeze(targetFile, state);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @Override
  public Collection<MultiblockState> initGetFromAfar(Chunk chunk) {
    try {
      Path targetDir = getFilePathFor(chunk);
      if (notExists(targetDir)) {
        return new HashSet<>();
      }
      return list(targetDir).map(path -> {
        try {
          MultiblockState state = Kryogenic.thaw(path);
          removeFromAfar(state);
          return state;
        } catch (IOException e) {
          throw new RuntimeException("I oofed");
        }
      }).collect(toSet());
    } catch (IOException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }

  @Override
  public void initRemoveFromAfar(MultiblockState state) {
    final Path targetFile = getFilePathFor(state.getTriggerChunk()).resolve(state.getUniqueid().toString());
    try {
      Files.deleteIfExists(targetFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Path getFilePathFor(Chunk chunk) {
    return getFilePathFor(chunk.getX(), chunk.getZ());
  }

  private Path getFilePathFor(int x, int z) {
    return dataFolder.resolve(x + "-" + z);
  }

  private void createDirIfNotExists(Path dir) {
    if (!exists(dir)) {
      final File dirFile = dir.toFile();
      boolean success = dirFile.mkdirs();
      dirFile.setWritable(true);
      if (!success) {
        throw new RuntimeException("Directory with path " + dir.toString() + " could not be created!");
      }
    }
  }


}
