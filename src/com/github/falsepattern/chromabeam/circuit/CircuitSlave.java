package com.github.falsepattern.chromabeam.circuit;

import com.github.falsepattern.chromabeam.mod.BasicComponent;

public abstract class CircuitSlave extends BasicComponent {

    public CircuitMaster master;
    /**
     * Mod components must use this initializer. The <code>registryName</code> defines the global name of the component.
     * It should be in the format of "developername.modname.componentname" or "modname.componentname" for consistency.
     * AlternativeCount defines how many alternative types the prefab of this component has.
     *
     * @param alternativeCount
     * @param registryName
     */
    public CircuitSlave(int alternativeCount, String registryName) {
        super(alternativeCount, registryName, "circuit");
    }


    @Override
    public void removedFromWorld() {
        master.undelete(this);
    }

    @Override
    public boolean isMipMapped() {
        return false;
    }
}
