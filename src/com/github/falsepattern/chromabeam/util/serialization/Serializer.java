package com.github.falsepattern.chromabeam.util.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Serializer implements AutoCloseable {
    private static final byte[] OBJECT_HEADER = new byte[]{0x01, 0x00};
    private OutputStream stream;
    public Serializer(OutputStream stream) {
        this.stream = stream;
    }
    public void writeByte(int b) {
        try {
            stream.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void writeBytes(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            writeByte(b[i]);
        }
    }

    public void writeBytes(byte[] b, int offset, int length) {
        if (offset + length > b.length) throw new ArrayIndexOutOfBoundsException();
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            writeByte(b[i]);
        }
    }

    public void writeBoolean(boolean b) {
        writeByte(b ? 1 : 0);
    }

    public void writeChar(char c) {
        writeByte(c & 0xff);
        writeByte(c >>> 8);
    }

    public void writeInt(int i) {
        writeByte(i & 0xff);
        writeByte((i >>> 8) & 0xff);
        writeByte((i >>> 16) & 0xff);
        writeByte(i >>> 24);
    }

    public void writeInts(int[] b, int offset, int length) {
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            writeInt(b[i]);
        }
    }

    public void writeAsciiString(String s) {
        byte[] chars = s.getBytes(StandardCharsets.US_ASCII);
        int l = chars.length - 1;
        for (int i = 0; i < l; i++) {
            writeByte(chars[i] & 0x7f);
        }
        writeByte((chars[chars.length - 1] & 0x7f) | 0x80);
    }

    public <T extends ChromaSerializable> void writeObject(T object) {
        writeBytes(OBJECT_HEADER);
        writeAsciiString(object.getClass().getName());
        object.write(this);
    }

    @Override
    public void close() {
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
