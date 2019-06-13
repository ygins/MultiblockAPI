package com.github.yona168.multiblockapi.registry.storage;

import com.github.yona168.multiblockapi.registry.storage.kryo.Kryogenic;
import com.github.yona168.multiblockapi.state.MultiblockState;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import static java.nio.file.Files.*;
import static java.util.stream.Collectors.toSet;

public class KryoStateStorer extends AbstractCachedStateStorer {
  private final Path dataFolder;

  public KryoStateStorer(Path dataFolder, Plugin plugin) {
    super(plugin);
    this.dataFolder = dataFolder;
    createDirIfNotExists(dataFolder);
  }

  @Override
  public void storeAway(MultiblockState state) {
    removeFromHere(state);
    final Chunk targetChunk = state.getTriggerBlockLoc().getChunk();
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
          e.printStackTrace();
        }
        return null;
      }).filter(Objects::nonNull).collect(toSet());
    } catch (IOException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }

  @Override
  public void removeFromAfar(MultiblockState state) {
    final Path targetFile = getFilePathFor(state.getTriggerBlockLoc().getChunk()).resolve(state.getUniqueid().toString());
    try {
      Files.deleteIfExists(targetFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Path getFilePathFor(Chunk targetChunk) {
    return dataFolder.resolve(targetChunk.getX() + "-" + targetChunk.getZ());
  }

  private void createDirIfNotExists(Path dir) {
    if (!exists(dir)) {
      try {
        createDirectories(dir);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
