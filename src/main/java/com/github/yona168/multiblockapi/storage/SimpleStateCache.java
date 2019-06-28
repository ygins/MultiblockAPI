package com.github.yona168.multiblockapi.storage;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimpleStateCache implements StateCache {

  private final Map<Location, MultiblockState> stateByLocation;
  private final Multimap<ChunkCoords, MultiblockState> stateByChunk;
  private final Multimap<World, MultiblockState> stateByWorld;

  public SimpleStateCache() {
    stateByWorld = HashMultimap.create();
    stateByChunk = HashMultimap.create();
    stateByLocation = new HashMap<>();
  }

  @Override
  public void store(MultiblockState state) {
    state.getAllBlocksLocs().stream().map(SimpleStateCache::normalize).forEach(loc -> stateByLocation.put(loc, state));
    state.getOccupiedChunks().forEach(loc -> stateByChunk.put(loc, state));
    stateByWorld.put(state.getWorld(), state);
  }

  @Override
  public MultiblockState getAt(Location location) {
    return stateByLocation.get(location);
  }

  @Override
  public Collection<MultiblockState> getAt(Chunk chunk) {
    return stateByChunk.get(ChunkCoords.fromChunk(chunk));
  }

  @Override
  public Collection<MultiblockState> getAt(World world) {
    return stateByWorld.get(world);
  }

  @Override
  public Collection<MultiblockState> getAll() {
    return stateByWorld.values();
  }

  @Override
  public void remove(MultiblockState state) {
    state.getAllBlocksLocs().forEach(stateByLocation::remove);
    state.getOccupiedChunks().forEach(chunk -> stateByChunk.remove(chunk, state));
    stateByWorld.remove(state.getWorld(), state);
  }

  @Override
  public void clear() {
    stateByChunk.clear();
    stateByLocation.clear();
    stateByWorld.clear();
  }

  private static Location normalize(Location loc) {
    loc.setYaw(0);
    loc.setPitch(0);
    return loc;
  }
}
