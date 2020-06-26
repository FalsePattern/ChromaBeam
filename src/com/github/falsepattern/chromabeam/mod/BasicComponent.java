package com.github.falsepattern.chromabeam.mod;

import com.github.falsepattern.chromabeam.mod.interfaces.MaskedWorld;
import com.github.falsepattern.chromabeam.mod.interfaces.World;
import com.github.falsepattern.chromabeam.util.serialization.Deserializer;
import com.github.falsepattern.chromabeam.util.serialization.Serializer;

import java.io.OutputStream;
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
    public BasicComponent(int alternativeCount, String registryName, String category) {
        super(alternativeCount, registryName, category);
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
    protected void serializeCustomData(Serializer output) {}
    @Override
    protected void deserializeCustomData(Deserializer input) {}

    @Override
    public void clickStart() {

    }

    @Override
    public void clickStop() {

    }

    @Override
    protected void setup() {}
}
