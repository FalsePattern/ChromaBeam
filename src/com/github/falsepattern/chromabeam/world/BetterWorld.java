package com.github.falsepattern.chromabeam.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.falsepattern.chromabeam.core.GlobalData;
import com.github.falsepattern.chromabeam.core.SaveEngine;
import com.github.falsepattern.chromabeam.mod.BeamCollision;
import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;
import com.github.falsepattern.chromabeam.mod.interfaces.World;
import com.github.falsepattern.chromabeam.util.Pair;
import com.github.falsepattern.chromabeam.util.storage.*;
import com.github.falsepattern.chromabeam.util.storage.nongeneric.NativeContainer2DIntArray;
import com.github.falsepattern.chromabeam.util.storage.nongeneric.UnsafeComponentList;
import com.github.falsepattern.chromabeam.util.storage.nongeneric.NodeGraphContainer2DComponent;

import java.util.*;

public class BetterWorld implements World {
    private final UnsafeComponentList componentList;
    private final NodeGraphContainer2DComponent componentWorldMap;
    private final BeamDataContainer beamData;
    private final BeamDrawHelper beamDrawHelper;
    private final ComponentDrawHelper mipMappedComponentDrawHelper;
    private final ComponentDrawHelper unMipMappedComponentDrawHelper;
    private final CustomMaskedWorld maskedWorld = new CustomMaskedWorld();
    private final boolean doRendering;
    public BetterWorld() {
        this(false);
    }
    public BetterWorld(boolean doRendering) {
        this.doRendering = doRendering;
        componentList = new UnsafeComponentList();
        componentWorldMap = new NodeGraphContainer2DComponent();
        beamData = new BeamDataContainer();
        if (doRendering) {
            beamDrawHelper = new BeamDrawHelper();
            mipMappedComponentDrawHelper = new ComponentDrawHelper();
            unMipMappedComponentDrawHelper = new ComponentDrawHelper();
        } else {
            beamDrawHelper = null;
            mipMappedComponentDrawHelper = null;
            unMipMappedComponentDrawHelper = null;
        }
        labels = new HashMap<>();
        labelsWorldMap = new NodeGraphContainer2D<>();
    }

    public synchronized void emitInitialBeams() {
        beamData.clear();
        var s = componentList.size() - 1;
        var st = componentList.storage;
        for (; s >= 0; s--) {
            var component = (Component)st[s];
            maskedWorld.prepare(component);
            component.emitInitialBeams(maskedWorld);
        }
    }

    public synchronized void resolveBeams() {
        int count;
        while (beamData.newData) {
            beamData.flip();
            while ((count = beamData.getBeamCount()) > 0) {
                for (int i = 0; i < count; i++) {
                    var data = beamData.popBeam();
                    int color = data[3];
                    if (color == 0) continue;
                    int x = data[0];
                    int y = data[1];
                    int rotation = data[2];
                    var ret = BeamCollision.PASS;
                    Pair<int[], Component> result = componentWorldMap.getNextExisting(x, y, rotation);
                    while (true) {
                        if (result == null) {
                            scheduleEndlessBeamDraw(x, y, rotation, color);
                            break;
                        } else {
                            maskedWorld.select(result.getB());
                            if ((maskedWorld.colorMap[rotation + 4] & color) != color) {
                                ret = result.getB().processIncomingBeam(rotation, color, maskedWorld);
                                maskedWorld.colorMap[rotation + 4] |= color;
                                if (ret != BeamCollision.PASS) {
                                    scheduleBeamDraw(x, y, rotation, Math.abs(rotation % 2 == 0 ? result.getB().getX() - x : result.getB().getY() - y), color, ret);
                                    break;
                                } else {
                                    maskedWorld.colorMap[rotation] |= color;
                                }
                            } else {
                                scheduleBeamDraw(x, y, rotation, Math.abs(rotation % 2 == 0 ? result.getB().getX() - x : result.getB().getY() - y), color, BeamCollision.EDGE);
                                break;
                            }
                        }
                        result = componentWorldMap.getNextExisting(result.getA()[0], result.getA()[1], rotation);
                    }
                }
            }
        }
    }

    public synchronized void beamUpdate() {
        emitInitialBeams();
        if (doRendering) {
            beamDrawHelper.clearBackBuffer();
            resolveBeams();
            beamDrawHelper.finishBackBuffer();
        } else {
            resolveBeams();
        }
    }

