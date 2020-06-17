package com.github.falsepattern.chromabeam.util.storage;

import com.badlogic.gdx.utils.Disposable;

public class DisposableUnsafeList<T> extends UnsafeList<T> implements Disposable {
    @Override
    public void dispose() {
        clear();
        storage = new Object[0];
    }
}
