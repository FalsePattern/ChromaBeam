package com.github.falsepattern.chromabeam.core;

import com.github.falsepattern.chromabeam.mod.interfaces.InputHandlerRegistry;
import com.github.falsepattern.chromabeam.mod.interfaces.RendererRegistry;

public final class PostInitializationRegistry {
    public final RendererRegistry screenSpaceRendererRegistry;
    public final RendererRegistry worldSpaceRendererRegistry;
    public final InputHandlerRegistry inputHandlerRegistry;

    public PostInitializationRegistry(RendererRegistry screenSpaceRendererRegistry, RendererRegistry worldSpaceRendererRegistry, InputHandlerRegistry inputHandlerRegistry) {
        this.screenSpaceRendererRegistry = screenSpaceRendererRegistry;
        this.worldSpaceRendererRegistry = worldSpaceRendererRegistry;
        this.inputHandlerRegistry = inputHandlerRegistry;
    }
}
