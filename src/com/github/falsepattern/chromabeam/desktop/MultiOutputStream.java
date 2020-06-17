package com.github.falsepattern.chromabeam.desktop;

import java.io.IOException;
import java.io.OutputStream;

class MultiOutputStream extends OutputStream {
    private final OutputStream[] streams;
    MultiOutputStream(OutputStream... streams) {
        this.streams = streams;
    }

    @Override
    public void write(int b) throws IOException {
        for (var s: streams) s.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (var s: streams) s.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (var s: streams) s.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        for (var s: streams) s.flush();
    }

    @Override
    public void close() throws IOException {
        for (var s: streams) s.close();
    }
}
