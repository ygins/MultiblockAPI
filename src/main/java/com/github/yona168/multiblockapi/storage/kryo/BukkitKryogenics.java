//Created by https://github.com/Exerosis with personal alterations. Used with permission.
package com.github.yona168.multiblockapi.storage.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.Class;
import java.util.UUID;

import static com.github.yona168.multiblockapi.storage.kryo.BukkitSerializers.*;
import static org.bukkit.Bukkit.getWorld;

public class BukkitKryogenics {
  private static final String ERR_SAVE_ENTITY = "Failed to serialize NBT for an Entity.";
  private static final String ERR_SAVE_ITEM = "Failed to serialize NBT for an ItemStack.";
  private static final String ERR_LOAD_ITEM = "Failed to deserialize NBT for an ItemStack.";
  private static final String ERR_LOAD_ENTITY = "Failed to deserialize NBT for an Entity.";

  public static void registerSerializers(Kryo kryo) {
    /*
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
  */
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
        kryo.writeObject(output, location.getWorld());
        output.writeDouble(location.getX());
        output.writeDouble(location.getY());
        output.writeDouble(location.getZ());
        output.writeFloat(location.getYaw());
        output.writeFloat(location.getPitch());

      }

      @Override
      public Location read(Kryo kryo, Input input, Class type) {
        return new Location(
                kryo.readObject(input, World.class),
                input.readDouble(),
                input.readDouble(),
                input.readDouble(),
                input.readFloat(),
                input.readFloat()
        );
      }
    });

    final Serializer<Block> blockSerializer = new Serializer<Block>() {
      @Override
      public void write(Kryo kryo, Output output, Block object) {
        kryo.writeClassAndObject(output, object.getLocation());
      }

      @Override
      public Block read(Kryo kryo, Input input, Class type) {
        return ((Location)kryo.readClassAndObject(input)).getBlock();
      }
    };
    kryo.register(Block.class, blockSerializer);
    kryo.register(CLASS_CRAFT_BLOCK, blockSerializer);

    Serializer<World> worldSerializer = new Serializer<World>() {
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

    Serializer<Chunk> chunkSerializer = new Serializer<Chunk>() {
      @Override
      public void write(Kryo kryo, Output output, Chunk object) {
        kryo.writeClassAndObject(output, ChunkCoords.fromChunk(object));
      }

      @Override
      public Chunk read(Kryo kryo, Input input, Class<? extends Chunk> type) {
        return ((ChunkCoords) kryo.readClassAndObject(input)).toChunk();
      }
    };

    kryo.addDefaultSerializer(Chunk.class, chunkSerializer);
    kryo.register(Chunk.class);

    kryo.addDefaultSerializer(Plugin.class,new Serializer<Plugin>(){

      @Override
      public void write(Kryo kryo, Output output, Plugin object) {
        output.writeString(object.getName());
      }

      @Override
      public Plugin read(Kryo kryo, Input input, Class<? extends Plugin> type) {
        return Bukkit.getPluginManager().getPlugin(input.readString());
      }
    });
  }


}

