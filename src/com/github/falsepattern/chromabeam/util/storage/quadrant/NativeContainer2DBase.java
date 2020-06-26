package com.github.falsepattern.chromabeam.util.storage.quadrant;

import com.github.falsepattern.chromabeam.util.Pair;
import com.github.falsepattern.chromabeam.util.storage.Container2D;
import com.github.falsepattern.chromabeam.util.storage.UnsafeList;

import java.util.List;

public class NativeContainer2DBase<T> implements Container2D<T> {
    Object[][][] quadrants = new Object[4][0][0];
    public NativeContainer2DBase() {
    }
    @Override
    public long getElementCount() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(int x, int y) {
        var quadrant = quadrants[((y >>> 31) << 1) | (x >>> 31)];
        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (y < quadrant.length) {
            var row = quadrant[y];
            if (x < row.length) {
                return (T) row[x];
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T set(int x, int y, T data) {
        var id = ((y >>> 31) << 1) | (x >>> 31);
        var quadrant = quadrants[id];
        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (quadrant.length <= y) {
            var oldQ = quadrant;
            quadrants[id] = quadrant = new Object[y + 1][0];
            System.arraycopy(oldQ, 0, quadrant, 0, oldQ.length);
        }
        var row = quadrant[y];
        if (row.length <= x) {
            var oldRow = row;
            quadrant[y] = row = new Object[x + 1];
            System.arraycopy(oldRow, 0, row, 0, oldRow.length);
        }
        var original = row[x];
        row[x] = data;
        return (T) original;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T remove(int x, int y) {
        var quadrant = quadrants[((y >>> 31) << 1) | (x >>> 31)];

        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (y < quadrant.length) {
            var row = quadrant[y];
            if (x < row.length) {
                var result = row[x];
                row[x] = null;
                return (T)result;
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        for (int i = 0; i < 4; i++) {
            quadrants[i] = new Object[0][0];
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getNonNullUnordered() {
        var result = new UnsafeList<T>();
        for (int i = 0; i < 4; i++) {
            var quadrant = quadrants[i];
            for (int y = 0; y < quadrant.length; y++) {
                var row = quadrant[y];
                for (int x = 0; x < row.length; x++) {
                    if (row[x] != null) result.add((T)row[x]);
                }
            }
        }
        return result;
    }

    @Override
    public Pair<int[], T> getNextExisting(int x, int y, int rotation) {
        throw new UnsupportedOperationException();
    }
}
