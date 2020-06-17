package com.github.falsepattern.chromabeam.mod;

/**
 * Used when determining beam collisions with components.
 */
public enum BeamCollision {
    /**
     * Signals that the beam should stop at the edge of the component
     */
    EDGE,

    /**
     * Signals that the beam should stop at the center of the component
     */
    CENTER,

    /**
     * Signals that the beam should ignore the component and continue forward
     */
    PASS
}
