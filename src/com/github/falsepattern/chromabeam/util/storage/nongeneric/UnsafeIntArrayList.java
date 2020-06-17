package com.github.falsepattern.chromabeam.util.storage.nongeneric;

import java.util.Arrays;
import java.util.Objects;

public class UnsafeIntArrayList {
    public int[][] storage;
    int arraySize;
    static final float GROWTH_MULTIPLIER = 1.5f;
    static final int DEFAULT_INITIAL_SIZE = 256;

    final int cellSize;

    public UnsafeIntArrayList(int cellSize) {
        this(DEFAULT_INITIAL_SIZE, cellSize);
    }

    public UnsafeIntArrayList(int initialSize, int cellSize) {
        super();
        storage = new int[initialSize][cellSize];
        this.cellSize = cellSize;
        arraySize = 0;
    }

    public int size() {
        return arraySize;
    }

    public boolean isEmpty() {
        return arraySize == 0;
    }

    public boolean contains(Object o) {
        if (o == null) return hasNull();
        for (int i = 0; i < arraySize; i++) {
            if (o.equals(storage[i])) return true;
        }
        return false;
    }

    private boolean hasNull() {
        for (int i = 0; i < arraySize; i++) {
            if (storage[i] == null) return true;
        }
        return false;
    }

    public boolean add(int[] t) {
        if (arraySize == storage.length) {
            extend();
        }
        System.arraycopy(t, 0, storage[arraySize++], 0, cellSize);
        return true;
    }

    public boolean remove(Object o) {
        for (int i = 0; i < arraySize; i++) {
            if (Objects.equals(o, storage[i])) {
                dropElement(i);
                return true;
            }
        }
        return false;
    }

    public int[][] toArray() {
        var result = new int[arraySize][cellSize];
        System.arraycopy(storage, 0, result, 0, arraySize);
        return result;
    }


    private void dropElement(int i) {
        var e = storage[i];
        System.arraycopy(storage, i + 1, storage, i, arraySize - i);
        arraySize--;
        storage[arraySize] = e;
    }


    public void clear() {
        arraySize = 0;
    }

    public int[] get(int index) {
        return storage[index];
    }

    public void set(int index, int[] element) {
        System.arraycopy(element, 0, storage[index], 0, cellSize);
    }

    private void extend() {
        storage = Arrays.copyOf(storage, (int)(storage.length * GROWTH_MULTIPLIER));
    }
}
