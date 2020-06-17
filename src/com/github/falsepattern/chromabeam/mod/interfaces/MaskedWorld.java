package com.github.falsepattern.chromabeam.mod.interfaces;

import com.github.falsepattern.chromabeam.mod.Component;

/**
 * This interface is used when passing in World objects to components so that the components can't access every cell
 * only it's neighbors, and the core utilities required for beam launching.
 */
public interface MaskedWorld {

    /**
     * Launches a beam from the component's position with specific direction and color.
     */
    void createBeam(int rotation, int color);

    /**
     * Returns all 4 cells next to the component in the order of front, right, back, left
     * @return An array of components. Always has a size of 4, and empty cells are marked by null.
     */
    default Component[] getAllNeighbors() {
        return new Component[]{getNeighborFront(), getNeighborRight(), getNeighborBack(), getNeighborLeft()};
    }

    /**
     * Gets the neighbor in front of the component.
     */
    Component getNeighborFront();

    /**
     * Gets the neighbor on the right of the component.
     */
    Component getNeighborRight();

    /**
     * Gets the neighbor behind of the component.
     */
    Component getNeighborBack();

    /**
     * Gets the neighbor on the left of the component.
     */
    Component getNeighborLeft();
}
