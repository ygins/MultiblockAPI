package com.github.yona168.multiblockapi.pattern;

import org.bukkit.Material;

public class PatternCreator {
  private final Material[][][] pattern;

  public PatternCreator(int height, int depth, int length) {
    this(new Material[height][depth][length]);
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

  public PatternCreator fillRow(int level, int row, Material material) {
    for (int c = 0; c < pattern[0][0].length; c++) {
      set(level, row, c, material);
    }
    return this;
  }

  public PatternCreator fillColumn(int level, int column, Material material) {
    for (int r = 0; r < pattern[0].length; r++) {
      set(level, r, column, material);
    }
    return this;
  }

  public PatternCreator fillLevel(int level, Material material) {
    for (int r = 0; r < pattern[0].length; r++) {
      for (int c = 0; c < pattern[0][0].length; c++) {
        set(level, r, c, material);
      }
    }
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

    public SetYPatternCreator fillLevel(Material material) {
      super.fillLevel(workingLevel, material);
      return this;
    }

    public SetYPatternCreator fillRow(int row, Material material){
      super.fillRow(workingLevel,row,material);
      return this;
    }

    public SetYPatternCreator fillColumn(int column, Material material){
      super.fillColumn(workingLevel, column, material);
      return this;
    }
  }


}
