package com.github.falsepattern.chromabeam.util.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Deserializer implements AutoCloseable{

    private InputStream stream;
    public Deserializer(InputStream stream) {
        this.stream = stream;
    }

    public byte readByte() {
        try {
            return (byte)stream.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte readByteRaw() throws Exception {
        return (byte)stream.read();
    }

    public int readByteAsInt() {
        try {
            return stream.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readBytes(int count) {
        byte[] output = new byte[count];
        readBytes(output, 0, count);
        return output;
    }

    public void readBytes(byte[] buffer, int offset, int length) {
        if (offset + length > buffer.length) throw new ArrayIndexOutOfBoundsException();
        int end = offset + length;
        try {
        for (int i = offset; i < end; i++) {
                buffer[i] = readByteRaw();
        }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean readBoolean() {
        return readByte() != 0;
    }

    public boolean[] readBooleans(int n) {
        boolean[] output = new boolean[n];
        try {
        for (int i = 0; i < n; i++) {
                output[i] = stream.read() != 0;
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    public short readShort() {
        try {
            return (short) (stream.read() | (stream.read() << 8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int readInt() {
        try {
            return stream.read() | (stream.read() << 8) | (stream.read() << 16) | (stream.read() << 24);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int[] readInts(int n) {
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = readInt();
        }
        return result;
    }

    public long readLong() {
        return readByteAsInt() | (readByteAsInt() << 8) | (readByteAsInt() << 16) | (readByteAsInt() << 24) |
                ((long) readByteAsInt() << 32) | ((long) readByteAsInt() << 40) | ((long) readByteAsInt() << 48)
                | ((long) readByteAsInt() << 56);
    }

    public String readString() {
        var builder = new StringBuilder();
        byte data = 0;
        while (((data = readByte()) & 0b10000000) == 0) {
            builder.append((char)(data & 0x7f));
        }
        builder.append((char)(data & 0x7f));
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public <T extends ChromaSerializable> T readObject() {
        if (readByteAsInt() != 0x01 || readByteAsInt() != 0x00) throw new IllegalStateException("Object header bytes not found while trying to deserialize data!");
        String className = readString();
        Class<T> clazz;
        try {
            var rawClass = Class.forName(className);
            if (ChromaSerializable.class.isAssignableFrom(rawClass)) {
                clazz = (Class<T>) rawClass;
                Constructor<T> constructor = clazz.getConstructor();
                var instance = constructor.newInstance();
                instance.read(this);
                return instance;
            } else {
                throw new IllegalArgumentException("Cannot deserialize object which does not implement ChromaSerializable! Possibly corrupted save?");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find class " + className, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 0 argument constructor for " + className, e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Could not create instance of " + className, e);
        }

    }

    @Override
    public void close() {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
