package com.github.yona168.multiblockapi.registry.storage.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yona168.multiblockapi.MultiblockAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.Class;
import java.util.Map;
import java.util.UUID;

import static com.github.yona168.multiblockapi.registry.storage.kryo.BukkitSerializers.*;
import static java.util.Arrays.stream;
import static org.bukkit.Bukkit.getWorld;
import static org.bukkit.entity.EntityType.values;

public class BukkitKryogenics {
  private static final String ERR_SAVE_ENTITY = "Failed to serialize NBT for an Entity.";
  private static final String ERR_SAVE_ITEM = "Failed to serialize NBT for an ItemStack.";
  private static final String ERR_LOAD_ITEM = "Failed to deserialize NBT for an ItemStack.";
  private static final String ERR_LOAD_ENTITY = "Failed to deserialize NBT for an Entity.";

  public static void registerSerializers(Kryo kryo) {
    stream(values()).map(EntityType::getEntityClass).forEach(type -> {
      if (type == null) {
        return;
      }
      kryo.register(type, new Serializer<Entity>() {
        @Override
        public void write(Kryo kryo, Output out, Entity entity) {
          try {
            saveEntity(entity, out);
          } catch (Exception reason) {
            throw new IllegalStateException(ERR_SAVE_ENTITY, reason);
          }
        }

        @Override
        public Entity read(Kryo kryo, Input in, Class<? extends Entity> type) {
          try {
            return loadEntity(Bukkit.getWorlds().get(0), in);
          } catch (Exception e) {
            throw new IllegalStateException(ERR_LOAD_ENTITY, e);
          }
        }
      });
    });

    new Serializer<ItemStack>() {
      {
        kryo.register(ItemStack.class, this);
        kryo.register(CLASS_CRAFT_ITEM, this);
      }

      @Override
      public void write(Kryo kryo, Output out, ItemStack item) {
        try {
          saveItem(item, out);
        } catch (Exception e) {
          throw new IllegalStateException(ERR_SAVE_ITEM, e);
        }
      }

      @Override
      public ItemStack read(Kryo kryo, Input in, Class<? extends ItemStack> type) {
        try {
          return BukkitSerializers.loadItem(in);
        } catch (Exception e) {
          throw new IllegalStateException(ERR_LOAD_ITEM, e);
        }
      }
    };

    Serializer<UUID> uuidSerializer = new Serializer<UUID>() {
      @Override
      public void write(Kryo kryo, Output out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
      }

      @Override
      public UUID read(Kryo kryo, Input in, Class<? extends UUID> type) {
        return new UUID(in.readLong(), in.readLong());
      }
    };
    kryo.register(UUID.class, uuidSerializer);

    kryo.register(Location.class, new Serializer<Location>() {
      @Override
      public void write(Kryo kryo, Output output, Location location) {
        kryo.writeClassAndObject(output, location.serialize());
      }

      @Override
      public Location read(Kryo kryo, Input input, Class type) {
        return Location.deserialize((Map<String,Object>)kryo.readClassAndObject(input));
      }
    });

    final Serializer<Block> blockSerializer = new Serializer<Block>() {
      @Override
      public void write(Kryo kryo, Output output, Block object) {
        kryo.writeClassAndObject(output, object.getLocation().serialize());
      }

      @Override
      public Block read(Kryo kryo, Input input, Class type) {
        return Location.deserialize((Map<String, Object>)kryo.readClassAndObject(input)).getBlock();
      }
    };
    kryo.register(Block.class, blockSerializer);
    kryo.register(CLASS_CRAFT_BLOCK, blockSerializer);

    Serializer<World> worldSerializer=new Serializer<World>() {
      @Override
      public void write(Kryo kryo, Output output, World object) {
        uuidSerializer.write(kryo, output, object.getUID());
      }

      @Override
      public World read(Kryo kryo, Input input, Class<? extends World> type) {
        return getWorld(uuidSerializer.read(kryo, input, UUID.class));
      }
    };
    kryo.register(World.class, worldSerializer);
    kryo.register(CLASS_CRAFT_WORLD, worldSerializer);
  }
}

