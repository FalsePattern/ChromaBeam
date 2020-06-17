package com.github.falsepattern.chromabeam.core;

import com.github.falsepattern.chromabeam.resource.ResourcePack;

public final class AssetRegistry {
    public final TextureRegistry textureRegistry;
    public final LangRegistry langRegistry;
    public final SoundRegistry soundRegistry;
    public final ResourcePack resourcePack;

    AssetRegistry(ResourcePack resourcePack) {
        textureRegistry = new TextureRegistry(resourcePack);
        langRegistry = new LangRegistry();
        soundRegistry = new SoundRegistry(resourcePack);
        this.resourcePack = resourcePack;
    }
}
