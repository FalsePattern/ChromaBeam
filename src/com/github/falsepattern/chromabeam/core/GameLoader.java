package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.esotericsoftware.kryo.Kryo;
import com.github.falsepattern.chromabeam.graphics.ScreenSpaceRenderer;
import com.github.falsepattern.chromabeam.graphics.WorldSpaceRenderer;
import com.github.falsepattern.chromabeam.mod.Mod;
import com.github.falsepattern.chromabeam.resource.ClasspathResourcePack;
import com.github.falsepattern.chromabeam.resource.DirectoryResourcePack;
import com.github.falsepattern.chromabeam.resource.ResourcePackStack;
import com.github.falsepattern.chromabeam.resource.ZipResourcePack;
import com.github.falsepattern.chromabeam.util.KeyBinds;
import com.github.falsepattern.chromabeam.world.BetterWorld;
import com.github.falsepattern.chromabeam.mod.interfaces.World;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipFile;

public class GameLoader extends ApplicationAdapter {
    Mod[] mods;
    ComponentRegistry componentRegistry;
    WorldSpaceRenderer worldSpaceRenderer;
    ScreenSpaceRenderer screenSpaceRenderer;
    InputHandlerManager inputHandlerManager;
    World world;
    private final AtomicBoolean cleanExit = new AtomicBoolean(false);
    private boolean simShouldRun = true;
    private Thread gameThread;
    private Thread crashWatcher;
    private Thread simThread;
    private Runnable simulation;
    private final long startNano;
    public GameLoader(Mod[] mods, long startNano) {
        this.mods = mods;
        this.startNano = startNano;
    }
    @SuppressWarnings({"BusyWait", "StatementWithEmptyBody"})
    @Override
    public void create() {
        CoreData.kryo = new Kryo();
        CoreData.kryo.setRegistrationRequired(false);
        GlobalData.inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(GlobalData.inputMultiplexer);
        GlobalData.keyBinds = new KeyBinds();
        GlobalData.inputMultiplexer.addProcessor(GlobalData.keyBinds);

        GlobalData.mods = mods;
        gameThread = Thread.currentThread();
        simulation = () -> {
            long before = 0;
            long after;
            long targetSleep;
            while (simShouldRun) {
                world.update();
                if (GlobalData.tpsLimitToggle) {
                    GlobalData.tpsLimitToggle = false;
                    GlobalData.tpsLimitOn = !GlobalData.tpsLimitOn;
                }
                targetSleep = GlobalData.tpsLimitOn ? 1000000000L / GlobalData.tpsLimit : 1;
                if (targetSleep > 1000000) {
                    try {
                        Thread.sleep(targetSleep / 1000000);
                    } catch (InterruptedException ignored) {}
                } else {
                    after = System.nanoTime();
                    long delta = after - before;
                    long targetTime = after + targetSleep - delta;
                    while (System.nanoTime() < targetTime) {}
                    before = System.nanoTime();
                }
            }
        };
        crashWatcher = new Thread(() -> {
            while (true) {
                if (!cleanExit.get() && gameThread.isAlive() && simThread.isAlive()) {
                    try {
                        gameThread.join(1000);
                    } catch (InterruptedException ignored) {}
                } else if (cleanExit.get()) {
                    break;
                } else {
                    simShouldRun = false;
                    System.err.println("The game has crashed! Attempting emergency world save...");
                    var root = new JFrame();
                    JOptionPane.showMessageDialog(root, "The game has crashed and cannot be recovered. After you close this message, a save dialog will pop up to do an emergency save.", "Emergency save", JOptionPane.WARNING_MESSAGE);
                    try {
                        if ( SaveEngine.saveComponentsToFile(CoreData.kryo, world.getAllComponents(), world.getAllLabels())) {
                            System.err.println("Emergency save has been written. Force-shutting down game.");
                        } else {
                            System.err.println("An error occurred during emergency save. All changes since the last save have been lost, sorry...");
                        }
                        if (gameThread.isAlive())
                            Gdx.app.exit();
                        if (simThread.isAlive()) {
                            simThread.interrupt();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("An error occurred during emergency save. All changes since the last save have been lost, sorry...");
                    }
                    System.exit(1);
                }
            }
        }, "CrashWatcher");
        System.out.println("Finding resource packs...");
        var finalResourcePack = new ResourcePackStack();
        finalResourcePack.addResourcePack(new ClasspathResourcePack());
        var dir = new File(Gdx.files.getLocalStoragePath() + "/resourcepacks");
        if (!dir.isDirectory()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Error while creating resourcepacks directory: File already exists!");
            }
        }
        var config = Gdx.files.local("resourcepacks.txt");
        var files = new ArrayList<String>();
        if (!config.exists()) {
            try (var output = new PrintStream(config.write(true))) {
                output.println("# You can put the file names of your used resourcepacks in this file line by line.");
                output.println("# Example:");
                output.println("# pack1.zip");
                output.println("# pack2.zip");
                output.flush();
            }
        } else {
            var cfg = config.readString().replace('\r', '\n').split("\n");
            for (var line: cfg) {
                files.add(line.trim());
            }
        }
        for (var fileName: files) {
            if (fileName.startsWith("#") || fileName.length() == 0)continue;
            var file = Paths.get(dir.getPath(), fileName).toFile();
            if (file.isDirectory()) {
                finalResourcePack.addResourcePack(new DirectoryResourcePack(file));
            } else if (file.getName().endsWith(".zip")){
                try {
                    finalResourcePack.addResourcePack(new ZipResourcePack(new ZipFile(file)));
                    System.out.println("found resourcepack " + fileName);
                } catch (IOException e) {
                    System.err.println("Invalid resource pack file: " + fileName);
                }
            } else {
                System.err.println("Invalid resource pack file: " + fileName);
            }
        }
        System.out.println("Loading mods, please wait...");
        //preInitialization
        var assetRegistry = new AssetRegistry(finalResourcePack);
        for (var mod: mods) {
            var modid = mod.getModid();
            if (!(modid.equals("core") || modid.equals("circuit")))
            System.out.println("PreInitializing mod " + mod.getModid());
            mod.preInitialization(assetRegistry);
        }
        GlobalData.textureManager = assetRegistry.textureRegistry.createManager();
        GlobalData.langManager = assetRegistry.langRegistry.createManager();
        GlobalData.soundManager = assetRegistry.soundRegistry.createManager();
        //initialization
        componentRegistry = new ComponentRegistry();
        for (var mod: mods) {
            var modid = mod.getModid();
            if (!(modid.equals("core") || modid.equals("circuit")))
            System.out.println("Initializing mod " + mod.getModid());
            componentRegistry.setLoadingMod(mod);
            mod.initialization(componentRegistry);
        }
        componentRegistry.finish();
        //postInitialization
        worldSpaceRenderer = new WorldSpaceRenderer();
        screenSpaceRenderer = new ScreenSpaceRenderer();
        inputHandlerManager = new InputHandlerManager();
        GlobalData.inputMultiplexer.addProcessor(worldSpaceRenderer);
        var reg = new PostInitializationRegistry(screenSpaceRenderer, worldSpaceRenderer, inputHandlerManager);
        for (var mod: mods) {
            var modid = mod.getModid();
            if (!(modid.equals("core") || modid.equals("circuit")))
            System.out.println("PostInitializing mod " + mod.getModid());
            mod.postInitialization(reg);
        }

        System.out.println(mods.length - 2 + " mod" + (mods.length - 2 != 1 ? "s" : "") + " loaded!");
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);

