package com.github.yona168.multiblockapi.state;

public interface Tickable extends Runnable {
  int getPeriod();
  boolean isAsync();
}
