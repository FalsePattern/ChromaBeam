package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.github.falsepattern.chromabeam.resource.ResourcePack;

import java.util.HashMap;
import java.util.Map;

public class SoundRegistry {
    private final Map<String, Sound> sounds;
    private final ResourcePack resourcePack;
    SoundRegistry(ResourcePack resourcePack) {
        sounds = new HashMap<>();
        this.resourcePack = resourcePack;
    }

    public void registerSound(String name, String assetPath) {
        verify(name);
        var fileHandle = resourcePack.getAssetHandle(assetPath);
        sounds.put(name, Gdx.audio.newSound(fileHandle));
    }

    private void verify(String name) {
        if (sounds.containsKey(name)) throw new IllegalArgumentException("Sound with name \"" + name + "\" already registered!");
    }

    SoundManager createManager() {
        return new SoundManager(sounds);
    }
}
