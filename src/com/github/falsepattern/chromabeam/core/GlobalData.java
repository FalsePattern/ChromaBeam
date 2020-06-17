package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.InputMultiplexer;
import com.github.falsepattern.chromabeam.mod.Mod;
import com.github.falsepattern.chromabeam.util.KeyBinds;

import java.util.HashMap;
import java.util.Map;

public final class GlobalData {

    public static final float SPRITE_SIZE = 32;
    public static final int TEXTURE_GAP = 2;

    public static int tpsLimit = 1;
    public static boolean tpsLimitToggle = false;
    public static boolean tpsLimitOn;
    public static String hoveredComponentName = "";

    public static Mod[] mods;

    public static TextureManager textureManager;
    public static LangManager langManager;
    public static SoundManager soundManager;
    public static KeyBinds keyBinds;
    public static InputMultiplexer inputMultiplexer;

    public static Map<String, String> modsInLoadedSave = new HashMap<>();
}
