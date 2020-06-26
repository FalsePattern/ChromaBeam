package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.Gdx;
import com.github.falsepattern.chromabeam.graphics.WorldRenderable;
import com.github.falsepattern.chromabeam.mod.Mod;
import com.github.falsepattern.chromabeam.mod.interfaces.World;
import com.github.falsepattern.chromabeam.world.BeamDrawHelper;

/**
 * This is the heart of the game. This mod has special access to some modules not exposed to other mods, including the base mod.
 */
public class CoreMod implements Mod {
    GUI gui;
    StuffPlacer stuffPlacer;
    CameraMover cameraMover;
    WorldRenderable worldRenderable;
    BackgroundRenderable backgroundRenderable;
    private static final String[] coreSounds = new String[]{"delete", "error", "load_error", "place", "saved", "welcome"};
    private static final String[] coreTextures = new String[]{"beam", "arrow", "bg", "arial-15"};
    @Override
    public void preInitialization(AssetRegistry registry) {
        for (var sound: coreSounds) {
            regSound(sound, registry);
        }
        for (var tex: coreTextures) {
            regTex(tex, registry);
        }
        registry.langRegistry.register("category.interact", "Interact");
        registry.langRegistry.register("category.circuit", "Circuit");
        registry.langRegistry.register("circuit.input", "Circuit IO port");
        registry.langRegistry.register("circuit.body", "Integrated Circuit");
    }

    private void regTex(String name, AssetRegistry registry) {
        registry.textureRegistry.loadTexture(name, "core/textures/" + name + ".png", 1, 1);
    }

    private void regSound(String name, AssetRegistry registry) {
        try {
            registry.soundRegistry.registerSound(name, "core/sounds/" + name + ".wav");
        } catch (IllegalArgumentException e) {
            registry.soundRegistry.registerSound(name, "core/sounds/" + name + ".ogg");
        }
    }

    @Override
    public void initialization(ComponentRegistry registry) {
        BeamDrawHelper.init();
        gui = new GUI();
        cameraMover = new CameraMover();
        worldRenderable = new WorldRenderable();
        backgroundRenderable = new BackgroundRenderable();
        stuffPlacer = new StuffPlacer(GlobalData.textureManager.getTexture("beam"), gui);
        stuffPlacer.setComponentRegistry(registry);
        gui.initArrowTexture(GlobalData.textureManager);
        Gdx.app.postRunnable(() -> {
            gui.readNamesFromRegistry(registry, GlobalData.langManager);
            gui.setSelectedComponent(0);
        });
    }

    @Override
    public void postInitialization(PostInitializationRegistry registry) {
        registry.inputHandlerRegistry.addInputHandler(gui);
        registry.inputHandlerRegistry.addInputHandler(stuffPlacer);
        registry.worldSpaceRendererRegistry.addRenderable(backgroundRenderable);
        registry.worldSpaceRendererRegistry.addRenderable(worldRenderable);
        registry.worldSpaceRendererRegistry.addRenderable(stuffPlacer);
        registry.worldSpaceRendererRegistry.addRenderable(cameraMover);
        registry.screenSpaceRendererRegistry.addRenderable(gui);
    }

    @Override
    public void worldInitialization(World world) {
        gui.setWorld(world);
        worldRenderable.setWorld(world);
        stuffPlacer.setWorld(world);
    }

    @Override
    public void shutdown() {
        gui.dispose();
    }

    @Override
    public String getModid() {
        return "core";
    }

    @Override
    public String getVersion() {
        return "0.7-dev";
    }
}
