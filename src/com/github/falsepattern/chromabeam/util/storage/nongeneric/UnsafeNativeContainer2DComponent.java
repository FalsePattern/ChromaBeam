package com.github.falsepattern.chromabeam.util.storage.nongeneric;

import com.github.falsepattern.chromabeam.mod.Component;
import com.github.falsepattern.chromabeam.util.MutablePair;
import com.github.falsepattern.chromabeam.util.Pair;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

import java.util.List;

public class UnsafeNativeContainer2DComponent {

    private final int[] pos = new int[2];
    private final MutablePair<int[], Component> resultPair = new MutablePair<>(pos, null);
    private int elementCount = 0;

    Component[][][] quadrants = new Component[4][0][0];

    public long getElementCount() {
        return elementCount;
    }

    public Component get(int x, int y) {
        var quadrant = quadrants[((y >>> 31) << 1) | (x >>> 31)];
        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (y < quadrant.length) {
            var row = quadrant[y];
            if (x < row.length) {
                return row[x];
            }
        }
        return null;
    }

    public Component set(int x, int y, Component data) {
        var id = ((y >>> 31) << 1) | (x >>> 31);
        var quadrant = quadrants[id];
        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (quadrant.length <= y) {
            var oldQ = quadrant;
            quadrants[id] = quadrant = new Component[y + 256][0];
            System.arraycopy(oldQ, 0, quadrant, 0, oldQ.length);
        }
        var row = quadrant[y];
        if (row.length <= x) {
            var oldRow = row;
            quadrant[y] = row = new Component[x + 256];
            System.arraycopy(oldRow, 0, row, 0, oldRow.length);
        }
        var original = row[x];
        if (original == null && data != null) elementCount++;
        if (original != null && data == null) elementCount--;
        row[x] = data;
        return original;
    }

    public boolean isEmpty(int x, int y) {
        return elementCount == 0;
    }

    public Component remove(int x, int y) {
        var quadrant = quadrants[((y >>> 31) << 1) | (x >>> 31)];

        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (y < quadrant.length) {
            var row = quadrant[y];
            if (x < row.length) {
                var result = row[x];
                if (result != null) elementCount--;
                row[x] = null;
                return result;
            }
        }
        return null;
    }

    public void clear() {
        quadrants = new Component[4][0][0];
        elementCount = 0;
    }

    public List<Component> getNonNullUnordered() {
        var result = new UnsafeList<Component>();
        for (int i = 0; i < 4; i++) {
            var quadrant = quadrants[i];
            for (int y = 0; y < quadrant.length; y++) {
                var row = quadrant[y];
                for (int x = 0; x < row.length; x++) {
                    if (row[x] != null) result.add(row[x]);
                }
            }
        }
        return result;
    }

    private Pair<int[], Component> createPair(int x, int y, Component data) {
        pos[0] = x;
        pos[1] = y;
        resultPair.b = data;
        return resultPair;
    }

    private Pair<int[], Component> getNXPP(int x, int y, Component[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (; x < row.length; x++) {
                if (row[x] != null) return createPair(x, y, row[x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNXNN(int x, int y, Component[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (x = ~x; x < row.length; x++) {
                if (row[x] != null) return createPair(~x, y, row[x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNXNP(int x, int y, Component[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (x = Math.min(x, row.length - 1); x >= 0; x--) {
                if (row[x] != null) return createPair(x, y, row[x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNXPN(int x, int y, Component[][] storage) {
        if (storage.length > y) {
            var row = storage[y];
            for (x = Math.min(~x, row.length - 1); x >= 0; x--) {
                if (row[x] != null) return createPair(~x, y,  row[x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNextX(int x, int y, int dx, Component[][] pos, Component[][] neg) {
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

    private Pair<int[], Component> getNYPP(int x, int y, Component[][] storage) {
        for (; y < storage.length; y++) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, y, storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNYNN(int x, int y, Component[][] storage) {
        for (y = ~y; y < storage.length; y++) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, ~y, storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNYNP(int x, int y, Component[][] storage) {
        for (y = Math.min(y, storage.length - 1); y >= 0; y--) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, y, storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNYPN(int x, int y, Component[][] storage) {
        for (y = Math.min(~y, storage.length - 1); y >= 0; y--) {
            if (storage[y].length > x) {
                if (storage[y][x] != null) return createPair(x, ~y, storage[y][x]);
            }
        }
        return null;
    }

    private Pair<int[], Component> getNextY(int x, int y, int dy, Component[][] pos, Component[][] neg) {
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

    public Pair<int[], Component> getNextExisting(int x, int y, int rotation) {
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
