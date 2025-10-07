package de.one_piece_api.util;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.config.DevilFruitConfig;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Client-side data storage for synchronized game configurations.
 * <p>
 * This class provides observable containers for configuration data that is
 * synchronized from the server to the client. UI components and game logic
 * can subscribe to these observables to react to configuration updates.
 *
 * @see Observable
 * @see ClassConfig
 * @see DevilFruitConfig
 */
public class ClientData {

    /**
     * Observable container for devil fruit configuration data.
     * <p>
     * This configuration is synchronized from the server and contains information
     * about devil fruit abilities, effects, and properties. Listeners can be
     * attached to react to configuration updates.
     */
    public static final Observable<DevilFruitConfig> DEVIL_FRUIT_CONFIG = new Observable<>();

    /**
     * Observable container for class configuration data mapped by identifier.
     * <p>
     * This configuration is synchronized from the server and contains all available
     * class definitions mapped by their identifiers. Listeners can be attached to
     * react to configuration updates or additions.
     */
    public static final Observable<Map<Identifier, ClassConfig>> CLASS_CONFIG = new Observable<>();

    /**
     * Initializes client data storage.
     * <p>
     * This method should be called during client initialization.
     */
    public static void init() {
    }
}