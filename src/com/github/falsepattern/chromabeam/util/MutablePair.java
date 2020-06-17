package com.github.falsepattern.chromabeam.util;

public class MutablePair<A, B> implements Pair<A, B> {
    public A a;
    public B b;
    public MutablePair(A a, B b) {
        this.a = a;
        this.b = b;
    }
    @Override
    public A getA() {
        return a;
    }

    @Override
    public B getB() {
        return b;
    }
}
