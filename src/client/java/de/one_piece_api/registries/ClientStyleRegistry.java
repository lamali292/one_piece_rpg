package de.one_piece_api.registries;

import de.one_piece_api.config.StyleConfig;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Client-side registry for managing visual style configurations.
 * <p>
 * This registry maintains a collection of style configurations synchronized from the server,
 * which are used to customize the appearance of skill connections and other UI elements.
 * The registry is populated via network packets and provides thread-safe access to styles.
 *
 * @see StyleConfig
 */
public class ClientStyleRegistry {

    /**
     * Internal storage for style configurations mapped by their identifiers.
     */
    private static final Map<Identifier, StyleConfig> styles = new HashMap<>();

    /**
     * Replaces all registered styles with a new set of styles.
     * <p>
     * This method clears the existing registry and populates it with the provided
     * style configurations. Typically called when receiving style data from the server.
     *
     * @param newStyles a map of style identifiers to their configurations
     */
    public static void setStyles(Map<Identifier, StyleConfig> newStyles) {
        styles.clear();
        styles.putAll(newStyles);
    }

    /**
     * Retrieves a style configuration by its identifier.
     *
     * @param id the identifier of the style to retrieve
     * @return an {@link Optional} containing the {@link StyleConfig} if found,
     *         or {@link Optional#empty()} if no style with the given identifier exists
     */
    public static Optional<StyleConfig> getStyle(Identifier id) {
        return Optional.ofNullable(styles.get(id));
    }
}