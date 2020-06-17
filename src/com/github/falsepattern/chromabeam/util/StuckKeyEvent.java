package com.github.falsepattern.chromabeam.util;

import com.github.falsepattern.chromabeam.core.GlobalData;

/**
 * Used when the window focus would change due to the user pressing a button.
 */
public class StuckKeyEvent {
    public String binding;
    public String simpleName;
    public int id;
    public Runnable callback;
    public StuckKeyEvent(String binding, Runnable callback) {
        this.binding = binding;
        this.simpleName = GlobalData.keyBinds.nameToGdxName(binding);
        this.id = GlobalData.keyBinds.nameToKeyCode(binding);
        this.callback = callback;
    }
}
