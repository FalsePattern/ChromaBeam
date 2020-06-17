package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.audio.Sound;

import java.util.Collections;
import java.util.Map;

public class SoundManager {
    private Map<String, Sound> sounds;
    SoundManager(Map<String, Sound> sounds) {
        this.sounds = Collections.unmodifiableMap(sounds);
    }

    public void play(String soundName) {
        if (!sounds.containsKey(soundName)) throw new IllegalArgumentException("Unregistered sound \"" + soundName + "\"");
        sounds.get(soundName).play();
    }


    void dispose() {
        for (var s: sounds.values()) {
            s.dispose();
        }
    }
}
