package kireiko.dev.utils.registry;

public interface Provider<T> {
    T get();
}