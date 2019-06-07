package com.github.yona168.multiblockapi.registry;

import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

import static org.bukkit.Bukkit.getServer;

public class MultiblockRegistry implements Registry<Multiblock<? extends MultiblockState>> {
  private final Set<Multiblock<? extends MultiblockState>> multiblocks = new HashSet<>();
  private final Map<Location, MultiblockState> multiblockBlockState = new HashMap<>();
  private final Plugin plugin;

  public MultiblockRegistry(Plugin plugin) {
    this.plugin = plugin;
    listen(PlayerInteractEvent.class, this::handleInteract);
    listen(BlockBreakEvent.class, this::handleBlockBreak);
  }

  private void handleInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    final Location clickedLoc = event.getClickedBlock().getLocation();
    MultiblockState existingState = multiblockBlockState.get(clickedLoc);
    if (existingState == null) {
      multiblocks.stream().map(multiblock -> multiblock.generateStateFrom(event)).filter(Optional::isPresent).map(Optional::get).findFirst().ifPresent(multiblockState -> {
        multiblockState.getAllBlocksLocs().forEach(loc -> multiblockBlockState.put(loc, multiblockState));
        multiblockState.getMultiblock().doClickActions(event, multiblockState);
      });
    } else {
      existingState.getMultiblock().doClickActions(event, existingState);
    }
  }

  private void handleBlockBreak(BlockBreakEvent event) {
    final Location broken = event.getBlock().getLocation();
    final MultiblockState brokenState = multiblockBlockState.get(broken);
    if (brokenState != null) {
      brokenState.getAllBlocksLocs().forEach(multiblockBlockState::remove);
    }
  }

  @Override
  public void register(Multiblock<? extends MultiblockState> item) {
    this.multiblocks.add(item);
  }

  @SuppressWarnings("unchecked cast")
  private <T extends Event> void listen(Class<T> clazz, Consumer<T> handler) {
    getServer().getPluginManager().registerEvent(clazz, new Listener() {
    }, EventPriority.NORMAL, (listener, event) -> handler.accept((T) event), this.plugin);
  }
}
