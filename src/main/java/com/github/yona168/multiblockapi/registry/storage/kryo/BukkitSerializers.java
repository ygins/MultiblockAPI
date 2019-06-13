package com.github.yona168.multiblockapi.registry.storage.kryo;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static org.bukkit.Bukkit.getServer;

public class BukkitSerializers {
  private static final String
          ERR_NBT_LOAD = "Failed to find a load method in NBTCompressedStreamTools",
          ERR_NBT_SAVE = "Failed to find a save method in NBTCompressedStreamTools",
          ERR_ENTITY_LOAD = "Failed to find a load method in EntityTypes.";

  private static final Constructor<?> CONSTRUCTOR_NBT;
  private static final Method
          METHOD_NBT_SAVE,
          METHOD_NBT_LOAD,
          METHOD_NBT_SET_STRING;

  private static final Class<?> CLASS_ITEM;
  protected static final Class<?> CLASS_CRAFT_ITEM;
  private static final Constructor<?> CONSTRUCTOR_ITEM;
  private static final Method
          METHOD_ITEM_CREATE,
          METHOD_ITEM_TO,
          METHOD_ITEM_FROM,
          METHOD_ITEM_SAVE;

  private static final Method
          METHOD_ENTITY_HANDLE,
          METHOD_ENTITY_GET,
          METHOD_ENTITY_SAVE,
          METHOD_ENTITY_LOAD,
          METHOD_WORLD_HANDLE;


  static {
    try {
      final String version = getServer().getClass().getName().split("\\.")[3];
      final String nms = "net.minecraft.server.%s.%s";
      final String cb = "org.bukkit.craftbukkit.%s.%s";

      final Class<?> nbt = Class(nms, version, "NBTTagCompound");
      CONSTRUCTOR_NBT = nbt.getConstructor();
      CONSTRUCTOR_NBT.setAccessible(true);
      METHOD_NBT_SET_STRING = nbt.getDeclaredMethod("setString", String.class, String.class);
      Method nbtSave = null, nbtLoad = null;
      for (Method method : Class(nms, version, "NBTCompressedStreamTools").getDeclaredMethods()) {
        final Class<?>[] params = method.getParameterTypes();
        if (params.length == 2 && params[1] == DataOutput.class)
          nbtSave = method;
        else if (params.length == 1 && params[0] == DataInputStream.class)
          nbtLoad = method;
      }
      if ((METHOD_NBT_SAVE = nbtSave) == null)
        throw new IllegalStateException(ERR_NBT_SAVE);
      if ((METHOD_NBT_LOAD = nbtLoad) == null)
        throw new IllegalStateException(ERR_NBT_LOAD);
      METHOD_NBT_SAVE.setAccessible(true);
      METHOD_NBT_LOAD.setAccessible(true);

      CLASS_ITEM = Class(nms, version, "ItemStack");
      CLASS_CRAFT_ITEM = Class(cb, version, "inventory.CraftItemStack");
      METHOD_ITEM_FROM = CLASS_CRAFT_ITEM.getDeclaredMethod("asBukkitCopy", CLASS_ITEM);
      METHOD_ITEM_TO = CLASS_CRAFT_ITEM.getDeclaredMethod("asNMSCopy", ItemStack.class);
      Method methodItemCreate;
      Constructor<?> itemConstructor;
      try {
        itemConstructor = CLASS_ITEM.getConstructor(nbt);
        itemConstructor.setAccessible(true);
        methodItemCreate = null;
      } catch (Throwable ignored) {
        itemConstructor = null;
        methodItemCreate = CLASS_ITEM.getDeclaredMethod("createStack", nbt);
        methodItemCreate.setAccessible(true);
      }
      CONSTRUCTOR_ITEM = itemConstructor;
      METHOD_ITEM_CREATE = methodItemCreate;
      METHOD_ITEM_SAVE = CLASS_ITEM.getDeclaredMethod("save", nbt);
      METHOD_ITEM_SAVE.setAccessible(true);

      final Class<?> entity = Class(nms, version, "Entity");
      final Class<?> craftEntity = Class(cb, version, "entity.CraftEntity");
      final Class<?> craftServer = Class(cb, version, "CraftServer");
      METHOD_ENTITY_HANDLE = craftEntity.getDeclaredMethod("getHandle");
      METHOD_ENTITY_GET = craftEntity.getDeclaredMethod("getEntity", craftServer, entity);
      METHOD_ENTITY_SAVE = entity.getDeclaredMethod("save", nbt);
      final Class<?> entityTypes = Class(nms, version, "EntityTypes");
      Method entityLoad = null;
      for (Method method : entityTypes.getDeclaredMethods()) {
        final Class<?>[] params = method.getParameterTypes();
        if (params.length == 2 && params[0] == entity)
          entityLoad = method;
      }
      if ((METHOD_ENTITY_LOAD = entityLoad) == null)
        throw new IllegalStateException(ERR_ENTITY_LOAD);

      final Class<?> craftWorld = Class(cb, version, "CraftWorld");
      METHOD_WORLD_HANDLE = craftWorld.getDeclaredMethod("getHandle");
    } catch (Exception e) {
      throw new IllegalStateException("Could not initialize reflection!", e);
    }
  }

