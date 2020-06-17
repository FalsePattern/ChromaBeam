package com.github.falsepattern.chromabeam.resource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.github.falsepattern.chromabeam.util.FakeFileHandle;

import java.io.IOException;
import java.io.InputStream;

public interface ResourcePack extends Disposable {
    InputStream getAssetStream(String assetPath);

    default FileHandle getAssetHandle(String assetPath) {
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

    default byte[] getAssetBytes(String assetPath) {
        byte[] result;
        try (var stream = getAssetStream(assetPath)) {
            result = stream.readAllBytes();
        } catch (IOException ignored){
            result = new byte[0];
        }
        return result;
    }

    @Override
    default void dispose() {

    }
}
