package com.github.yona168.multiblockapi.storage.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.github.yona168.multiblockapi.registry.MultiblockRegistry;
import com.github.yona168.multiblockapi.state.MultiblockState;
import com.github.yona168.multiblockapi.structure.Multiblock;
import com.github.yona168.multiblockapi.util.ChunkCoords;
import com.github.yona168.multiblockapi.util.SimpleChunkCoords;
import com.gitlab.avelyn.architecture.base.Component;
import com.gitlab.avelyn.architecture.base.Toggleable;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.cglib.CGLibProxySerializer;
import de.javakaffee.kryoserializers.guava.*;
import org.bukkit.NamespacedKey;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static java.nio.file.Files.*;
import static org.bukkit.Bukkit.getWorld;

@SuppressWarnings("unchecked")
public class Kryogenic {


  private static Kryo KRYO;

  public static Kryo getKryo() {
    return KRYO;
  }

  public static Toggleable toggleable(MultiblockRegistry multiblockRegistry) {
    Component component = new Component();
    component.onEnable(() ->KRYO=new Kryo());
    component.onEnable(()->init(multiblockRegistry));
    component.onDisable(()->KRYO=null);
    return component;
  }

  private static void init(MultiblockRegistry multiblockRegistry) {
    KRYO.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    KRYO.setRegistrationRequired(false);
    BukkitKryogenics.registerSerializers(KRYO);
    KRYO.register(Arrays.asList("").getClass(), new DefaultSerializers.ArraysAsListSerializer());
    KRYO.register(Collections.EMPTY_LIST.getClass(), new DefaultSerializers.CollectionsEmptyListSerializer());
    KRYO.register(Collections.EMPTY_MAP.getClass(), new DefaultSerializers.CollectionsEmptyMapSerializer());
    KRYO.register(Collections.EMPTY_SET.getClass(), new DefaultSerializers.CollectionsEmptySetSerializer());
    KRYO.register(Collections.singletonList("").getClass(), new DefaultSerializers.CollectionsSingletonListSerializer());
    KRYO.register(Collections.singleton("").getClass(), new DefaultSerializers.CollectionsSingletonSetSerializer());
    KRYO.register(Collections.singletonMap("", "").getClass(), new DefaultSerializers.CollectionsSingletonMapSerializer());
    KRYO.register(GregorianCalendar.class, new GregorianCalendarSerializer());
    KRYO.register(InvocationHandler.class, new JdkProxySerializer());
    UnmodifiableCollectionsSerializer.registerSerializers(KRYO);
    SynchronizedCollectionsSerializer.registerSerializers(KRYO);

// register CGLibProxySerializer, works in combination with the appropriate action in handleUnregisteredClass (see below)
    KRYO.register(CGLibProxySerializer.CGLibProxyMarker.class, new CGLibProxySerializer());
// guava ImmutableList, ImmutableSet, ImmutableMap, ImmutableMultimap, ImmutableTable, ReverseList, UnmodifiableNavigableSet
    ImmutableListSerializer.registerSerializers(KRYO);
    ImmutableSetSerializer.registerSerializers(KRYO);
    ImmutableMapSerializer.registerSerializers(KRYO);
    ImmutableMultimapSerializer.registerSerializers(KRYO);
    ImmutableTableSerializer.registerSerializers(KRYO);
    ReverseListSerializer.registerSerializers(KRYO);
    UnmodifiableNavigableSetSerializer.registerSerializers(KRYO);
// guava ArrayListMultimap, HashMultimap, LinkedHashMultimap, LinkedListMultimap, TreeMultimap, ArrayTable, HashBasedTable, TreeBasedTable
    ArrayListMultimapSerializer.registerSerializers(KRYO);
    HashMultimapSerializer.registerSerializers(KRYO);
    LinkedHashMultimapSerializer.registerSerializers(KRYO);
    LinkedListMultimapSerializer.registerSerializers(KRYO);
    TreeMultimapSerializer.registerSerializers(KRYO);
    ArrayTableSerializer.registerSerializers(KRYO);
    HashBasedTableSerializer.registerSerializers(KRYO);
    TreeBasedTableSerializer.registerSerializers(KRYO);
    KRYO.addDefaultSerializer(Multiblock.class, new Serializer<Multiblock>() {
      @Override
      public void write(Kryo kryo, Output output, Multiblock object) {
        kryo.writeClassAndObject(output, object.getId());
      }

      @Override
      public Multiblock read(Kryo kryo, Input input, Class<? extends Multiblock> type) {
        return multiblockRegistry.get((NamespacedKey) kryo.readClassAndObject(input));
      }
    });
    KRYO.addDefaultSerializer(ChunkCoords.class, new Serializer<ChunkCoords>() {

      @Override
      public void write(Kryo kryo, Output output, ChunkCoords object) {
        kryo.writeClassAndObject(output, object.getWorld().getUID());
        output.writeInt(object.getX());
        output.writeInt(object.getZ());
      }

      @Override
      public ChunkCoords read(Kryo kryo, Input input, Class<? extends ChunkCoords> type) {
        return ChunkCoords.fromData(
                getWorld((UUID) kryo.readClassAndObject(input)),
                input.readInt(),
                input.readInt()
        );
      }
    });
    KRYO.register(SimpleChunkCoords.class);
    KRYO.register(MultiblockState.Orientation.class, new Serializer<MultiblockState.Orientation>() {

      @Override
      public void write(Kryo kryo, Output output, MultiblockState.Orientation object) {
        output.writeString(object.name());
      }

      @Override
      public MultiblockState.Orientation read(Kryo kryo, Input input, Class<? extends MultiblockState.Orientation> type) {
        return MultiblockState.Orientation.valueOf(input.readString());
      }
    });
  }

  //--Path--
  public static void freeze(Path file, Object value) throws IOException {
    if (file.getParent() != null)
      createDirectories(file.getParent());
    freeze(newOutputStream(file), value);
  }

  public static <Type> Type thaw(Path file) throws IOException {
    return thaw(file, () -> null);
  }

  public static <Type> Type thaw(Path file, Type defaultValue) throws IOException {
    return thaw(file, () -> defaultValue);
  }

  public static <Type> Type thaw(Path file, Supplier<Type> defaultSupplier) throws IOException {
    return isRegularFile(file) ? thaw(newInputStream(file), defaultSupplier) : defaultSupplier.get();
  }

  //--IO--
  public static void freeze(OutputStream output, Object value) {
    try (final Output out = new UnsafeOutput(output)) {
      KRYO.writeClassAndObject(out, value);
    }
  }

  public static <Type> Type thaw(InputStream input) {
    return thaw(input, () -> null);
  }

  public static <Type> Type thaw(InputStream input, Type defaultValue) {
    return thaw(input, () -> defaultValue);
  }

  public static <Type> Type thaw(InputStream input, Supplier<Type> defaultSupplier) {
    try (final Input in = new UnsafeInput(input)) {
      final Type value = (Type) KRYO.readClassAndObject(in);
      return value == null ? defaultSupplier.get() : value;
    }
  }

  public static void register(Class<?> clazz) {
    KRYO.register(clazz);
  }
}