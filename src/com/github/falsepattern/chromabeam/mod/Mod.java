package com.github.falsepattern.chromabeam.mod;

import com.github.falsepattern.chromabeam.core.AssetRegistry;
import com.github.falsepattern.chromabeam.core.ComponentRegistry;
import com.github.falsepattern.chromabeam.core.PostInitializationRegistry;
import com.github.falsepattern.chromabeam.mod.interfaces.World;

public interface Mod {
    /**
     * First phase of mod loading. Mods load all textures and language definitions here.
     * @param registry This contains the language and the texture registration instance.
     */
    void preInitialization(AssetRegistry registry);

    /**
     * Second phase of mod loading. Mods register all added components here.
     */
    void initialization(ComponentRegistry registry);

    /**
     * Final phase of mod loading. Mods can register custom renderers and input handlers here.
     */
    void postInitialization(PostInitializationRegistry registry);

    /**
     * Called after a world has been created/loaded. The mod can do custom initialization using the world here.
     */
    void worldInitialization(World world);

    /**
     * Called when the user exits the game. The mod can do cleanup before the game exits.
     */
    void shutdown();

    /**
     * Returns the modid of the mod. Should be composed of multiple lowercase a-z characters.
     */
    String getModid();

    /**
     * Returns the version of the mod. Internally this is not used, but mods can use it for inter-mod interaction or
     * other operations.
     */
    String getVersion();
}


