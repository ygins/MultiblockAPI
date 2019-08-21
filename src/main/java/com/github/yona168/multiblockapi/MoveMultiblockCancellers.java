package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.storage.StateCache;
import com.github.yona168.multiblockapi.util.AvelynUtils;
import com.gitlab.avelyn.architecture.base.Component;
import com.gitlab.avelyn.core.base.Events;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

class MoveMultiblockCancellers extends Component {
  private final StateCache stateCache;

  private MoveMultiblockCancellers(StateCache stateCache) {
    this.stateCache = stateCache;
    addChild(cancel(BlockPistonExtendEvent.class)
            .iff(event -> event.getBlocks().stream().anyMatch(block ->
                    isMultiblock(block) || isMultiblock(block.getRelative(event.getDirection()))
            )));
    addChild(cancel(BlockPistonRetractEvent.class).iff(event -> event.getBlocks().stream().anyMatch(block ->
            isMultiblock(block) || isMultiblock(block.getRelative(event.getDirection()))
    )));
    addChild(cancel(EntityChangeBlockEvent.class).iff(event -> event.getEntity() instanceof FallingBlock
            && isMultiblock(event.getBlock())).andThen(event -> {
      FallingBlock fallingBlock = (FallingBlock) event.getBlock();
      if (fallingBlock.getDropItem()) {
        fallingBlock.getWorld().dropItemNaturally(fallingBlock.getLocation(), new ItemStack(fallingBlock.getMaterial()));
      }
    }));
  }

  private static <T extends Event & Cancellable> Cancel<T> cancel(Class<T> inClazz) {
    return new Cancel<>(inClazz);
  }

  private static class Cancel<T extends Event & Cancellable> extends Component {
    private final Class<T> clazz;
    private final Set<Consumer<T>> andThens;

    private Cancel(Class<T> clazz) {
      this.clazz = clazz;
      andThens = new HashSet<>();
    }

    private Cancel<T> iff(Predicate<T> test) {
      addChild(AvelynUtils.listen(clazz, event -> {
        if (test.test(event)) {
          event.setCancelled(true);
          andThens.forEach(handler -> handler.accept(event));
        }
      }));
      return this;
    }

    private Cancel<T> andThen(Consumer<T> handler) {
      andThens.add(handler);
      return this;
    }
  }

  private boolean isMultiblock(Location location) {
    return stateCache.getAt(location) != null;
  }

  private boolean isMultiblock(Block block) {
    return isMultiblock(block.getLocation());
  }

  static MoveMultiblockCancellers cancelAttemptsToMoveMultiblocks(StateCache stateCache) {
    return new MoveMultiblockCancellers(stateCache);
  }
}
