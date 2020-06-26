package com.github.falsepattern.chromabeam.circuit;

import com.github.falsepattern.chromabeam.core.AssetRegistry;
import com.github.falsepattern.chromabeam.core.ComponentRegistry;
import com.github.falsepattern.chromabeam.core.PostInitializationRegistry;
import com.github.falsepattern.chromabeam.mod.Mod;
import com.github.falsepattern.chromabeam.mod.interfaces.World;

public class CircuitMod implements Mod {
    @Override
    public void preInitialization(AssetRegistry registry) {
        registry.textureRegistry.loadTexture("circuit.input", "circuit/textures/input.png", 8, 1);
        registry.textureRegistry.loadTexture("circuit.body", "circuit/textures/body.png", 2, 1);
    }

    @Override
    public void initialization(ComponentRegistry registry) {
        registry.registerComponent(CircuitIOPortVirtual.class);
    }

    @Override
    public void postInitialization(PostInitializationRegistry registry) {

    }

    @Override
    public void worldInitialization(World world) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getModid() {
        return "circuit";
    }

    @Override
    public String getVersion() {
        return "0.7-dev";
    }
}
