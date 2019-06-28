package com.github.yona168.multiblockapi.pattern;


import com.github.yona168.multiblockapi.util.ThreeDimensionalArrayCoords;
import org.bukkit.Material;

public interface Pattern {
  Material[][][] asArray();

  ThreeDimensionalArrayCoords getTriggerCoords();

  static Pattern of(Material[][][] pattern, ThreeDimensionalArrayCoords coords) {
    return new Pattern() {
      @Override
      public Material[][][] asArray() {
        return pattern;
      }

      @Override
      public ThreeDimensionalArrayCoords getTriggerCoords() {
        return coords;
      }
    };
  }
}
