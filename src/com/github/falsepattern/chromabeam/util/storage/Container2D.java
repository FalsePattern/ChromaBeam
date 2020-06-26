package com.github.falsepattern.chromabeam.util.storage;

import com.github.falsepattern.chromabeam.util.ImmutablePair;
import com.github.falsepattern.chromabeam.util.Pair;
import com.github.falsepattern.chromabeam.util.Vector2i;

import java.util.List;
import java.util.function.Function;

/**
 * A 2-dimensional integer-indexed datastructure allowing both positive and negative indices.
 */
public interface Container2D<T> {

    /**
     * @return The amount of non-null objects contained.
     */
    long getElementCount();

    /**
     * @return The element at the specified coordinates, or null if the cell is empty
     */
    T get(int x, int y);


    /**
     * Reads a specific cell, and if it's null, then sets it to the computed value.
     * @return The value at the specified location, or the computer value if empty.
     */
    default T getOrCompute(int x, int y, Function<int[], T> computeIfEmpty) {
        var result = get(x, y);
        if (result == null) {
            result = computeIfEmpty.apply(new int[]{x, y});
            set(x, y, result);
        }
        return result;
    }

    /**
     * Replaces the element at the specified coordinates with the specified element.
     * @return The old element, or null if the cell is empty
     */
    T set(int x, int y, T data);

    /**
     * Replaces the cell at the specified location with null
     * @return The old contents of the cell
     */
    T remove(int x, int y);

    /**
     * @return True if the specified cell is empty
     */
    boolean isEmpty(int x, int y);

    /**
     * Sets every cell to null.
     */
    void clear();

    /**
     * @return All elements in this container, which are non-null
     */
    List<T> getNonNullUnordered();

    /**
     * Finds the first element in a cardinal direction relative to the specified coordinates.
     * @return The coordinates as an integer array and the element that was found. Null if nothing was found
     */
    Pair<int[], T> getNextExisting(int x, int y, int rotation);

    //alternatives for ease of use

    /**
     * @return The element at the specified coordinates, or null if the cell is empty
     */
    default T get(Vector2i position) {
        return get(position.x, position.y);
    }

    /**
     * Replaces the element at the specified coordinates with the specified element.
     * @return The old element, or null if the cell is empty
     */
    default T set(Vector2i position, T data) {
        return set(position.x, position.y, data);
    }

    /**
     * Replaces the cell at the specified location with null
     * @return The old contents of the cell
     */
    default T remove(Vector2i position) {
        return remove(position.x, position.y);
    }

    /**
     * @return True if the specified cell is empty
     */
    default boolean isEmpty(Vector2i position) {
        return isEmpty(position.x, position.y);
    }

    /**
     * Finds the first element in a cardinal direction relative to the specified coordinates.
     * @return The coordinates as an integer array and the element that was found. Null if nothing was found
     */
    default Pair<int[], T> getNextExisting(Vector2i origin, int rotation) {
        return getNextExisting(origin.x, origin.y, rotation);
    }

    /**
     * Finds the first element in a cardinal direction relative to the specified coordinates.
     * @return The coordinates as an integer array and the element that was found. Null if nothing was found
     */
    default Pair<Vector2i, T> getNextExistingVec(int x, int y, int rotation) {
        var result = getNextExisting(x, y, rotation);
        return new ImmutablePair<>(new Vector2i(result.getA()[0], result.getA()[1]), result.getB());
    }

    /**
     * Finds the first element in a cardinal direction relative to the specified coordinates.
     * @return The coordinates as an integer array and the element that was found. Null if nothing was found
     */
    default Pair<Vector2i, T> getNextExistingVec(Vector2i origin, int rotation) {
        return getNextExistingVec(origin.x, origin.y, rotation);
    }

}
