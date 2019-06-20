package com.github.yona168.multiblockapi.util;

public class Stacktrace {
  public static void printStackTrace(){
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    for (int i = 1; i < elements.length; i++) {
      StackTraceElement s = elements[i];
      System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
              + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
    }
  }
}