    public synchronized void componentUpdate() {
        if (doRendering) {
            mipMappedComponentDrawHelper.clearBackBuffer();
            unMipMappedComponentDrawHelper.clearBackBuffer();
            var s = componentList.size() - 1;
            var st = componentList.storage;
            for (; s >= 0; s--) {
                var component = (Component) st[s];
                component.componentUpdate();
                if (component.isMipMapped())
                    mipMappedComponentDrawHelper.addToBackBuffer(component);
                else
                    unMipMappedComponentDrawHelper.addToBackBuffer(component);
            }
            mipMappedComponentDrawHelper.finishBackBuffer();
            unMipMappedComponentDrawHelper.finishBackBuffer();
        } else {
            var s = componentList.size() - 1;
            var st = componentList.storage;
            for (; s >= 0; s--) {
                var component = (Component) st[s];
                component.componentUpdate();
            }
        }
    }

    @Override
    public void createBeam(int x, int y, int rotation, int color) {
            if (color != 0)
                beamData.pushBeam(x, y, rotation, color);
    }

    @Override
    public synchronized void update() {
        if (paused) return;
        beamUpdate();
        componentUpdate();
    }

    @Override
    public Component getComponent(int x, int y) {
        return componentWorldMap.get(x, y);
    }

    @Override
    public synchronized Component removeComponent(int x, int y) {
            var component = componentWorldMap.remove(x, y);
            if (component != null) {
                componentList.remove(component);
                component.removedFromWorld();
            }
            return component;
    }

    @Override
    public synchronized Component[] getAllComponents() {
            return componentList.toArray();
    }

    @Override
    public synchronized void clear() {
            componentList.clear();
            componentWorldMap.clear();
            labels.clear();
            labelsWorldMap.clear();
    }

    private final Map<int[], String> labels;
    private final Container2D<String> labelsWorldMap;

    @Override
    public void setLabel(int x, int y, String text) {
        if (text == null || text.equals("")) {
            removeLabel(x, y);
            return;
        }
        if (!text.equals(labelsWorldMap.get(x, y))) {
            labelsWorldMap.set(x, y, text);
            labels.put(new int[]{x, y}, text);
        }
    }

    @Override
    public String getLabel(int x, int y) {
        var result = labelsWorldMap.get(x, y);
        if (result == null) return "";
        return result;
    }

    @Override
    public void removeLabel(int x, int y) {
        var text = labelsWorldMap.remove(x, y);
        if (text != null) {
            int[] target = null;
            for (var entry: labels.entrySet()) {
                var pos = entry.getKey();
                if (pos[0] == x && pos[1] == y) {
                    target = pos;
                    break;
                }
            }
            if (target != null) {
                labels.remove(target);
            }
        }
    }

    @Override
    public Map<int[], String> getAllLabels() {
        return Collections.unmodifiableMap(labels);
    }

    @Override
    public void drawLabels(SpriteBatch batch, OrthographicCamera camera, BitmapFont font) {
        var buf = new Vector3();
        for (var entry: labels.entrySet()) {
            var pos = entry.getKey();
            buf.set(pos[0], pos[1], 0);
            buf.add(0.5f, 0.5f, 0);
            worldCamera.project(buf);
            font.draw(batch, entry.getValue(), buf.x, Gdx.graphics.getHeight() - buf.y);
        }
    }

    @Override
    public synchronized Component cloneComponent(int x, int y, int rotation, boolean flipped, Component original) {
        var component = original.createCloneWithTransform(x, y, rotation, flipped, original.getAlternativeID());
            maskedWorld.add(x, y);
            var old = componentWorldMap.set(x, y, component);
            if (old != null) {
                old.removedFromWorld();
            }
            var iOld = componentList.indexOf(old);
            if (iOld > -1) {
                componentList.set(iOld, component);
            } else {
                componentList.add(component);
            }
            component.placedIntoWorld(this);
            return component;
    }

    @Override
    public synchronized Component setComponent(Component component) {
        return setComponentRaw(component.getX(), component.getY(), component);
    }

    private volatile boolean paused = false;
    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void unpause() {
        paused = false;
    }

    @Override
    public synchronized Component setComponent(int x, int y, Component component) {
            component.setPos(x, y);
            return setComponentRaw(x, y, component);
    }

