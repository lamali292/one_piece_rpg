package de.one_piece_api.util;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.util.reactive.Observable;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Centralized client-side data storage with change notifications.
 * All UI components should listen to these observables for updates.
 */
public class ClientData {
    public static final Observable<DevilFruitConfig> DEVIL_FRUIT_CONFIG = new Observable<>();
    public static final Observable<Map<Identifier, ClassConfig>> CLASS_CONFIG = new Observable<>();

    // NEW: Observable for data invalidation events
    public static final Observable<DataInvalidationEvent> DATA_INVALIDATION = new Observable<>();

    public static void init() {
        // Set up cascading invalidation when configs change
        DEVIL_FRUIT_CONFIG.addListener(config -> invalidate(DataInvalidationType.DEVIL_FRUIT_CONFIG));
        CLASS_CONFIG.addListener(configs -> invalidate(DataInvalidationType.CLASS_CONFIG));
    }

    /**
     * Notifies all listeners that data has been invalidated and needs refresh
     */
    public static void invalidate(DataInvalidationType type) {
        DATA_INVALIDATION.set(new DataInvalidationEvent(type, System.currentTimeMillis()));
    }

    /**
     * Types of data that can be invalidated
     */
    public enum DataInvalidationType {
        DEVIL_FRUIT_CONFIG,
        CLASS_CONFIG,
        CATEGORY_DATA,
        ALL
    }

    /**
     * Event fired when data is invalidated
     */
    public record DataInvalidationEvent(DataInvalidationType type, long timestamp) {}
}