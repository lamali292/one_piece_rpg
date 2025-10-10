package de.one_piece_api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Event<T> {
    private final List<T> listeners = new ArrayList<>();
    private final Function<List<T>, T> invokerFactory;
    private T invoker;

    private Event(Function<List<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.invoker = invokerFactory.apply(listeners);
    }

    public void register(T listener) {
        listeners.add(listener);
        // Rebuild invoker after adding listener
        this.invoker = invokerFactory.apply(listeners);
    }

    public void unregister(T listener) {
        listeners.remove(listener);
        // Rebuild invoker after removing listener
        this.invoker = invokerFactory.apply(listeners);
    }

    public T invoker() {
        return invoker;
    }

    public static <T> Event<T> create(Function<List<T>, T> invokerFactory) {
        return new Event<>(invokerFactory);
    }
}