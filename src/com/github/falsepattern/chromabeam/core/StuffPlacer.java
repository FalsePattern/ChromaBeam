package com.github.falsepattern.chromabeam.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.github.falsepattern.chromabeam.circuit.CircuitMaster;
import com.github.falsepattern.chromabeam.circuit.CircuitSlave;
import com.github.falsepattern.chromabeam.graphics.DrawingHelpers;
import com.github.falsepattern.chromabeam.mod.interfaces.InputHandler;
import com.github.falsepattern.chromabeam.mod.interfaces.Renderable;
import com.github.falsepattern.chromabeam.util.MutablePair;
import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.mod.interfaces.World;
import com.github.falsepattern.chromabeam.util.Color;
import com.github.falsepattern.chromabeam.util.Vector2i;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * I'm a very big boy.
 */
class StuffPlacer implements Renderable, InputHandler {

    private final Vector3 hoverConversionBuffer = new Vector3();
    private World world;
    private List<Component> prefabs;
    private int selectedComponent = 0;
    private Component current;
    private boolean empty;
    private int x;
    private int y;
    private final TextureRegion overlayTexture;
    private final GUI gui;
    private boolean interacting = false;
    private State state = State.PLACE;
    private final Vector2i positionA = new Vector2i();
    private final Vector2i positionB = new Vector2i();
    private final Kryo kryo;
    private boolean clicking = false;

    public StuffPlacer(TextureRegion selectionOverlayTexture, GUI gui, Kryo kryo) {
        this.overlayTexture = selectionOverlayTexture;
        this.gui = gui;
        this.kryo = kryo;
    }

    private Component worldComponent;
    @Override
    public void prepare(SpriteBatch batch, OrthographicCamera camera) {
        hoverConversionBuffer.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(hoverConversionBuffer);
        x = (int)Math.floor(hoverConversionBuffer.x);
        y = (int)Math.floor(hoverConversionBuffer.y);
        gui.setMousePos(x, y);
    }

