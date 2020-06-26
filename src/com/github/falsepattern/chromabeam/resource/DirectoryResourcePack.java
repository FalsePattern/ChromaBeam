package com.github.falsepattern.chromabeam.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class DirectoryResourcePack implements ResourcePack{
    private final File directory;
    public DirectoryResourcePack(File directory) {
        this.directory = directory;
    }
    @Override
    public InputStream getAssetStream(String assetPath) {
        InputStream result = null;
        if (directory.isDirectory()) {
            var file = new File(Paths.get(directory.getAbsolutePath(), "assets", assetPath).toUri());
            if (file.exists()) {
                try {
                    result = new FileInputStream(file);
                } catch (IOException ignored) {}
            }
        }
        return result;
    }
}
