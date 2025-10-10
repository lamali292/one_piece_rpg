package de.one_piece_api.util.reactive;

@FunctionalInterface
public interface ChangeListener<T> {
    void onChange(T value);
}
