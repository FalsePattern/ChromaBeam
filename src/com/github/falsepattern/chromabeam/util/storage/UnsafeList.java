package com.github.falsepattern.chromabeam.util.storage;

import java.lang.reflect.Array;
import java.util.*;

/**
 * ---INTERNAL CLASS, USE NOT RECOMMENDED---
 * This is a class implementing the List interface without any kind of bounds or validity checks.
 */
public class UnsafeList<T> extends AbstractList<T> implements RandomAccess{

    public Object[] storage;
    int arraySize;
    static final float GROWTH_MULTIPLIER = 1.5f;
    static final int DEFAULT_INITIAL_SIZE = 256;

    public UnsafeList() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public UnsafeList(int initialSize) {
        super();
        storage = new Object[initialSize];
        arraySize = 0;
    }

    @Override
    public int size() {
        return arraySize;
    }

    @Override
    public boolean isEmpty() {
        return arraySize == 0;
    }

    @Override
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

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int ctr;

            @Override
            public boolean hasNext() {
                return ctr < arraySize;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                return (T)storage[ctr++];
            }
        };
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(storage, arraySize);
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    @Override
    public <T1> T1[] toArray(T1[] a) {
        if (a.length >= arraySize) {
            System.arraycopy(storage, 0, a, 0, arraySize);
            if (a.length > arraySize) {
                a[arraySize] = null;
            }
            return a;
        } else {
            var arr = (T1[])Array.newInstance(a.getClass().getComponentType(), arraySize);
            System.arraycopy(storage, 0, arr, 0, arraySize);
            return arr;
        }
    }

    @Override
    public boolean add(T t) {
        if (arraySize == storage.length) {
            extend();
        }
        storage[arraySize++] = t;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < arraySize; i++) {
            if (Objects.equals(o, storage[i])) {
                dropElement(i);
                return true;
            }
        }
        return false;
    }


    private void dropElement(int i) {
        System.arraycopy(storage, i + 1, storage, i, arraySize - i);
        arraySize--;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T element: c) {
            add(element);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        var newSize = arraySize + c.size();
        if (newSize > storage.length)
            storage = Arrays.copyOf(storage, newSize);
        for (T element: c) {
            storage[arraySize++] = element;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        var result = false;
        for (Object element: c) {
            result |= remove(element);
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        var result = false;
        for (int i = 0; i < arraySize; i++) {
            if (!c.contains(storage[i])) {
                dropElement(i);
                result = true;
            }
        }
        return result;
    }

    @Override
    public void clear() {
        arraySize = 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(int index) {
        return (T)storage[index];
    }

    @SuppressWarnings("unchecked")
    @Override
    public T set(int index, T element) {
        if (index == arraySize) arraySize++;
        var prev = storage[index];
        storage[index] = element;
        return (T)prev;
    }

    @Override
    public void add(int index, T element) {
        if (arraySize + 1 > storage.length) {
            extend();
        }
        System.arraycopy(storage, index, storage, index + 1, arraySize - index);
        arraySize++;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T remove(int index) {
        var original = storage[index];
        dropElement(index);
        return (T)original;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < arraySize; i++) {
            if (Objects.equals(o, storage[i])) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = arraySize - 1; i > 0; i--) {
            if (Objects.equals(o, storage[i])) return i;
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new CustomIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new CustomIterator(index);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof List)) {
            return false;
        } else {
            var iterA = iterator();
            var iterB = ((List<?>)o).iterator();
            while (iterA.hasNext() && iterB.hasNext()) {
                if (!Objects.equals(iterA.next(), iterB.next())) return false;
            }
            return iterA.hasNext() == iterB.hasNext();
        }
    }



    private void extend() {
        storage = Arrays.copyOf(storage, (int)(storage.length * GROWTH_MULTIPLIER));
    }

    private class CustomIterator implements ListIterator<T> {

        private int ctr;
        private CustomIterator(int size) {
            ctr = size;
        }
        @Override
        public boolean hasNext() {
            return ctr < arraySize;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            canModify = true;
            if (ctr == arraySize) throw new NoSuchElementException();
            return (T)storage[lastReturned = ctr++];
        }

        @Override
        public boolean hasPrevious() {
            return ctr > 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T previous() {
            canModify = true;
            if (ctr == 0) throw new NoSuchElementException();
            return (T)storage[lastReturned = --ctr];
        }

        @Override
        public int nextIndex() {
            return ctr;
        }

        @Override
        public int previousIndex() {
            return ctr - 1;
        }

        private boolean canModify = false;
        private int lastReturned = 0;

        @Override
        public void remove() {
            if (canModify) {
                UnsafeList.this.remove(lastReturned);
                canModify = false;
            } else
                throw new IllegalStateException();
        }

        @Override
        public void set(T t) {
            if (canModify) {
                UnsafeList.this.set(lastReturned, t);
                canModify = false;
            } else
                throw new IllegalStateException();
        }

        @Override
        public void add(T t) {
            if (canModify) {
                canModify = false;
                UnsafeList.this.add(lastReturned, t);
            } else
                throw new IllegalStateException();
        }
    }
}
