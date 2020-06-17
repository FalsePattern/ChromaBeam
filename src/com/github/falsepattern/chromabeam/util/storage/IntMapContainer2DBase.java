package com.github.falsepattern.chromabeam.util.storage;

import com.badlogic.gdx.utils.IntMap;
import com.github.falsepattern.chromabeam.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class IntMapContainer2DBase<T> implements Container2D<T>{
    protected final IntMap<IntMap<T>> storage;
    protected final IntMap<T> empty;
    private int elementCount = 0;

    public IntMapContainer2DBase() {
        storage = new IntMap<>();
        empty = new IntMap<>();
    }

    @Override
    public long getElementCount() {
        return elementCount;
    }

    @Override
    public T get(int x, int y) {
        return storage.get(y, empty).get(x);
    }

    @Override
    public T set(int x, int y, T data) {
        var row = storage.get(y);
        if (row == null) {
            row = new IntMap<>();
            storage.put(y, row);
        }
        var out = row.put(x, data);
        if (out == null && data != null) {
            elementCount++;
        } else if (out != null && data == null) {
            elementCount--;
        }
        return out;
    }

    @Override
    public T remove(int x, int y) {
        var result = storage.get(y, empty).remove(x);
        if (result != null) {
            elementCount--;
        }
        return result;
    }

    @Override
    public boolean isEmpty(int x, int y) {
        return elementCount == 0;
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public List<T> getNonNullUnordered() {
        ArrayList<T> result = new ArrayList<>(elementCount);
        for (var kvPair: storage) {
            for (var cell: kvPair.value) {
                if (cell.value != null) {
                    result.add(cell.value);
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
