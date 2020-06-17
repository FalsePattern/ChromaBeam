package com.github.falsepattern.chromabeam.util;

public class ImmutablePair<A, B> implements Pair<A, B>{
    public final A A;
    public final B B;
    public ImmutablePair(A a, B b) {
        A = a;
        B = b;
    }
    @Override
    public A getA() {
        return A;
    }

    @Override
    public B getB() {
        return B;
    }
}
