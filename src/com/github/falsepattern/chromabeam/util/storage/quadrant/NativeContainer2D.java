package com.github.falsepattern.chromabeam.util.storage.quadrant;

import com.github.falsepattern.chromabeam.util.Pair;

abstract class NativeContainer2D<T> extends NativeContainer2DBase<T>{

    abstract Pair<int[], T> createPair(int x, int y, T data);

    private int elementCount = 0;

    @Override
    public long getElementCount() {
        return elementCount;
    }

    @Override
    public void clear() {
        super.clear();
        elementCount = 0;
    }

    @Override
    public T set(int x, int y, T data) {
        var old = super.set(x, y, data);
        if (old == null && data != null) elementCount++;
        else if (old != null && data == null) elementCount--;
        return old;
    }

    @Override
    public T remove(int x, int y) {
        var old = super.remove(x, y);
        if (old != null) elementCount--;
        return old;
    }

    @Override
    public boolean isEmpty(int x, int y) {
        return elementCount == 0;
    }


    private Pair<int[], T> getNXPP(int x, int y, Object[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (; x < row.length; x++) {
                if (row[x] != null) return createPair(x, y, (T) row[x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNXNN(int x, int y, Object[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (x = ~x; x < row.length; x++) {
                if (row[x] != null) return createPair(~x, y, (T) row[x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNXNP(int x, int y, Object[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (x = Math.min(x, row.length - 1); x >= 0; x--) {
                if (row[x] != null) return createPair(x, y, (T) row[x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNXPN(int x, int y, Object[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (x = Math.min(~x, row.length - 1); x >= 0; x--) {
                if (row[x] != null) createPair(~x, y, (T) row[x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNextX(int x, int y, int dx, Object[][] pos, Object[][] neg) {
        x += dx;
        if (dx > 0 && x >= 0) {
            return getNXPP(x, y, pos);
        } else if (dx < 0 && x < 0) {
            return getNXNN(x, y, neg);
        } else if (dx < 0) {
            var first = getNXNP(x, y, pos);
            if (first == null) return getNXNN(-1, y, neg);
            return first;
        } else if (dx > 0) {
            var first = getNXPN(x, y, neg);
            if (first == null) return getNXPP(0, y, pos);
            return first;
        }
        return null;
    }

    private Pair<int[], T> getNYPP(int x, int y, Object[][] storage) {
        for (; y < storage.length; y++) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, y, (T) storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNYNN(int x, int y, Object[][] storage) {
        for (y = ~y; y < storage.length; y++) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, ~y, (T) storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNYNP(int x, int y, Object[][] storage) {
        for (y = Math.min(y, storage.length - 1); y >= 0; y--) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, y, (T) storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNYPN(int x, int y, Object[][] storage) {
        for (y = Math.min(~y, storage.length - 1); y >= 0; y--) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, ~y, (T) storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], T> getNextY(int x, int y, int dy, Object[][] pos, Object[][] neg) {
        y += dy;
        if (dy > 0 && y >= 0) {
            return getNYPP(x, y, pos);
        } else if (dy < 0 && y < 0) {
            return getNYNN(x, y, neg);
        } else if (dy < 0) {
            var first =getNYNP(x, y, pos);
            if (first == null) return getNYNN(x, -1, neg);
            return first;
        } else if (dy > 0) {
            var first = getNYPN(x, y, neg);
            if (first == null) return getNYPP(x, 0, pos);
            return first;
        }
        return null;
    }

    @Override
    public Pair<int[], T> getNextExisting(int x, int y, int rotation) {
        return switch (rotation) {
            default -> null;
            case 0, 2 -> {
                var result = getNextX(x, y < 0 ? ~y : y, 1 - rotation, quadrants[(y >>> 31) << 1], quadrants[((y >>> 31) << 1) | 1]);
                if (result != null)
                result.getA()[1] = y;
                yield result;
            }
            case 1, 3 -> {
                var result = getNextY(x < 0 ? ~x : x, y, rotation - 2, quadrants[(x >>> 31)], quadrants[(x >>> 31) | 2]);
                if (result != null)
                result.getA()[0] = x;
                yield result;
            }
        };
    }
}
