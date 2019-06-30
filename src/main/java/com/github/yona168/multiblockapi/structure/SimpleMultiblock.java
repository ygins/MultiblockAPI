package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.pattern.Pattern;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.state.SimpleMultiblockState;
import com.github.yona168.multiblockapi.storage.StateDataTunnel;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static java.util.Optional.empty;

public class SimpleMultiblock<T extends MultiblockState> implements Multiblock<T> {
  private final Material trigger;
  private final Pattern pattern;
  private final Set<BiConsumer<PlayerInteractEvent, T>> eventConsumers;
  private final StateCreator<T> stateCreator;
  private final NamespacedKey id;
  private final StateDataTunnel dataTunnel;
  private final Set<BiPredicate<PlayerInteractEvent, Multiblock<T>>> preStateGenChecks;
  private final Set<BiPredicate<PlayerInteractEvent, T>> postStateGenChecks;


  public SimpleMultiblock(Pattern pattern, NamespacedKey id, StateDataTunnel dataTunnel, StateCreator<T> stateCreator) {
    this.eventConsumers = new HashSet<>();
    this.pattern = pattern;
    this.trigger = ThreeDimensionalArrayCoords.get(pattern, pattern.getTriggerCoords());
    this.stateCreator = stateCreator;
    this.id = id;
    this.dataTunnel = dataTunnel;
    this.preStateGenChecks = new HashSet<>();
    this.postStateGenChecks = new HashSet<>();
  }

  @Override
  public Optional<T> generateStateFrom(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null || event.getClickedBlock().getType() != trigger) {
      return empty();
    } else {
      final Material[][][] pattern = getPattern().asArray();
      final Location clickedLoc = event.getClickedBlock().getLocation();
      final ThreeDimensionalArrayCoords triggerCoords = this.pattern.getTriggerCoords();
      final Block facingSouthBottomLeft = clickedLoc.clone().add(triggerCoords.getColumn(), -triggerCoords.getY(), triggerCoords.getRow()).getBlock();
      boolean southValid = true;
      boolean northValid = true;
      boolean westValid = true;
      boolean eastValid = true;
      Set<Location> allBlockLocations = new HashSet<>();
      south:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            final Material targetMaterial = pattern[y][r][c];
            if (targetMaterial != null) {
              final Block relativeBlock = facingSouthBottomLeft.getRelative(-c, y, -r);
              if (targetMaterial != relativeBlock.getType()) {
                southValid = false;
                allBlockLocations.clear();
                break south;
              }
              allBlockLocations.add(relativeBlock.getLocation());
            }
          }
        }
      }
      if (southValid) {
        if(!checkPreState(event)){
          return Optional.empty();
        }
        T state = stateCreator.createFrom(this, new LocationInfo(facingSouthBottomLeft, allBlockLocations, SimpleMultiblockState.Orientation.SOUTH), event);
        if (checkPostState(event, state)) {
          return Optional.of(state);
        } else {
          return Optional.empty();
        }
      }
      final Block facingNorthBottomLeft = clickedLoc.clone().add(-triggerCoords.getColumn(), -triggerCoords.getY(), -triggerCoords.getRow()).getBlock();
      north:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            final Material targetMaterial = pattern[y][r][c];
            if (targetMaterial != null) {
              Block relativeBlock = facingNorthBottomLeft.getRelative(c, y, r);
              if (targetMaterial != relativeBlock.getType()) {
                northValid = false;
                allBlockLocations.clear();
                break north;
              }
              allBlockLocations.add(relativeBlock.getLocation());
            }
          }
        }
      }
      if (northValid) {
        if(!checkPreState(event)){
          return Optional.empty();
        }
        T state = stateCreator.createFrom(this, new LocationInfo(facingNorthBottomLeft, allBlockLocations, SimpleMultiblockState.Orientation.NORTH), event);
        if(checkPostState(event,state)){
          return Optional.of(state);
        }else{
          return Optional.empty();
        }
      }


      final Block facingEastBottomLeft = clickedLoc.clone().add(triggerCoords.getRow(), -triggerCoords.getY(), -triggerCoords.getColumn()).getBlock();
      east:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            final Material targetMaterial = pattern[y][r][c];
            if (targetMaterial != null) {
              Block relativeBlock = facingEastBottomLeft.getRelative(-r, y, c);
              if (targetMaterial != relativeBlock.getType()) {
                eastValid = false;
                allBlockLocations.clear();
                break east;
              }
              allBlockLocations.add(relativeBlock.getLocation());
            }
          }
        }
      }
      if (eastValid){
        if(!checkPreState(event)){
          return Optional.empty();
        }
        T state=stateCreator.createFrom(this, new LocationInfo(facingEastBottomLeft, allBlockLocations, MultiblockState.Orientation.EAST), event);
        if(checkPostState(event,state)){
          return Optional.of(state);
        }else{
          return Optional.empty();
        }
      }


      final Block facingWestBottomLeft = clickedLoc.clone().add(-triggerCoords.getRow(), -triggerCoords.getY(), triggerCoords.getColumn()).getBlock();
      west:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            final Material targetMaterial = pattern[y][r][c];
            if (targetMaterial != null) {
              Block relativeBlock = facingWestBottomLeft.getRelative(r, y, -c);
              if (targetMaterial != relativeBlock.getType()) {
                westValid = false;
                allBlockLocations.clear();
                break west;
              }
              allBlockLocations.add(relativeBlock.getLocation());
            }
          }
        }
      }
      if (westValid){
        if(!checkPreState(event)){
          return Optional.empty();
        }
        T state=stateCreator.createFrom(this, new LocationInfo(facingWestBottomLeft, allBlockLocations, SimpleMultiblockState.Orientation.WEST), event);
        if(checkPostState(event, state)){
          return Optional.of(state);
        }else{
          return Optional.empty();
        }
      }
    }
    return empty();
  }

  @Override
  public void onClick(BiConsumer<PlayerInteractEvent, T> eventConsumer) {
    this.eventConsumers.add(eventConsumer);
  }

  @Override
  public void preStateGenCheck(BiPredicate<PlayerInteractEvent, Multiblock<T>> check) {
    this.preStateGenChecks.add(check);
  }

  @Override
  public void postStateGenCheck(BiPredicate<PlayerInteractEvent, T> check) {
    this.postStateGenChecks.add(check);
  }

  private boolean checkPreState(PlayerInteractEvent event) {
    return this.preStateGenChecks.stream().allMatch(predicate -> predicate.test(event, this));
  }

  private boolean checkPostState(PlayerInteractEvent event, T state) {
    return this.postStateGenChecks.stream().allMatch(predicate -> predicate.test(event, state));
  }

  @Override
  public void doClickActions(PlayerInteractEvent event, T multiblockState) {
    event.setCancelled(true);
    this.eventConsumers.forEach(cons -> cons.accept(event, multiblockState));
  }

  @Override
  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public NamespacedKey getId() {
    return this.id;
  }

  @Override
  public StateDataTunnel getDataTunnel() {
    return dataTunnel;
  }
}
