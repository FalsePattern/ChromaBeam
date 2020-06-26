package com.github.falsepattern.chromabeam.mod.interfaces;

/**
 * Wrapper interface for renderers only allowing addition of custom renderers.
 */
public interface RendererRegistry {

    /**
     * Adds a renderable to the renderer.
     */
    void addRenderable(Renderable renderable);
}
