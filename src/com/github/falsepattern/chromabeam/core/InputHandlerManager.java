package com.github.falsepattern.chromabeam.core;

import com.github.falsepattern.chromabeam.mod.interfaces.InputHandler;
import com.github.falsepattern.chromabeam.mod.interfaces.InputHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

public final class InputHandlerManager implements InputHandlerRegistry {
    final List<InputHandler> handlers;
    InputHandlerManager() {
        handlers = new ArrayList<>();
    }
    @Override
    public void addInputHandler(InputHandler inputHandler) {
        handlers.add(inputHandler);
    }
}
