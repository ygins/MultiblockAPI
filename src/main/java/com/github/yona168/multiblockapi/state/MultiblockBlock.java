package com.github.yona168.multiblockapi.state;

import org.bukkit.Location;

public interface MultiblockBlock {
  Location getLocation();

  Function getFunction();

  enum Function {
    STRUCTURE,
    TRIGGER
  }
}
