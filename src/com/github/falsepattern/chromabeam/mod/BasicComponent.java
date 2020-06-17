package com.github.falsepattern.chromabeam.mod;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;
import com.github.falsepattern.chromabeam.mod.interfaces.World;

import java.lang.reflect.InvocationTargetException;

/**
 * A class implementing most abstract methods in {@link Component} to reduce boilerplate in actual components.
 */
public abstract class BasicComponent extends Component{

    /**
     * Mod components must use this initializer. The <code>registryName</code> defines the global name of the component.
     * It should be in the format of "developername.modname.componentname" or "modname.componentname" for consistency.
     * AlternativeCount defines how many alternative types the prefab of this component has.
     */
    public BasicComponent(int alternativeCount, String registryName) {
        super(alternativeCount, registryName);
    }

    /**
     * Create a new un-initialized clone of the current component. Most components should not override this.
     * @return A not yet initialized component.
     */
    @Override
    protected Component cloneComponent() {
        try {
            return ((Class<? extends Component>)getClass()).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void emitInitialBeams(MaskedWorld world) {}
    @Override
    public void componentUpdate() {}
    @Override
    public void removedFromWorld(){}
    @Override
    public void placedIntoWorld(World world) {}
    @Override
    public BeamCollision processIncomingBeam(int beamHeading, int color, MaskedWorld world) {return BeamCollision.EDGE;}
    @Override
    protected void cloneDataFromOriginal(Component original) {}
    @Override
    protected void serializeCustomData(Kryo kryo, Output output) {}
    @Override
    protected void deserializeCustomData(Kryo kryo, Input input) {}

    @Override
    public void clickStart() {

    }

    @Override
    public void clickStop() {

    }

    @Override
    protected void setup() {}
}
