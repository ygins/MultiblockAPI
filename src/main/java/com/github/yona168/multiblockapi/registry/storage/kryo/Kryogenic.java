package com.github.yona168.multiblockapi.registry.storage.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Supplier;

import static java.nio.file.Files.*;

@SuppressWarnings("unchecked")
public class Kryogenic {
    public static final Kryo KRYO = new Kryo();
    static{
        BukkitKryogenics.registerSerializers(KRYO);
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
}