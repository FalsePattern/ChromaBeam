package com.github.falsepattern.chromabeam.util.storage.nongeneric;

import com.github.falsepattern.chromabeam.mod.Component;

import java.util.*;

/**
 * Like UnsafeList, but for components, and stripped down
 */
public class UnsafeComponentList {
    public Component[] storage;
    int arraySize;
    static final float GROWTH_MULTIPLIER = 1.5f;
    static final int DEFAULT_INITIAL_SIZE = 256;

    public UnsafeComponentList() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public UnsafeComponentList(int initialSize) {
        super();
        storage = new Component[initialSize];
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

    public boolean add(Component t) {
        if (arraySize == storage.length) {
            extend();
        }
        storage[arraySize++] = t;
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

    public Component[] toArray() {
        var result = new Component[arraySize];
        System.arraycopy(storage, 0, result, 0, arraySize);
        return result;
    }


    private void dropElement(int i) {
        System.arraycopy(storage, i + 1, storage, i, arraySize - i);
        arraySize--;
    }


    public void clear() {
        arraySize = 0;
    }

    public Component get(int index) {
        return storage[index];
    }

    public Component set(int index, Component element) {
        var prev = storage[index];
        storage[index] = element;
        return prev;
    }

    public void add(int index, Component element) {
        if (arraySize + 1 > storage.length) {
            extend();
        }
        System.arraycopy(storage, index, storage, index + 1, arraySize - index);
        storage[index] = element;
        arraySize++;
    }

    public Component remove(int index) {
        var original = storage[index];
        dropElement(index);
        return original;
    }

    public int indexOf(Component o) {
        for (int i = 0; i < arraySize; i++) {
            if (Objects.equals(o, storage[i])) return i;
        }
        return -1;
    }

    public int lastIndexOf(Component o) {
        for (int i = arraySize - 1; i > 0; i--) {
            if (Objects.equals(o, storage[i])) return i;
        }
        return -1;
    }

    private void extend() {
        storage = Arrays.copyOf(storage, (int)(storage.length * GROWTH_MULTIPLIER));
    }


}
