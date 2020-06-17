package com.github.falsepattern.chromabeam.resource;
import com.badlogic.gdx.utils.Disposable;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipResourcePack implements ResourcePack, Disposable {
    private final ZipFile zipFile;
    public ZipResourcePack(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    private ZipEntry getEntry(String path) {
        return zipFile.getEntry("assets/" + path);
    }
    @Override
    public InputStream getAssetStream(String assetPath) {
        var entry = getEntry(assetPath);
        if (entry != null) {
            try {
                return zipFile.getInputStream(entry);
            } catch (IOException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void dispose() {
        try {
            zipFile.close();
        } catch (IOException ignored){}
    }
}
