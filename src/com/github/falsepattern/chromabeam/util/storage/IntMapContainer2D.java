package com.github.falsepattern.chromabeam.util.storage;

import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.github.falsepattern.chromabeam.util.ImmutablePair;
import com.github.falsepattern.chromabeam.util.MutablePair;
import com.github.falsepattern.chromabeam.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Container2D using {@link IntMap}s provided by the LibGDX engine.
 * @param <T>
 */
public class IntMapContainer2D<T> extends IntMapContainer2DBase<T>{
    private final IntIntMap cellsPerRow;
    private final IntIntMap cellsPerColumn;
    private final boolean unsafeOptimizations;
    private int highX;
    private int highY;
    private int lowX;
    private int lowY;

    public IntMapContainer2D() {
        this(false);
    }

    public IntMapContainer2D(boolean unsafeOptimizations) {
        cellsPerRow = new IntIntMap();
        cellsPerColumn = new IntIntMap();
        this.unsafeOptimizations = unsafeOptimizations;
    }

    private void activate(int x, int y) {
        cellsPerRow.getAndIncrement(y, 0, 1);
        cellsPerColumn.getAndIncrement(x, 0, 1);
        if (y > highY) highY = y;
        else if (y < lowY) lowY = y;
        if (x > highX) highX = x;
        else if (x < lowX) lowX = x;
    }

    private void shrinkBounds() {
        while (cellsPerRow.get(highY, 0) == 0 && highY >= lowY) {
            highY--;
        }
        while (cellsPerRow.get(lowY, 0) == 0 && lowY <= highY) {
            lowY--;
        }
        while (cellsPerColumn.get(highX, 0) == 0 && highX >= lowX) {
            highX--;
        }
        while (cellsPerColumn.get(lowY, 0) == 0 && lowX <= highX) {
            lowX--;
        }
        if (highY < lowY) {
            highY = lowY = 0;
        }
        if (highX < lowX) {
            highX = lowX = 0;
        }
    }

    private void deactivate(int x, int y) {
        int r = cellsPerRow.getAndIncrement(y, 0, -1);
        int c = cellsPerColumn.getAndIncrement(x, 0, -1);
        if ((r <= 0 && (y == highY || y == lowY)) || (c <= 0 && (x == lowX || x == highX))) {
            shrinkBounds();
        }
    }

    @Override
    public T set(int x, int y, T data) {
        var out = super.set(x, y, data);
        if (out == null && data != null) {
            activate(x, y);
        } else if (out != null && data == null) {
            deactivate(x, y);
        }
        return out;
    }

    @Override
    public T remove(int x, int y) {
        var result = super.remove(x, y);
        if (result != null) {
            deactivate(x, y);
        }
        return result;
    }

    @Override
    public void clear() {
        cellsPerRow.clear();
        cellsPerColumn.clear();
        lowX = lowY = highX = highY = 0;
    }

    private final MutablePair<int[], T> nextAxisGetBuffer = new MutablePair<>(new int[]{0, 0}, null);

    private Pair<int[], T> getNextXAxis(int x, int y, int dx) {
        if (y < lowY || y > highY) return null;
        var row = storage.get(y, empty);
        if (row == empty) return null;
        T result;
        while ((x += dx) >= lowX && x <= highX) {
            if ((result = row.get(x, null)) != null) {
                if (unsafeOptimizations) {
                    var coord = nextAxisGetBuffer.a;
                    coord[0] = x;
                    coord[1] = y;
                    nextAxisGetBuffer.b = result;
                    return nextAxisGetBuffer;
                } else {
                    return new ImmutablePair<>(new int[]{x, y}, result);
                }
            }
        }
        return null;
    }

    private Pair<int[], T> getNextYAxis(int x, int y, int dy) {
        if (x < lowX || x > highX) return null;
        T result;
        while ((y += dy) >= lowY && y <= highY) {
            if ((result = storage.get(y, empty).get(x, null)) != null) {
                if (unsafeOptimizations) {
                    var coord = nextAxisGetBuffer.a;
                    coord[0] = x;
                    coord[1] = y;
                    nextAxisGetBuffer.b = result;
                    return nextAxisGetBuffer;
                } else {
                    return new ImmutablePair<>(new int[]{x, y}, result);
                }
            }
        }
        return null;
    }

    @Override
    public Pair<int[], T> getNextExisting(int x, int y, int rotation) {
        if (rotation == 0 || rotation == 2) {
            return getNextXAxis(x, y, -rotation + 1);
        } else {
            return getNextYAxis(x, y, rotation == 3 ? 1 : -1);
        }
    }
}
