package com.github.falsepattern.chromabeam.util.storage.quadrant;

import com.github.falsepattern.chromabeam.util.ImmutablePair;
import com.github.falsepattern.chromabeam.util.Pair;

public class SafeNativeContainer2D<U> extends NativeContainer2D<U> {
    @Override
    Pair<int[], U> createPair(int x, int y, U data) {
        return new ImmutablePair<>(new int[]{x, y}, data);
    }
}