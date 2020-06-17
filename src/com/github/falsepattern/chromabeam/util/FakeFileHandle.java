package com.github.falsepattern.chromabeam.util;

import com.badlogic.gdx.files.FileHandleStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used to trick LibGDX into thinking that a simple input stream is actually a file. Does not work for writes,
 * and can only be read once.
 */
public class FakeFileHandle extends FileHandleStream {
    private final InputStream stream;
    private int length;
    private final String extension;
    public FakeFileHandle(InputStream stream, String extension) {
        super("");
        this.stream = stream;
        this.extension = extension;
        try {
            this.length = stream.available();
        } catch (IOException e) {
            this.length = 0;
        }
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public InputStream read() {
        return stream;
    }

    @Override
    public byte[] readBytes() {
        return super.readBytes();
    }

    @Override
    public String extension() {
        return extension;
    }
}
