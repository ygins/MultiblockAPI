package com.github.yona168.multiblockapi.structure;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.state.SimpleMultiblockState;
import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.Optional.empty;

public class SimpleMultiblock<T extends MultiblockState> implements Multiblock<T> {
  private final Material trigger;
  private final ThreeDimensionalArrayCoords triggerCoords;
  private final Material[][][] pattern;
  private final Set<BiConsumer<PlayerInteractEvent, T>> eventConsumers;
  private final BiFunction<SimpleMultiblock<T>, LocationInfo, T> stateCreator;

  public SimpleMultiblock(Material[][][] pattern, ThreeDimensionalArrayCoords coords, BiFunction<SimpleMultiblock<T>, LocationInfo, T> stateCreator) {
    this.eventConsumers = new HashSet<>();
    this.pattern = pattern;
    this.triggerCoords = coords;
    this.trigger = ThreeDimensionalArrayCoords.get(pattern, coords);
    this.stateCreator = stateCreator;
  }

  @Override
  public Optional<T> generateStateFrom(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null || event.getClickedBlock().getType() != trigger) {
      return empty();
    } else {
      final Material[][][] pattern = getPattern();
      final Location clickedLoc = event.getClickedBlock().getLocation();
      final ThreeDimensionalArrayCoords triggerCoords = this.triggerCoords;
      final Block facingSouthBottomLeft = clickedLoc.clone().add(triggerCoords.getColumn(), -triggerCoords.getY(), triggerCoords.getRow()).getBlock();
      boolean southValid = true;
      boolean northValid = true;
      boolean westValid = true;
      boolean eastValid = true;
      south:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            if (pattern[y][r][c] != facingSouthBottomLeft.getRelative(-c, y, -r).getType()) {
              southValid = false;
              break south;
            }
          }
        }
      }
      if (southValid)
        return Optional.of(stateCreator.apply(this, new LocationInfo(facingSouthBottomLeft, SimpleMultiblockState.Orientation.SOUTH)));
      final Block facingNorthBottomLeft = clickedLoc.clone().add(-triggerCoords.getColumn(), -triggerCoords.getY(), -triggerCoords.getRow()).getBlock();
      north:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            if (pattern[y][r][c] != facingNorthBottomLeft.getRelative(c, y, r).getType()) {
              northValid = false;
              break north;
            }
          }
        }
      }
      if (northValid)
        return Optional.of(stateCreator.apply(this, new LocationInfo(facingSouthBottomLeft, SimpleMultiblockState.Orientation.NORTH)));

      final Block facingEastBottomLeft = clickedLoc.clone().add(triggerCoords.getRow(), -triggerCoords.getY(), -triggerCoords.getColumn()).getBlock();
      east:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            if (pattern[y][r][c] != facingEastBottomLeft.getRelative(-r, y, c).getType()) {
              eastValid = false;
              break east;
            }
          }
        }
      }
      if (eastValid)
        return Optional.of(stateCreator.apply(this, new LocationInfo(facingEastBottomLeft, SimpleMultiblockState.Orientation.EAST)));

      final Block facingWestBottomLeft = clickedLoc.clone().add(-triggerCoords.getRow(), -triggerCoords.getY(), triggerCoords.getColumn()).getBlock();
      west:
      for (int y = 0; y < pattern.length; y++) {
        for (int r = 0; r < pattern[0].length; r++) {
          for (int c = 0; c < pattern[0][0].length; c++) {
            if (pattern[y][r][c] != facingWestBottomLeft.getRelative(r, y, -c).getType()) {
              westValid = false;
              break west;
            }
          }
        }
      }
      if (westValid)
        return Optional.of(stateCreator.apply(this, new LocationInfo(facingWestBottomLeft, SimpleMultiblockState.Orientation.WEST)));
    }
    return empty();
  }

  @Override
  public void onClick(BiConsumer<PlayerInteractEvent, T> eventConsumer) {
    this.eventConsumers.add(eventConsumer);
  }

  @Override
  public void doClickActions(PlayerInteractEvent event, T multiblockState) {
    event.setCancelled(true);
    this.eventConsumers.forEach(cons -> cons.accept(event, multiblockState));
  }

  @Override
  public Material[][][] getPattern() {
    return pattern;
  }

  @Override
  public ThreeDimensionalArrayCoords getTriggerCoords() {
    return this.triggerCoords;
  }
}
