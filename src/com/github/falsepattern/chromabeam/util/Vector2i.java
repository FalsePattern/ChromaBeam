package com.github.falsepattern.chromabeam.util;

import com.badlogic.gdx.math.Vector2;

public class Vector2i {
    public int x;
    public int y;

    public static final Vector2i X = new Vector2i(1, 0);
    public static final Vector2i Y = new Vector2i(0, 1);
    public static final Vector2i XN = new Vector2i(-1, 0);
    public static final Vector2i YN = new Vector2i(0, -1);
    public static final Vector2i ZERO = new Vector2i(0, 0);
    public static final Vector2i ONE = new Vector2i(1, 1);
    public Vector2i() {
        this(0, 0);
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i original) {
        this(original.x, original.y);
    }

    public Vector2i(Vector2 original) {
        this((int)original.x, (int)original.y);
    }

    public Vector2i set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2i add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2i add(Vector2i other) {
        x += other.x;
        y += other.y;
        return this;
    }

    public Vector2i sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2i sub(Vector2i other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public Vector2i mul(float scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    public Vector2i mul(Vector2i other) {
        x *= other.x;
        y *= other.y;
        return this;
    }

    public Vector2i rotate90DegCW(int steps) {
        if (steps < 0) {
            steps += ((steps / -4) + 1) * 4;
        }
        int tx = x;
        switch(steps % 4) {
            default -> {}
            case 1 -> {
                x = +y;
                y = -tx;
            }
            case 2 -> {
                x = -x;
                y = -y;
            }
            case 3 -> {
                x = -y;
                y = +tx;
            }
        }
        return this;
    }

    public Vector2i rotate90DegCCW(int steps) {
        return rotate90DegCW(4 - steps);
    }

    public Vector2i cln() {
        return new Vector2i(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector2i) {
            return x == ((Vector2i) obj).x && y == ((Vector2i) obj).y;
        } else if (obj instanceof Vector2) {
            return x == ((Vector2) obj).x && y == ((Vector2) obj).y;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(x: " + x + ", y: " + y + ")";
    }

}
