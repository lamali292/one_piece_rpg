package de.one_piece_api.util;

@FunctionalInterface
public interface ChangeListener<T> {
    void onChange(T value);
}