    private Component setComponentRaw(int x, int y, Component component) {
        if (component != null) {
            componentList.add(component);
            maskedWorld.add(x, y);
            var old = componentWorldMap.set(x, y, component);
            component.placedIntoWorld(this);
            if (old != null) {
                componentList.remove(old);
                old.removedFromWorld();
            }
            return old;
        } else {
            return removeComponent(x, y);
        }
    }

    private OrthographicCamera worldCamera;
    @Override
    public void drawComponents(SpriteBatch batch, OrthographicCamera camera) {
        if (doRendering) {
            unMipMappedComponentDrawHelper.draw(batch, camera);
            batch.flush();
            GlobalData.textureManager.texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
            mipMappedComponentDrawHelper.draw(batch, camera);
            batch.flush();
            GlobalData.textureManager.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            worldCamera = camera;
        }
    }

    @Override
    public void drawBeams(SpriteBatch batch, OrthographicCamera camera) {
        if (doRendering)
        beamDrawHelper.draw(batch, camera);
    }

    @Override
    public synchronized void write(Kryo kryo, Output output) {
        SaveEngine.serializeComponents(kryo, output, componentList.toArray());
    }

    @Override
    public synchronized void read(Kryo kryo, Input input) {
        var inputs = SaveEngine.deserializeComponents(kryo, input);
        for (int i = 0; i < inputs.length; i++) {
            setComponent(inputs[i]);
        }
    }

    private void scheduleBeamDraw(int x, int y, int rotation, int length, int color, BeamCollision collision) {
        if (doRendering) {
            beamDrawHelper.addToBackBuffer(x, y, rotation, length, color, switch (collision) {
                case EDGE -> 1;
                case CENTER -> 0;
                case PASS -> throw new IllegalArgumentException();
            });
        }
    }

    private void scheduleEndlessBeamDraw(int x, int y, int rotation, int color) {
        if (doRendering) {
            beamDrawHelper.addToBackBuffer(x, y, rotation, Integer.MAX_VALUE, color, 1);
        }
    }

    private class CustomMaskedWorld implements MaskedWorld {
        private int x;
        private int y;
        private int rotation;
        private int flipped;
        private final NativeContainer2DIntArray componentColors = new NativeContainer2DIntArray();

        private int[] colorMap;
        private final int[] empty = new int[8];
        private void prepare(Component component) {
            x = component.getX();
            y = component.getY();
            rotation = component.getRotation();
            flipped = component.getFlipped() ? -1 : 1;
            colorMap = componentColors.get(x, y);
            System.arraycopy(empty, 0, colorMap, 0, 8);
        }

        private void add(int x, int y) {
            componentColors.set(x, y, new int[8]);
        }

        private void select(Component component) {
            x = component.getX();
            y = component.getY();
            rotation = component.getRotation();
            flipped = component.getFlipped() ? -1 : 1;
            colorMap = componentColors.get(x, y);
        }

        @Override
        public void createBeam(int rotation, int color) {
            if ((colorMap[rotation] & color) != color) {
                colorMap[rotation] |= color;
                BetterWorld.this.createBeam(x, y, rotation, color);
            }
        }

        private Component getRelative(int rx, int ry) {
            return BetterWorld.this.getComponent(x + rx, y + ry);
        }

        @Override
        public Component getNeighborFront() {
            return getRelative(switch (rotation) {
                default -> 0;
                case 0, 2 -> 1 - rotation;
            }, switch (rotation) {
                default -> 0;
                case 1, 3 -> 1 - (rotation / 2) * 2;
            });
        }

        @Override
        public Component getNeighborRight() {
            return getRelative(switch (rotation) {
                default -> 0;
                case 1, 3 -> (1 - (rotation / 2) * 2) * -flipped;
            },switch (rotation) {
                default -> 0;
                case 0, 2 -> (1 - rotation) * -flipped;
            });
        }

        @Override
        public Component getNeighborBack() {
            return getRelative(switch (rotation) {
                default -> 0;
                case 0, 2 -> rotation - 1;
            }, switch (rotation) {
                default -> 0;
                case 1, 3 -> (rotation / 2) * 2 - 1;
            });
        }

        @Override
        public Component getNeighborLeft() {
            return getRelative(switch (rotation) {
                default -> 0;
                case 1, 3 -> (1 - (rotation / 2) * 2) * flipped;
            },switch (rotation) {
                default -> 0;
                case 0, 2 -> (1 - rotation) * flipped;
            });
        }
    }
}