        Gdx.graphics.setVSync(true);
        world = new BetterWorld(true);
        for (var mod: mods) {
            mod.worldInitialization(world);
        }
        System.out.println("Game world created! Welcome to ChromaBeam!");
        simThread = new Thread(simulation);
        simThread.start();
        crashWatcher.start();
        GlobalData.soundManager.play("welcome");
        System.out.println("Total load time: " + (System.nanoTime() - startNano) / 1000000000f + " s");
    }

    @Override
    public void render() {
        draw();
        world.pause();
        processInput();
        world.unpause();
        GlobalData.keyBinds.update();
    }

    private void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        worldSpaceRenderer.draw();
        screenSpaceRenderer.draw();
    }

    private void processInput() {
        var shiftHeld = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        for (var handler: inputHandlerManager.handlers) {
            handler.handleInput(shiftHeld);
        }
    }

    @Override
    public void resize(int width, int height) {
        worldSpaceRenderer.resize(width, height);
        screenSpaceRenderer.resize(width, height);
    }

    @Override
    public void dispose() {
        cleanExit.set(true);
        crashWatcher.interrupt();
        simShouldRun = false;
        try {
            simThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var frame = new JFrame();
        var prompt = JOptionPane.showConfirmDialog(frame, "Would you like to save the current world to disk?", "Qutting game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (prompt == JOptionPane.YES_OPTION) {
            SaveEngine.saveComponentsToFile(CoreData.kryo, world.getAllComponents(), world.getAllLabels());
        }
        GlobalData.textureManager.dispose();
        GlobalData.soundManager.dispose();
        worldSpaceRenderer.dispose();
        screenSpaceRenderer.dispose();
    }


}
