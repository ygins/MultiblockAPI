package com.github.yona168.multiblockapi.util;

import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class NamespacedKey {
  private final String namespacedKey;
  private final String namespace;
  private final String key;

  public NamespacedKey(Plugin plugin, String key) {
    this(plugin.getName(), key);
  }

  public NamespacedKey(String namespace, String key) {
    this.namespace = namespace;
    this.key = key;
    this.namespacedKey = namespace + "-" + key;
  }

  public String getNamespacedKey() {
    return namespacedKey;
  }

  public String getKey() {
    return key;
  }

  public String getNamespace() {
    return namespace;
  }

  @Override
  public String toString() {
    return getNamespacedKey();
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespacedKey);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof NamespacedKey) {
      NamespacedKey other = (NamespacedKey) o;
      return namespacedKey.equals(other.namespacedKey) &&
              namespace.equals(other.namespace) &&
              key.equals(other.key);
    }
    return false;
  }
}
