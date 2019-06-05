package com.github.yona168.multiblockapi;

import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.registry.Registry;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiblockAPI extends JavaPlugin {
  private final Registry<Multiblock<? extends MultiblockState>> multiblockRegistry = new MultiblockRegistry(this);

  @Override
  public void onEnable() {
    //TEST MULTIBLOCK
    /*
    final Material[][][] pattern=new PatternCreator(2,3,2).level(0)
            .set(0,0,Material.OAK_PLANKS).set(1,0, Material.BIRCH_PLANKS).set(1,1,Material.OAK_PLANKS)
           .level(1).set(1,1, Material.OBSIDIAN).getPattern();
    final ThreeDimensionalArrayCoords triggerCoords=new ThreeDimensionalArrayCoords(1,1,1);
    final Multiblock testMultiblock=new SimpleMultiblock(pattern, triggerCoords);
    testMultiblock.doThisOnClick(event->{
      broadcastMessage("works!");
      broadcastMessage(System.currentTimeMillis()-start+"ms");
    });
    registerMultiblock(testMultiblock);

  }

  public void registerMultiblock(Multiblock multiblock){

    this.multiBlocks.add(multiblock);
    */

  }

}
