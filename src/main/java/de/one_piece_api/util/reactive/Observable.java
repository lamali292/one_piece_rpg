package de.one_piece_api.util.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An observable value container that notifies listeners when its value changes.
 * <p>
 * This class implements the observer pattern, allowing multiple listeners to be
 * notified whenever the stored value is updated. Listeners are only notified when
 * the new value differs from the current value (as determined by {@link Objects#equals}).
 *
 * @param <T> the type of value being observed
 * @see ChangeListener
 */
public class Observable<T> {

    /** The current value stored in this observable */
    private T value;

    /** List of listeners registered to receive change notifications */
    private final List<ChangeListener<T>> listeners = new ArrayList<>();

    /**
     * Creates an observable with an initial value.
     *
     * @param initialValue the initial value to store
     */
    public Observable(T initialValue) {
        this.value = initialValue;
    }

    /**
     * Creates an observable with a {@code null} initial value.
     */
    public Observable() {
        this.value = null;
    }

    /**
     * Adds a listener to be notified of value changes.
     * <p>
     * The listener will be called whenever {@link #set(Object)} is called with
     * a value that differs from the current value.
     *
     * @param listener the listener to add
     */
    public void addListener(ChangeListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ChangeListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Gets the current value as an {@link Optional}.
     *
     * @return an {@link Optional} containing the current value,
     *         or {@link Optional#empty()} if the value is {@code null}
     */
    public Optional<T> get() {
        return Optional.ofNullable(value);
    }

    /**
     * Sets a new value and notifies listeners if the value changed.
     * <p>
     * Listeners are only notified if the new value is not equal to the current
     * value (as determined by {@link Objects#equals}). This prevents redundant
     * notifications when the same value is set multiple times.
     *
     * @param newValue the new value to set
     */
    public void set(T newValue) {
        if (!Objects.equals(this.value, newValue)) {
            this.value = newValue;
            notifyListeners(newValue);
        }
    }

    /**
     * Notifies all registered listeners of the value change.
     *
     * @param newValue the new value to pass to listeners
     */
    private void notifyListeners(T newValue) {
        for (ChangeListener<T> listener : listeners) {
            listener.onChange(newValue);
        }
    }
}