package de.one_piece_api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Observable<T> {
    private T value;
    private final List<ChangeListener<T>> listeners = new ArrayList<>();

    public Observable(T initialValue) {
        this.value = initialValue;
    }

    public Observable() {
        this.value = null;
    }

    public void addListener(ChangeListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ChangeListener<T> listener) {
        listeners.remove(listener);
    }

    public Optional<T> get() {
        return Optional.ofNullable(value);
    }

    public void set(T newValue) {
        if (!Objects.equals(this.value, newValue)) {
            this.value = newValue;
            notifyListeners(newValue);
        }
    }

    private void notifyListeners(T newValue) {
        for (ChangeListener<T> listener : listeners) {
            listener.onChange(newValue);
        }
    }
}