package com.github.falsepattern.chromabeam.mod;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.falsepattern.chromabeam.core.GlobalData;
import com.github.falsepattern.chromabeam.mod.interfaces.World;
import com.github.falsepattern.chromabeam.util.Color;
import com.github.falsepattern.chromabeam.util.Vector2i;
import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * The class representing components in the world. You should probably extend {@link BasicComponent}.
 */
public abstract class Component implements KryoSerializable {

    /**
     * Called at the start of every tick. Initial beam emissions by components are done in this phase.
     * @param world A masked world instance, where the component can only create beams at it's position
     */
    public abstract void emitInitialBeams(MaskedWorld world);

    /**
     * Called after every beam has been calculated. Components do internal state changes and prepare for the next tick
     */
    public abstract void componentUpdate();

    /**
     * Called when the component is removed from world space.
     */
    public abstract void removedFromWorld();

    /**
     * Called when the component is put into world space.
     */
    public abstract void placedIntoWorld(World world);

    /**
     * Called when a beam hits the component. Multiple beams can arrive from the same direction during the same tick,
     * so the component needs to track these changes internally.
     * @param beamHeading The direction the beam is going towards
     * @param color The color of the beam
     * @param world A masked world instance, where the component can only create beams at it's position
     * @return The way the incoming beam collided with the component
     */
    public abstract BeamCollision processIncomingBeam(int beamHeading, int color, MaskedWorld world);

    /**
     * When a component is cloned, the component can copy it's custom data inside this method.
     * @param original The component to clone the data from.
     */
    protected abstract void cloneDataFromOriginal(Component original);

    /**
     * Called after initializeDataFromPrefab and deserializeCustomData. The component can prepare it's internal state
     * for it's first live tick.
     */
    protected abstract void setup();

    /**
     * Called after the basic properties of the component have been loaded from the save. Components must load values
     * they saved here.
     * @param kryo The kryo deserializer instance.
     * @param input The Kryo input stream the save is being loaded from.
     */
    protected abstract void deserializeCustomData(Kryo kryo, Input input);

    /**
     * Called after the basic properties of the component have been saved to disk. Components must save custom data
     * information here.
     * @param kryo The kryo serializer instance.
     * @param output The Kryo input stream the save is being saved to.
     */
    protected abstract void serializeCustomData(Kryo kryo, Output output);

    /**
     * Create a new, unitialized instance of the current component.
     * @return A not yet initialized instance.
     */
    protected abstract Component cloneComponent();



    /**
     * The x coordinate of the object in world space.
     */
    protected int x;

    /**
     * The vertical coordinate of the object in world space.
     */
    protected int y;

    /**
     * The direction of the object's forward direction.
     */
    protected int rotation;

    /**
     * True if the object's left and right sides are swapped.
     */
    protected boolean flipped = false;

    /**
     * The direction towards the object's front.
     */
    protected int forward;

    /**
     * The direction towards the object's rear.
     */
    protected int backward;

    /**
     * The direction towards the object's left. Affected by mirroring.
     */
    protected int left;

    /**
     * The direction towards the object's right. Affected by mirroring.
     */
    protected int right;

    /**
     * The number of alternative prefab types the component has.
     */
    protected final int alternativeCount;

    /**
     * The index of the component's alternative type.
     */
    protected int alternativeID = 0;

    /**
     * The texture(s) matching the object's registration name.
     */
    protected TextureRegion[] texture;

    private void setTexture() {
        texture = GlobalData.textureManager.getMultiTexture(registryName);
        if (texture == null) throw new RuntimeException("No registered texture for " + registryName);
    }


    private boolean isPrefab;

    private String registryName;

    /**
     * Create a new component with the specified parameters. The component must not do initialization in it's constructor!
     * @param alternativeCount The amount of alternative types this component can have. This is used when the user
     *                         toggles between the placeable alternatives of this component. If unsure, set to 1.
     * @param registryName The name this component should be registered as. Must be the same as the component's
     *                     texture's registration name.
     */
    public Component(int alternativeCount, String registryName) {
        this.alternativeCount = alternativeCount;
        this.registryName = registryName;
        setTexture();
        this.isPrefab = true;
    }


    /**
     * Returns true if this component is not part of any world.
     */
    public boolean isPrefab() {
        return isPrefab;
    }

    /**
     * @return The texture that represents the component in it's current state.
     */
    public TextureRegion getTexture() {
        return texture[alternativeID];
    }

    /**
     * @return Whether the renderer should use smooth, mipMapped graphics for this component.
     */
    public boolean isMipMapped() {return true;}

    /**
     * @return the component's name, which was specified during initialization.
     */
    public final String getRegistryName() {
        return registryName;
    }

    /**
     * @return The total amount of alternative states this components can have.
     */
    public int getAlternativeCount() {
        return alternativeCount;
    }

    /**
     * @return The horizontal position of the component.
     */
    public int getX() {
        return x;
    }

    /**
     * @return The vertical position of the component.
     */
    public int getY() {
        return y;
    }

    /**
     * @return The position of the component represented in a 2D vector.
     */
    public Vector2i getPos() {
        return new Vector2i(x, y);
    }

    /**
     * @return The forward direction of the component.
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * @return true if the component's left and right sides are swapped.
     */
    public boolean getFlipped() {
        return flipped;
    }

    /**
     * @param x The component's desired horizontal position.
     */
    public void setX(int x) {
        this.x = x;
        refreshTransform();
    }

    /**
     * @param y The component's desired vertical position.
     */
    public void setY(int y) {
        this.y = y;
        refreshTransform();
    }