  private static Class<?> Class(String format, String version, String name) throws ClassNotFoundException {
    return forName(format(format, version, name));
  }

  public static Object createNBTTagCompound() throws Exception {
    return CONSTRUCTOR_NBT.newInstance();
  }

  public static void saveNBT(OutputStream out, Object nbt) throws Exception {
    METHOD_NBT_SAVE.invoke(null, nbt, new DataOutputStream(out));
  }

  public static Object loadNBT(InputStream in) throws Exception {
    return METHOD_NBT_LOAD.invoke(null, new DataInputStream(in));
  }

  public static void setString(Object nbt, String key, String value) throws Exception {
    METHOD_NBT_SET_STRING.invoke(nbt, key, value);
  }

  public static Object getHandle(Entity entity) throws Exception {
    return METHOD_ENTITY_HANDLE.invoke(entity);
  }

  public static Object getHandle(World world) throws Exception {
    return METHOD_WORLD_HANDLE.invoke(world);
  }

  public static Object itemFromBukkit(ItemStack item) throws Exception {
    return METHOD_ITEM_TO.invoke(null, item);
  }

  public static ItemStack itemToBukkit(Object item) throws Exception {
    return (ItemStack) METHOD_ITEM_FROM.invoke(null, item);
  }

  public static void saveItems(ItemStack[] contents, OutputStream out) throws Exception {
    final ByteBuffer length = ByteBuffer.allocate(4);
    length.putInt(contents.length);
    out.write(length.array());
    for (ItemStack item : contents)
      saveItem(item, out);
  }

  public static ItemStack[] loadItems(InputStream in) throws Exception {
    final ByteBuffer length = ByteBuffer.allocate(4);
    in.read(length.array());
    final ItemStack[] contents = new ItemStack[length.getInt()];
    for (int i = 0; i < contents.length; i++)
      contents[i] = loadItem(in);
    return contents;
  }

  public static void saveEntity(Entity entity, OutputStream out) throws Exception {
    final String id = entity.getType().getName();
    if (entity.isDead() || id == null)
      throw new IllegalStateException("Cannot save dead entity.");
    final Object nbt = createNBTTagCompound();
    setString(nbt, "id", id);
    METHOD_ENTITY_SAVE.invoke(getHandle(entity), nbt);
    saveNBT(out, nbt);
  }

  public static Entity loadEntity(World world, InputStream in) throws Exception {
    final Object entity = METHOD_ENTITY_LOAD.invoke(null, loadNBT(in), getHandle(world));
    if (entity == null)
      return null;
    return (Entity) METHOD_ENTITY_GET.invoke(null, getServer(), entity);
  }

  public static void saveItem(ItemStack item, OutputStream out) throws Exception {
    saveNBT(out, METHOD_ITEM_SAVE.invoke(itemFromBukkit(item), createNBTTagCompound()));
  }

  public static ItemStack loadItem(InputStream in) throws Exception {
    return itemToBukkit(CONSTRUCTOR_ITEM == null ?
            METHOD_ITEM_CREATE.invoke(null, loadNBT(in)) :
            CONSTRUCTOR_ITEM.newInstance(loadNBT(in))
    );
  }
}