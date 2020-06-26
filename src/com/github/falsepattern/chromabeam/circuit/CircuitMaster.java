package com.github.falsepattern.chromabeam.circuit;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import com.github.falsepattern.chromabeam.core.GlobalData;
import com.github.falsepattern.chromabeam.core.SaveEngine;
import com.github.falsepattern.chromabeam.mod.BasicComponent;
import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;
import com.github.falsepattern.chromabeam.mod.interfaces.World;
import com.github.falsepattern.chromabeam.util.Vector2i;
import com.github.falsepattern.chromabeam.util.serialization.Deserializer;
import com.github.falsepattern.chromabeam.util.serialization.Serializer;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;
import com.github.falsepattern.chromabeam.world.BetterWorld;

import java.util.*;
import java.util.function.Function;

public class CircuitMaster extends BasicComponent {
    private final IntMap<CircuitIOPort> parentPorts = new IntMap<>();
    private final IntMap<CircuitIOPortVirtual> childPorts = new IntMap<>();
    public UnsafeList<Component> slaves = new UnsafeList<>();
    private World childWorld;
    private World parentWorld;
    private int width;
    private int height;
    private boolean deleting = false;
    private boolean unBuilt = true;
    private int rawArea = 0;
    private int[] storedLabelPositions = new int[0];
    private String[] storedLabels = new String[0];
    public CircuitMaster() {
        super(1, "circuit.body", "circuit");
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    public void create(World parentWorld, Vector2i posA, Vector2i posB) {
        var labelStorePosBuilder = new UnsafeList<Integer>();
        var labelStoreTextBuilder = new UnsafeList<String>();
        unBuilt = false;
        int channel = 0;
        UnsafeList<UnsafeList<CircuitIOPort>> ports = new UnsafeList<>();
        for (int i = 0; i < 4; i++) {
            ports.add(new UnsafeList<>());
        }
        childWorld = new BetterWorld();
        this.parentWorld = parentWorld;
        int x1 = Math.min(posA.x, posB.x);
        int x2 = Math.max(posA.x, posB.x);
        int y1 = Math.min(posA.y, posB.y);
        int y2 = Math.max(posA.y, posB.y);
        for (int y = y2; y >= y1; y--) {
            for (int x = x1; x <= x2; x++) {
                var comp = parentWorld.removeComponent(x, y);
                if (!(comp == null || comp instanceof CircuitSlave)) {
                    if (comp instanceof CircuitMaster) {
                        var master = (CircuitMaster) comp;
                        master.parentWorld = childWorld;
                        rawArea += master.rawArea;
                    } else if (comp instanceof CircuitIOPortVirtual) {
                        var vp = (CircuitIOPortVirtual) comp;
                        var rp = new CircuitIOPort();
                        rp.setRotation((vp.getRotation() + 2) % 4);
                        rp.linkID = vp.linkID = channel;
                        rp.master = vp.master = this;
                        slaves.add(rp);
                        childPorts.put(channel, vp);
                        parentPorts.put(channel, rp);
                        ports.get(rp.getRotation()).add(rp);
                        channel++;
                    } else {
                        rawArea++;
                    }
                    childWorld.setComponent(comp);
                }
                var label = parentWorld.getLabel(x, y);
                if (!(label == null || label.trim().equals(""))) {
                    labelStorePosBuilder.add(x);
                    labelStorePosBuilder.add(y);
                    labelStoreTextBuilder.add(label);
                }
                parentWorld.removeLabel(x, y);
            }
        }
        setupParentWorldChip(x1, y2, ports, rawArea);
        storedLabelPositions = new int[labelStorePosBuilder.size()];
        var st = labelStorePosBuilder.storage;
        for (int i = 0; i < storedLabelPositions.length; i++) {
            storedLabelPositions[i] = (int)st[i];
        }
        storedLabels = new String[labelStoreTextBuilder.size()];
        System.arraycopy(labelStoreTextBuilder.storage, 0, storedLabels, 0, storedLabels.length);
    }

    @Override
    protected void cloneDataFromOriginal(Component original) {
        var origMaster = (CircuitMaster) original;
        var oX = origMaster.getX();
        var oY = origMaster.getY();
        parentWorld = origMaster.parentWorld;
        for (var slave: origMaster.slaves) {
            Component slaveClone;
            if (origMaster.unBuilt) {
                slaveClone = slave.createClone();
            } else {
                slaveClone = slave.createCloneWithTransform(slave.getX() - oX, slave.getY() - oY, slave.getRotation(), slave.getFlipped(), slave.getAlternativeID());
            }
            if (slaveClone instanceof CircuitSlave) {
                ((CircuitSlave) slaveClone).master = this;
            }
            if (slaveClone instanceof CircuitIOPort) {
                parentPorts.put(((CircuitIOPort) slaveClone).linkID, (CircuitIOPort)slaveClone);
            }
            slaves.add(slaveClone);
        }
        var origComps = origMaster.childWorld.getAllComponents();
        childWorld = new BetterWorld();
        for (var origComp: origComps) {
            if (origComp instanceof CircuitSlave) continue;
            var clone = origComp.createClone();
            if (clone instanceof CircuitIOPortVirtual) {
                childPorts.put(((CircuitIOPortVirtual) clone).linkID, (CircuitIOPortVirtual) clone);
                ((CircuitIOPortVirtual) clone).master = this;
            }
            childWorld.setComponent(clone);
        }
        width = origMaster.width;
        height = origMaster.height;
        rawArea = origMaster.rawArea;
        storedLabelPositions = new int[origMaster.storedLabelPositions.length];
        storedLabels = new String[origMaster.storedLabels.length];
        System.arraycopy(origMaster.storedLabelPositions, 0, storedLabelPositions, 0, storedLabelPositions.length);
        System.arraycopy(origMaster.storedLabels, 0, storedLabels, 0, storedLabels.length);
    }

    @Override
    public void placedIntoWorld(World world) {
        if (unBuilt) {
            parentWorld = world;
            for (var slave: slaves) {
                world.setComponent(x + slave.getX(), y + slave.getY(), slave);
            }
            unBuilt = false;
        }
    }

    private void setupParentWorldChip(int xPos, int yPos, List<? extends List<CircuitIOPort>> ports, int minimumArea) {
        width = Math.max(ports.get(1).size(), ports.get(3).size()) + 2;
        height = Math.max(ports.get(0).size(), ports.get(2).size()) + 2;
        while (((width - 2) * (height - 2) * 8) < minimumArea) {
            if (height < width) height++; else width++;
        }
        setupCornerAndFill(xPos, yPos, width, height);
        placePorts(ports.get(0), height - 2, (i) -> xPos, (i) -> yPos - 1 - i);
        placePorts(ports.get(1), width - 2, (i) -> xPos + 1 + i, (i) -> yPos);
        placePorts(ports.get(2), height - 2, (i) -> xPos + width - 1, (i) -> yPos - 1 - i);
        placePorts(ports.get(3), width - 2, (i) -> xPos + 1 + i, (i) -> yPos - height + 1);
    }

    private void placePorts(List<CircuitIOPort> ioPorts, int paddableSize, Function<Integer, Integer> xPosFunction, Function<Integer, Integer> yPosFunction) {
        for (int i = 0; i < ioPorts.size(); i++) {
            var port = ioPorts.get(i);
            parentWorld.setComponent(xPosFunction.apply(i), yPosFunction.apply(i), port);
        }
        for (int i = ioPorts.size(); i < paddableSize; i++) {
            var filler = new CircuitBody();
            filler.master = this;
            slaves.add(filler);
            parentWorld.setComponent(xPosFunction.apply(i), yPosFunction.apply(i), filler);
        }
    }

    private void setupCornerAndFill(int xPos, int yPos, int width, int height) {
        for (int rY = 1; rY < height - 1; rY++) {
            for (int rX = 1; rX < width - 1; rX++) {
                var cell = new CircuitBody();
                cell.master = this;
                slaves.add(cell);
                parentWorld.setComponent(xPos + rX, yPos - rY, cell);
            }
        }
        var corners = new CircuitBody[]{new CircuitBody(), new CircuitBody(), new CircuitBody()};
        parentWorld.setComponent(xPos, yPos, this);
        parentWorld.setComponent(xPos + width - 1, yPos, corners[0]);
        parentWorld.setComponent(xPos + width - 1, yPos - height + 1, corners[1]);
        parentWorld.setComponent(xPos, yPos - height + 1, corners[2]);
        for (var corner: corners) {
            corner.master = this;
            slaves.add(corner);
        }
    }

    @Override
    public void componentUpdate() {
        childWorld.componentUpdate();
    }

    @Override
    public void removedFromWorld() {
        deleting = true;
        for (var slave: slaves) {
            parentWorld.removeComponent(slave.getX(), slave.getY());
            slave.setPos(slave.getX() - x, slave.getY() - y);
        }
        deleting = false;
        unBuilt = true;
    }


    void undelete(Component component) {
        if (!deleting) {
            parentWorld.setComponent(component);
        }
    }

    private void findVirtualPorts() {
        var components = childWorld.getAllComponents();
        for (int i = 0; i < components.length; i++) {
            var comp = components[i];
            if (comp instanceof CircuitIOPortVirtual) {
                childPorts.put(((CircuitIOPortVirtual) comp).linkID, (CircuitIOPortVirtual)comp);
                ((CircuitIOPortVirtual) comp).master = this;
            }
        }
    }

    public void passBeamToParent(int color, int linkID) {
        var port = parentPorts.get(linkID);
        parentWorld.createBeam(port.getX(), port.getY(), (port.getRotation() + 2) % 4, color);
    }

    public void passBeamToChild(int color, int linkID) {
        var port = childPorts.get(linkID);
        childWorld.createBeam(port.getX(), port.getY(), (port.getRotation() + 2) % 4, color);
        childWorld.resolveBeams();
    }

    @Override
    public void emitInitialBeams(MaskedWorld world) {
        childWorld.emitInitialBeams();
        childWorld.resolveBeams();
    }


    @Override
    protected void serializeCustomData(Serializer output) {
        SaveEngine.serializeComponents(output, childWorld.getAllComponents());
        output.writeInt(width);
        output.writeInt(height);
        output.writeInt(rawArea);
        output.writeInt(slaves.size());
        for (var slave: slaves) {
            var cln = slave.createClone();
            cln.setPos(slave.getX() - x, slave.getY() - y);
            output.writeObject(cln);
        }
        output.writeInt(storedLabels.length);
        output.writeInts(storedLabelPositions, 0, storedLabelPositions.length);
        for (int i = 0; i < storedLabels.length; i++) {
            output.writeAsciiString(storedLabels[i]);
        }
    }

    @Override
    protected void deserializeCustomData(Deserializer input) {
        childWorld = new BetterWorld();
        var components = SaveEngine.deserializeComponents(input);
        for (int i = 0; i < components.length; i++) {
            childWorld.setComponent(components[i]);
        }
        width = input.readInt();
        height = input.readInt();
        rawArea = input.readInt();
        int slaveCount = input.readInt();
        for (int i = 0; i < slaveCount; i++) {
            var slave = (CircuitSlave)input.readObject();
            slave.master = this;
            slaves.add(slave);
            if (slave instanceof CircuitIOPort) {
                var port = (CircuitIOPort) slave;
                parentPorts.put(port.linkID, port);
            }
        }
        storedLabels = new String[input.readInt()];
        storedLabelPositions = input.readInts(storedLabels.length * 2);
        for (int i = 0; i < storedLabels.length; i++) {
            storedLabels[i] = input.readString();
        }
        findVirtualPorts();
    }

    public void exportChildWorld() {
        var labelMap = new HashMap<int[], String>();
        for (int i = 0; i < storedLabels.length; i++) {
            labelMap.put(new int[]{storedLabelPositions[i * 2], storedLabelPositions[i * 2 + 1]}, storedLabels[i]);
        }
        if (SaveEngine.saveComponentsToFile(childWorld.getAllComponents(), labelMap)) {
            GlobalData.soundManager.play("saved");
        }
    }

    @Override
    public boolean isMipMapped() {
        return false;
    }

    @Override
    public TextureRegion getTexture() {
        return texture[1];
    }
}
