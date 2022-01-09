package com.aeltumn.autocraft.api;

/**
 * A simple implementation of a Pair that stores a key and value.
 */
public class Pair<A, B> {
    private final A key;
    private final B value;

    public Pair(A key, B value) {
        this.key = key;
        this.value = value;
    }

    public A getKey() {
        return key;
    }

    public B getValue() {
        return value;
    }
}
