package com.github.yona168.multiblockapi.pattern;

import org.bukkit.Material;

public class PatternCreator {
  private final Material[][][] pattern;

  public PatternCreator(int height, int depth, int length) {
    this(new Material[height][depth][length]);
    for (int y = 0; y < pattern.length; y++) {
      for (int r = 0; r < pattern[0].length; r++) {
        for (int c = 0; c < pattern[0][0].length; c++) {
          pattern[y][r][c] = Material.AIR;
        }
      }
    }
  }

  private PatternCreator(Material[][][] arr) {
    this.pattern = arr;
  }

  public static PatternCreator wrap(Material[][][] arr) {
    return new PatternCreator(arr);
  }

  public PatternCreator set(int level, int row, int column, Material material) {
    this.pattern[level][row][column] = material;
    return this;
  }

  public SetYPatternCreator level(int y) {
    if (y < 0) {
      throw new IllegalArgumentException("You cannot set the working level to a negative value! you entered: " + y);
    }
    return new SetYPatternCreator(pattern, y);
  }

  public Material[][][] getPattern() {
    return pattern;
  }

  public static class SetYPatternCreator extends PatternCreator {
    private final int workingLevel;

    private SetYPatternCreator(Material[][][] arr, int workingLevel) {
      super(arr);
      this.workingLevel = workingLevel;
    }

    public SetYPatternCreator set(int row, int column, Material material) {
      super.set(workingLevel, row, column, material);
      return this;
    }
  }


}