    @Override
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        switch(state) {
            case PLACE -> renderPlacing(batch);
            case SELECT, STACKPRIME -> renderSelecting(batch);
            case COPYPASTE, CUTPASE -> renderHoverHolo(batch);
            case STACKEXECUTE -> {
                renderSelecting(batch);
                renderStacking(batch);
            }
            case CIRCUIT -> {
                renderCircuiting(batch);
            }
        }
    }

    private void renderCircuiting(SpriteBatch batch) {

    }

    private void renderStacking(SpriteBatch batch) {
        int deltaX = (stackDirection == 0 ? 1 : stackDirection == 2 ? -1 : 0) * (Math.abs(positionB.x - positionA.x) + 1);
        int deltaY = (stackDirection == 1 ? -1 : stackDirection == 3 ? 1 : 0) * (Math.abs(positionB.y - positionA.y) + 1);
        boolean warn = false;
        for (var comp:clonedComponents.a) {
            int x = comp.getX() + positionA.x;
            int y = comp.getY() + positionA.y;
            if (comp instanceof CircuitSlave) {
                x += ((CircuitSlave) comp).master.getX();
                y += ((CircuitSlave) comp).master.getY();
            }
            for (int i = 1; i <= stackCount; i++) {
                x += deltaX;
                y += deltaY;
                DrawingHelpers.drawComponentTextureColoredAlpha(batch, comp.getTexture(), x, y, comp.getRotation(), comp.getDrawColor(), comp.getFlipped(), 0.5f);
                warn |= world.getComponent(x, y) != null;
            }
        }
        overlapping = warn;
        gui.warnCollision = warn;
    }

    private void renderPlacing(SpriteBatch batch) {
        if (!interacting && current != null) {
            int compColor = current.getDrawColor();
            if (compColor != Color.WHITE) {
                DrawingHelpers.drawComponentColoredAlpha(batch, current, .5f);
            } else {
                if (empty) {
                    batch.setColor(.8f, 1, .8f, .5f);
                } else {
                    batch.setColor(1, .8f, .8f, .5f);
                }
                DrawingHelpers.drawComponent(batch, current);
            }
        }
    }

    private void renderSelecting(SpriteBatch batch) {
        batch.setColor(1, 1, 1, .25f);
        int x, y, w, h;
        if (positionA.x < positionB.x) {
            x = positionA.x;
            w = positionB.x - positionA.x + 1;
        } else {
            x = positionB.x;
            w = positionA.x - positionB.x + 1;
        }
        if (positionA.y < positionB.y) {
            y = positionA.y;
            h = positionB.y - positionA.y + 1;
        } else {
            y = positionB.y;
            h = positionA.y - positionB.y + 1;
        }
        batch.draw(overlayTexture, x, y, w, h);
        batch.setColor(0, 0, 1, .25f);
        batch.draw(overlayTexture, positionA.x, positionA.y, 1, 1);
        batch.setColor(0, 1, 0, .25f);
        batch.draw(overlayTexture, positionB.x, positionB.y, 1, 1);
    }

    private boolean overlapping = false;
    private void renderHoverHolo(SpriteBatch batch) {
        boolean warn = false;
        for (var comp:clonedComponents.a) {
            int x = comp.getX() + this.x;
            int y = comp.getY() + this.y;
            if (comp instanceof CircuitSlave) {
                x += ((CircuitSlave) comp).master.getX();
                y += ((CircuitSlave) comp).master.getY();
            }
            DrawingHelpers.drawComponentTextureColoredAlpha(batch, comp.getTexture(), x, y, comp.getRotation(), comp.getDrawColor(), comp.getFlipped(), 0.5f);
            warn |= world.getComponent(x, y) != null;
        }
        overlapping = warn;
        gui.warnCollision = warn;

    }

    @Override
    public void finish(SpriteBatch batch, OrthographicCamera camera) {

    }

    @Override
    public void resize(int width, int height) {}

    public void setWorld(World world) {
        this.world = world;
    }

    public void setComponentRegistry(ComponentRegistry registry) {
        selectedComponent = 0;
        interacting = true;
        prefabs = registry.getAllPrefabs();
    }

    @Override
    public void handleInput(boolean shiftHeld) {
        var newComponent = world.getComponent(x, y);
        if (worldComponent != newComponent) {
            worldComponent = newComponent;
            if (worldComponent != null) {
                GlobalData.hoveredComponentName = GlobalData.langManager.translate(worldComponent.getRegistryName());
            } else {
                GlobalData.hoveredComponentName = "";
            }
        }
        var prev = current;
        empty = worldComponent == null;
        if (interacting) {
            current = worldComponent;
        } else {
            current.setPos(x, y);
        }
        switch (state) {
            case PLACE -> handlePlacerInput(shiftHeld);
            case SELECT -> handleSelectorInput(shiftHeld);
            case COPYPASTE -> handleCopyPasteInput(shiftHeld);
            case CUTPASE -> handleCutPasteInput(shiftHeld);
            case STACKPRIME -> handleStackPrimeInput(shiftHeld);
            case STACKEXECUTE -> handleStackExecuteInput(shiftHeld);
            case CIRCUIT -> handleCircuitInput(shiftHeld);
        }

        if (current != prev && clicking) {
            if (prev != null)
                prev.clickStop();
            if (current != null)
                current.clickStart();
        }
    }

    private void handleCircuitInput(boolean shiftHeld) {

    }

    private int stackCount = 1;
    private int stackDirection = 0;

    private void handleStackExecuteInput(boolean shiftHeld) {
        int mod = shiftHeld ? 10 : 1;
        if (GlobalData.keyBinds.isJustPressed("up")) {
            stackCount += mod;
        }
        if (GlobalData.keyBinds.isJustPressed("down")) {
            stackCount -= mod;
            if (stackCount < 1) stackCount = 1;
        }
        if (GlobalData.keyBinds.isJustPressed("cancel")) {
            gui.setGUIMode(GUI.GUIMode.SELECTOR);
            state = State.SELECT;
        }

        if (!overlapping && GlobalData.keyBinds.isJustPressed("apply")) {
            int deltaX = (stackDirection == 0 ? 1 : stackDirection == 2 ? -1 : 0) * (Math.abs(positionB.x - positionA.x) + 1);
            int deltaY = (stackDirection == 1 ? -1 : stackDirection == 3 ? 1 : 0) * (Math.abs(positionB.y - positionA.y) + 1);

            for (var comp:clonedComponents.a) {

                if (! (comp instanceof CircuitSlave)) {
                    int x = comp.getX() + positionA.x;
                    int y = comp.getY() + positionA.y;
                    int rot = comp.getRotation();
                    boolean flip = comp.getFlipped();
                    for (int i = 1; i <= stackCount; i++) {
                        x += deltaX;
                        y += deltaY;
                        world.cloneComponent(x, y, rot, flip, comp);
                    }
                }
            }
            gui.setGUIMode(GUI.GUIMode.PLACER);
            state = State.PLACE;
        }
        gui.stackValue = stackCount;
    }

    private void handleStackPrimeInput(boolean shiftHeld) {
        if (GlobalData.keyBinds.isJustPressed("right")) {
            gui.stackDirection = stackDirection = 0;
        } else if (GlobalData.keyBinds.isJustPressed("down")) {
            gui.stackDirection = stackDirection = 1;
        } else if (GlobalData.keyBinds.isJustPressed("left")) {
            gui.stackDirection = stackDirection = 2;
        } else if (GlobalData.keyBinds.isJustPressed("up")) {
            gui.stackDirection = stackDirection = 3;
        } else if (GlobalData.keyBinds.isJustPressed("cancel")) {
            gui.setGUIMode(GUI.GUIMode.SELECTOR);
            state = State.SELECT;
            return;
        } else {
            return;
        }
        gui.setGUIMode(GUI.GUIMode.STACKEXECUTE);
        state = State.STACKEXECUTE;
        stackCount = 1;
        clear();
        cloneSelectionToHover();
    }

    private boolean clickBlock = false;

    private void handleCutPasteInput(boolean shiftHeld) {

        handleSelectionTransforms(shiftHeld);
        if (!overlapping && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (var comp : clonedComponents.a) {

                if (! (comp instanceof CircuitSlave)) {
                comp.setPos(comp.getX() + x, comp.getY() + y);
                world.setComponent(comp);
                }
            }
            for (var label: clonedComponents.b.entrySet()) {
                var pos = label.getKey();
                world.setLabel(pos[0] + x, pos[1] + y, label.getValue());
            }
            clear();
            state = State.SELECT;
            gui.setGUIMode(GUI.GUIMode.SELECTOR);
            clickBlock = true;
            return;
        }
        if (GlobalData.keyBinds.isJustPressed("cancel")) {
            state = State.SELECT;
            gui.setGUIMode(GUI.GUIMode.SELECTOR);
            int oX = positionA.x;
            int oY = positionA.y;
            for (var comp: clonedComponents.a) {
                if (! (comp instanceof CircuitSlave))
                world.setComponent(comp.getX() + oX, comp.getY() + oY, comp);
            }
            for (var label: clonedComponents.b.entrySet()) {
                var pos = label.getKey();
                world.setLabel(pos[0] + oX, pos[1] + oY, label.getValue());
            }
        }
    }


    private void handleSelectionTransforms(boolean shiftHeld) {
        if (GlobalData.keyBinds.isJustPressed("rotate")) {
            if (shiftHeld) {
                for (var comp: clonedComponents.a) {
                    comp.addRotation(-1);
                    comp.setPos(-comp.getY(), comp.getX());
                }
                for (var label: clonedComponents.b.keySet()) {
                    var tmp = label[1];
                    label[1] = label[0];
                    label[0] = -tmp;
                }
            } else {
                for (var comp : clonedComponents.a) {
                    comp.addRotation(1);
                    comp.setPos(comp.getY(), -comp.getX());
                }
                for (var label: clonedComponents.b.keySet()) {
                    var tmp = label[0];
                    label[0] = label[1];
                    label[1] = -tmp;
                }
            }
        }
        if (GlobalData.keyBinds.isJustPressed("flip")) {
            for (var comp: clonedComponents.a) {
                var rot = comp.getRotation();
                comp.setFlipped(!comp.getFlipped());
                comp.setPos(-comp.getX(), comp.getY());
                if (rot == 0 || rot == 2) {
                    comp.addRotation(2);
                }
            }
            for (var label: clonedComponents.b.keySet()) {
                label[0] = -label[0];
            }
        }
        if (GlobalData.keyBinds.isJustPressed("right")) {
            for (var comp:clonedComponents.a) {
                if (!(comp instanceof CircuitSlave))
                comp.setX(comp.getX() + 1);
            }
            for (var label: clonedComponents.b.keySet()) {
                label[0] += 1;
            }
        }
        if (GlobalData.keyBinds.isJustPressed("left")) {
            for (var comp:clonedComponents.a) {
                if (!(comp instanceof CircuitSlave))
                comp.setX(comp.getX() - 1);
            }
            for (var label: clonedComponents.b.keySet()) {
                label[0] -= 1;
            }
        }
        if (GlobalData.keyBinds.isJustPressed("up")) {
            for (var comp:clonedComponents.a) {
                if (!(comp instanceof CircuitSlave))
                comp.setY(comp.getY() + 1);
            }
            for (var label: clonedComponents.b.keySet()) {
                label[1] += 1;
            }
        }
        if (GlobalData.keyBinds.isJustPressed("down")) {
            for (var comp:clonedComponents.a) {
                if (!(comp instanceof CircuitSlave))
                comp.setY(comp.getY() - 1);
            }
            for (var label: clonedComponents.b.keySet()) {
                label[1] -= 1;
            }
        }
    }

    private void handleCopyPasteInput(boolean shiftHeld) {
        handleSelectionTransforms(shiftHeld);
        if ((!overlapping || shiftHeld) && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (var comp : clonedComponents.a) {
                if (! (comp instanceof CircuitSlave))
                world.cloneComponent(comp.getX() + x, comp.getY() + y, comp.getRotation(), comp.getFlipped(), comp);
            }
            for (var label: clonedComponents.b.entrySet()) {
                var pos = label.getKey();
                world.setLabel(pos[0] + x, pos[1] + y, label.getValue());
            }
        }
        if (GlobalData.keyBinds.isJustPressed("cancel")) {
            state = State.SELECT;
            gui.setGUIMode(GUI.GUIMode.SELECTOR);
        }
    }

    private final MutablePair<List<Component>, Map<int[], String>> clonedComponents = new MutablePair<>(new UnsafeList<>(), new HashMap<>());
    private void cloneSelectionToHover() {
        clonedComponents.a.clear();
        clonedComponents.b.clear();
        int x1, x2, y1, y2;
        x1 = Math.min(positionA.x, positionB.x);
        x2 = Math.max(positionA.x, positionB.x);
        y1 = Math.min(positionA.y, positionB.y);
        y2 = Math.max(positionA.y, positionB.y);
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                var comp = world.getComponent(x, y);
                if (comp != null && !(comp instanceof CircuitSlave)) {
                    var clone = comp.createCloneWithTransform(comp.getX() - x1, comp.getY() - y2, comp.getRotation(), comp.getFlipped(), comp.getAlternativeID());
                    clonedComponents.a.add(clone);
                    if (clone instanceof CircuitMaster) {
                        clonedComponents.a.addAll(((CircuitMaster)clone).slaves);
                    }
                }
                var label = world.getLabel(x, y);
                if (label != null && !label.equals("")) {
                    clonedComponents.b.put(new int[]{x - positionA.x, y - positionA.y}, label);
                }
            }
        }
    }

    private void eraseSelection() {
        int x1, x2, y1, y2;
        x1 = Math.min(positionA.x, positionB.x);
        x2 = Math.max(positionA.x, positionB.x);
        y1 = Math.min(positionA.y, positionB.y);
        y2 = Math.max(positionA.y, positionB.y);
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                world.removeComponent(x, y);
                world.removeLabel(x, y);
            }
        }
    }

    private void handleSelectorInput(boolean shiftHeld) {
        if (GlobalData.keyBinds.isJustPressed("cancel")) {
            state = State.PLACE;
            gui.setGUIMode(GUI.GUIMode.PLACER);
            return;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !clickBlock) {
            positionA.set(x, y);
            gui.setSelectionPoints(positionA, positionB);
        } else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            clickBlock = false;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            positionB.set(x, y);
            gui.setSelectionPoints(positionA, positionB);
        }
        if (GlobalData.keyBinds.isJustPressed("copy")) {
            state = State.COPYPASTE;
            gui.setGUIMode(GUI.GUIMode.COPYPASTE);
            cloneSelectionToHover();
        }
        if (GlobalData.keyBinds.isJustPressed("cut")) {
            state = State.CUTPASE;
            gui.setGUIMode(GUI.GUIMode.CUTPASTE);
            cloneSelectionToHover();
            eraseSelection();
        }
        if (GlobalData.keyBinds.isJustPressed("stack")) {
            clear();
            state = State.STACKPRIME;
            gui.setGUIMode(GUI.GUIMode.STACKPRIME);
        }
        if (GlobalData.keyBinds.isJustPressed("circuit")) {
            var circuitMaster = new CircuitMaster();
            circuitMaster.create(world, positionA, positionB);
            state = State.PLACE;
            gui.setGUIMode(GUI.GUIMode.PLACER);
        }
        if (GlobalData.keyBinds.isJustPressed("export")) {
            gui.postStuckKeyEvent("export", () -> {
                cloneSelectionToHover();
                SaveEngine.saveComponentsToFile(kryo, clonedComponents.a.toArray(Component[]::new), clonedComponents.b);
            });
        }

        if (GlobalData.keyBinds.isJustPressed("import")) {
            gui.postStuckKeyEvent("import", () -> {
                var arr = SaveEngine.loadComponentsFromFile(kryo);
                if (arr != null) {
                    clear();
                    clonedComponents.a.addAll(Arrays.asList(arr.A));
                    clonedComponents.b.putAll(arr.B);
                    state = State.COPYPASTE;
                    gui.setGUIMode(GUI.GUIMode.CUTPASTE);
                }
            });
        }
        if (GlobalData.keyBinds.isJustPressed("delete")) {
            clear();
            eraseSelection();
            state = State.PLACE;
            gui.setGUIMode(GUI.GUIMode.PLACER);
        }
    }

    private void clear() {
        clonedComponents.a.clear();
        clonedComponents.b.clear();
    }

    private enum State {
        PLACE, SELECT, COPYPASTE, CUTPASE, STACKPRIME, CIRCUIT, STACKEXECUTE
    }

    private void handlePlacerInput(boolean shiftHeld) {
        if (GlobalData.keyBinds.isJustPressed("duplicator")) {
            state = State.SELECT;
            gui.setGUIMode(GUI.GUIMode.SELECTOR);
            return;
        }
        if (GlobalData.keyBinds.isJustPressed("scroll_components")) {
            var s = prefabs.size() + 1;
            if (s == 1) return;
            selectComponentInCategory(shiftHeld ? -1 : 1);
        }
        if (GlobalData.keyBinds.isJustPressed("scroll_categories")) {
            var s = prefabs.size() + 1;
            if (s == 1) return;
            snapToCategory(shiftHeld ? -1 : 1);
        }
        if (current != null) {
            if (GlobalData.keyBinds.isJustPressed("scroll_alternatives")) {
                if (shiftHeld)
                    current.prevAlternative();
                else
                    current.nextAlternative();
            }

            if (GlobalData.keyBinds.isJustPressed("flip")) {
                current.setFlipped(!current.getFlipped());
            }

            if (GlobalData.keyBinds.isJustPressed("rotate")) {
                current.addRotation(shiftHeld ? -1 : 1);
            }
        }

        if (GlobalData.keyBinds.isJustPressed("label")) {
            if (shiftHeld) {
                if (!world.getLabel(x, y).equals("")) {
                    world.removeLabel(x, y);
                    GlobalData.soundManager.play("delete");
                }
            } else {
                gui.postStuckKeyEvent("label", () -> {
                    world.setLabel(x, y, JOptionPane.showInputDialog("Enter label text"));
                    GlobalData.soundManager.play("place");
                });
            }
        }

        if (GlobalData.keyBinds.isJustPressed("saveload")) {
            gui.postStuckKeyEvent("saveload", () -> { var promptAlive = new AtomicBoolean(true);
                var frame = new JFrame("ChromaBeam World Save Manager");
                var save = new JButton("Save World");
                var load = new JButton("Load World");
                var clear = new JButton("Clear World");
                var cancel = new JButton("Cancel");
                var runnable = new AtomicReference<Runnable>();
                save.addActionListener((event) -> {
                    runnable.set(() -> {
                        if (SaveEngine.saveComponentsToFile(kryo, world.getAllComponents(), world.getAllLabels())) {
                            GlobalData.soundManager.play("saved");
                        }
                    });
                    promptAlive.set(false);
                });
                load.addActionListener((event) -> {
                    runnable.set(() -> {
                        var comps = SaveEngine.loadComponentsFromFile(kryo);
                        if (comps != null) {
                            world.clear();
                            world.pause();
                            for (var comp : comps.A) {
                                if (! (comp instanceof CircuitSlave))
                                world.setComponent(comp);
                            }
                            world.unpause();
                            for (var label : comps.B.entrySet()) {
                                var pos = label.getKey();
                                world.setLabel(pos[0], pos[1], label.getValue());
                            }
                            GlobalData.soundManager.play("saved");
                        }
                    });
                    promptAlive.set(false);
                });
                clear.addActionListener((event) -> {
                    runnable.set(() -> {
                        world.clear();
                        GlobalData.soundManager.play("saved");
                    });
                    promptAlive.set(false);
                });
                cancel.addActionListener((event) -> promptAlive.set(false));
                frame.add(save);
                frame.add(load);
                frame.add(clear);
                frame.add(cancel);
                frame.setLayout(new GridLayout(1, 4));
                frame.setSize(600, 100);
                var size = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setLocation(size.width / 2 - 300, size.height / 2 - 50);
                frame.setVisible(true);
                while (promptAlive.get() && frame.isVisible()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {}
                }
                frame.setVisible(false);
                frame.dispose();
                if (runnable.get() != null) {
                    runnable.get().run();
                }
            });

        }

        if (!interacting && empty && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            world.cloneComponent(current);
            GlobalData.soundManager.play("place");
        }
        if (!empty && Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            world.removeComponent(x, y);
            GlobalData.soundManager.play("delete");
        }
        if (shiftHeld && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            if (worldComponent == null) {
                setSelectedComponent(0);
            } else {
                var reg = worldComponent.getRegistryName();
                var option = prefabs.stream().filter((prefab) -> prefab.getRegistryName().equals(reg)).findFirst();
                if (option.isPresent()) {
                    var id = prefabs.indexOf(option.get()) + 1;
                    setSelectedComponent(id);
                    if (current != null) {
                        current.setRotation(worldComponent.getRotation());
                        current.setFlipped(worldComponent.getFlipped());
                        current.setAlternative(worldComponent.getAlternativeID());
                    }
                } else {
                    setSelectedComponent(0);
                }
            }
        }
        if (interacting && !empty) {
            var click = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
            if (click && !clicking) {
                current.clickStart();
                clicking = true;
            } else if (!click && clicking) {
                current.clickStop();
                clicking = false;
            }
        }
    }

    private void snapToCategory(int delta) {
        var prevCat = gui.selectedCategory;
        do {
            selectComponent(delta);
        } while (prevCat == gui.selectedCategory);
        if (delta < 0) {
            prevCat = gui.selectedCategory;
            while (prevCat == gui.selectedCategory) {
                selectComponent(delta);
            }
            selectComponent(-delta);
        }
    }

    private void selectComponentInCategory(int delta) {
        var category = gui.selectedCategory;
        selectComponent(delta);
        if (delta < 0 && category != gui.selectedCategory) {
            snapToCategory(1);
            snapToCategory(1);
            selectComponent(-1);
        } else if (delta > 0 && category != gui.selectedCategory) {
            snapToCategory(-1);
        }
    }

    private void selectComponent(int delta) {
        var s = prefabs.size() + 1;
        setSelectedComponent((((selectedComponent + delta) % s) + s) % s);
    }

    private void setSelectedComponent(int newSel) {
        this.selectedComponent = newSel;
        gui.setSelectedComponent(selectedComponent);
        if (selectedComponent == 0) {
            interacting = true;
            current = world.getComponent(x, y);
        } else {
            interacting = false;
            var old = current == null ? selectedComponent == 1 ? prefabs.get(prefabs.size() - 1) : prefabs.get(0) : current;
            current = prefabs.get(selectedComponent - 1);
            current.setPos(old.getX(), old.getY());
        }
    }
}
