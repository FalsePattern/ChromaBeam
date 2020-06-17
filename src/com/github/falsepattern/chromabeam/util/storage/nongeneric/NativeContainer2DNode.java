package com.github.falsepattern.chromabeam.util.storage.nongeneric;

public class NativeContainer2DNode {
    int[][][][][] quadrants = new int[4][0][0][0][0];

    public int[][] get(int x, int y) {
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

    public void set(int x, int y, int[][] data) {
        var id = ((y >>> 31) << 1) | (x >>> 31);
        var quadrant = quadrants[id];
        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (quadrant.length <= y) {
            var oldQ = quadrant;
            quadrants[id] = quadrant = new int[y + 256][0][0][0];
            System.arraycopy(oldQ, 0, quadrant, 0, oldQ.length);
        }
        var row = quadrant[y];
        if (row.length <= x) {
            var oldRow = row;
            quadrant[y] = row = new int[x + 256][0][0];
            System.arraycopy(oldRow, 0, row, 0, oldRow.length);
        }
        row[x] = data;
    }

    public int[][] remove(int x, int y) {
        var quadrant = quadrants[((y >>> 31) << 1) | (x >>> 31)];

        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (y < quadrant.length) {
            var row = quadrant[y];
            if (x < row.length) {
                var result = row[x];
                row[x] = null;
                return result;
            }
        }
        return null;
    }

    public void clear() {
        quadrants = new int[4][0][0][0][0];
    }
}
