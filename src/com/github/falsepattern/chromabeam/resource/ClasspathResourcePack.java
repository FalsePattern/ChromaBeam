package com.github.falsepattern.chromabeam.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.github.falsepattern.chromabeam.util.FakeFileHandle;

import java.io.IOException;
import java.io.InputStream;

public class ClasspathResourcePack implements ResourcePack {
    @Override
    public InputStream getAssetStream(String assetPath) {
        return getClass().getResourceAsStream("/assets/" + assetPath);
    }

    @Override
    public FileHandle getAssetHandle(String assetPath) {
        var stream = getAssetStream(assetPath);
        if (stream != null) {
            var id = assetPath.lastIndexOf('.');
            if (id >= 0 && id + 1 < assetPath.length()) {
                return new FakeFileHandle(stream, assetPath.substring(id + 1));
            } else {
                return new FakeFileHandle(stream, "");
            }
        } else {
            return null;
        }
    }

    @Override
    public byte[] getAssetBytes(String assetPath) {
        byte[] result;
        try (var stream = getAssetStream(assetPath)) {
            result = stream.readAllBytes();
        } catch (IOException ignored){
            result = new byte[0];
        }
        return result;
    }
}
