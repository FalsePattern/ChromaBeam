package com.github.falsepattern.chromabeam.util.storage.quadrant;

import com.github.falsepattern.chromabeam.util.MutablePair;
import com.github.falsepattern.chromabeam.util.Pair;

public class UnsafeNativeContainer2D<U> extends NativeContainer2D<U> {
    private final int[] pos = new int[2];
    private final MutablePair<int[], U> resultPair = new MutablePair<>(pos, null);
    @Override
    Pair<int[], U> createPair(int x, int y, U data) {
        pos[0] = x;
        pos[1] = y;
        resultPair.b = data;
        return resultPair;
    }
}