    /**
     * @param x The component's desired horizontal position.
     * @param y The component's desired vertical position.
     */
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        refreshTransform();
    }

    /**
     * @param rotation The component's desired amount of clockwise 90 degree rotations. 0 is to the right.
     */
    public void setRotation(int rotation) {
        this.rotation = rotation;
        refreshTransform();
    }

    /**
     * Rotates the component clockwise with the specified amount. Negative numbers rotate counter-clockwise. 0 is a no-op.
     * @param delta The amount to rotate the component by.
     */
    public void addRotation(int delta) {
        if (delta == 0)return;
        this.rotation += delta;
        refreshTransform();
    }

    /**
     * Rotates the component clockwise 90 degrees.
     */
    public void rotateCW() {
        rotation++;
        refreshTransform();
    }

    /**
     * Rotates the component counter-clockwise 90 degrees.
     */
    public void rotateCCW() {
        rotation--;
        refreshTransform();
    }

    /**
     * @param flipped Whether the component's left and right sides are swapped.
     */
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        refreshTransform();
    }

    /**
     * Sets the position and rotation of the component.
     * @param x The horizontal position to set.
     * @param y The vertical position to set.
     * @param rotation The rotation to set.
     */
    public void setTransform(int x, int y, int rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        refreshTransform();
    }

    /**
     * Sets the position, rotation and flipped state of the component.
     * @param x The horizontal position to set.
     * @param y The vertical position to set.
     * @param rotation The rotation to set.
     * @param flipped Whether the component's left and right sides are swapped.
     */
    public void setTransform(int x, int y, int rotation, boolean flipped) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.flipped = flipped;
        refreshTransform();
    }

    /**
     * @param altID The component's desired alternative id.
     */
    public void setAlternative(int altID) {
        if (altID < 0 || altID >= getAlternativeCount()) {
            throw new RuntimeException("Invalid alternative ID " + altID + " for component " + registryName);
        } else {
            alternativeID = altID;
        }
    }

    /**
     * @return The index of the component's currently active alternative type.
     */
    public int getAlternativeID() {
        return alternativeID;
    }

    /**
     * Increments the component's alternative index by 1. If this increment would go higher than the highest alternative
     * index, then the index is set to 0.
     */
    public void nextAlternative() {
        if (alternativeID == getAlternativeCount() - 1) {
            setAlternative(0);
        } else {
            setAlternative(alternativeID + 1);
        }
    }
    /**
     * Decrements the component's alternative index by 1. If this decrement would go lower than 0, then the index is set
     * to the highest alternative index.
     */
    public void prevAlternative() {
        if (alternativeID == 0) {
            setAlternative(getAlternativeCount() - 1);
        } else {
            setAlternative(alternativeID - 1);
        }
    }

    /**
     * Called when the player left-clicks on the component. Not called when placing.
     */
    public abstract void clickStart();

    /**
     * Called when the player stops left-clicking on the component. Not called when placing.
     */
    public abstract void clickStop();

    /**
     * @return the color that this component should get drawn with. See {@link Color} for more information on the color
     *         indexing.
     */
    public int getDrawColor() {
        return Color.WHITE;
    }

    /**
     * Called whenever any of the position/rotation/mirroring changing functions have been called. Recalculates the
     * directions of the sides of the component.
     */
    protected void refreshTransform(){
        rotation = ((rotation % 4) + 4) % 4;
        forward = rotation;
        backward = rotation < 2 ? rotation + 2 : rotation - 2;
        var baseLeft = rotation == 0 ? 3 : rotation - 1;
        var baseRight = rotation == 3 ? 0 : rotation + 1;
        left = flipped ? baseRight : baseLeft;
        right = flipped ? baseLeft : baseRight;
    }

    private void initWithData(int x, int y, int rotation, boolean flipped, int alternativeID, String registryName) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.flipped = flipped;
        this.alternativeID = alternativeID;
        this.registryName = registryName;
        setTexture();
        this.isPrefab = false;
        refreshTransform();
    }

    /**
     * ---INTERNAL METHOD---
     * @param x The horizontal position of the clone.
     * @param y The vertical position of the clone.
     * @param rotation The rotation of the clone.
     * @param flipped The flipped state of the clone.
     * @param alternativeID The alternative id of the clone.
     */
    public final Component createCloneWithTransform(int x, int y, int rotation, boolean flipped, int alternativeID) {
        var result = cloneComponent();
        result.initWithData(x, y, rotation, flipped, alternativeID, registryName);
        result.cloneDataFromOriginal(this);
        result.setup();
        return result;
    }

    public final Component createClone() {
        return createCloneWithTransform(getX(), getY(), getRotation(), getFlipped(), getAlternativeID());
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(x);
        output.writeInt(y);
        output.writeInt(rotation);
        output.writeInt(alternativeID);
        output.writeBoolean(flipped);
        var customOutput = new Output(new ByteArrayOutputStream());
        serializeCustomData(kryo, customOutput);
        byte[] serializedData = customOutput.toBytes();
        output.writeInt(serializedData.length);
        output.writeBytes(serializedData);

    }

    @Override
    public void read(Kryo kryo, Input input) {
        x = input.readInt();
        y = input.readInt();
        rotation = input.readInt();
        alternativeID = input.readInt();
        flipped = input.readBoolean();
        isPrefab = false;
        refreshTransform();
        var coreVer = GlobalData.modsInLoadedSave.get("core");
        if (!(coreVer.equals("0.3.0") || coreVer.equals("0.3.1"))) {
            int customDataLength = input.readInt();
            byte[] customData = input.readBytes(customDataLength);
            input = new Input(new ByteArrayInputStream(customData));
        }
        deserializeCustomData(kryo, input);
        setup();
    }
}
