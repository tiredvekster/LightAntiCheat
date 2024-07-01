package com.kireiko.utils.registry;

public interface Provider<T> {
    T get();
}