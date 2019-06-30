package com.github.yona168.multiblockapi.storage.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
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


  private static Pool<Kryo> kryoPool;

  public static Pool<Kryo> getKryoPool() {
    return kryoPool;
  }
  public static Kryo getNextKryo(){return getKryoPool().obtain();}

  public static Toggleable enabler(MultiblockRegistry multiblockRegistry) {
    Component component = new Component();
    component.onEnable(() ->kryoPool=new Pool<Kryo>(true,false, 32) {
      @Override
      protected Kryo create() {
        final Kryo kryo=new Kryo();
        init(multiblockRegistry,kryo);
        return kryo;
      }
    });
    component.onDisable(()->kryoPool=null);
    return component;
  }

  private static void init(MultiblockRegistry multiblockRegistry, Kryo KRYO) {
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
  public static void freeze(Kryo kryo,Path file, Object value) throws IOException {
    if (file.getParent() != null)
      createDirectories(file.getParent());
    freeze(kryo,newOutputStream(file), value);
  }

  public static <Type> Type thaw(Kryo kryo,Path file) throws IOException {
    return thaw(kryo,file, () -> null);
  }

  public static <Type> Type thaw(Kryo kryo,Path file, Type defaultValue) throws IOException {
    return thaw(kryo,file, () -> defaultValue);
  }

  public static <Type> Type thaw(Kryo kryo,Path file, Supplier<Type> defaultSupplier) throws IOException {
    return isRegularFile(file) ? thaw(kryo,newInputStream(file), defaultSupplier) : defaultSupplier.get();
  }

  //--IO--
  public static void freeze(Kryo kryo,OutputStream output, Object value) {
    try (final Output out = new UnsafeOutput(output)) {
      kryo.writeClassAndObject(out, value);
    }
  }

  public static <Type> Type thaw(Kryo kryo,InputStream input) {
    return thaw(kryo,input, () -> null);
  }

  public static <Type> Type thaw(Kryo kryo,InputStream input, Type defaultValue) {
    return thaw(kryo,input, () -> defaultValue);
  }

  public static <Type> Type thaw(Kryo kryo,InputStream input, Supplier<Type> defaultSupplier) {
    try (final Input in = new UnsafeInput(input)) {
      final Type value = (Type) kryo.readClassAndObject(in);
      return value == null ? defaultSupplier.get() : value;
    }
  }
}