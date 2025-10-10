package de.one_piece_content_data.builder;

import de.one_piece_api.config.skill.ConnectionsConfig;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Builder for creating skill tree connection configurations.
 * <p>
 * This builder provides a fluent API for defining both normal and exclusive
 * connections between skills. Connections can be bidirectional (arrows on both ends)
 * or unidirectional (arrow on one end), and can optionally have custom styling.
 *
 * <h2>Connection Types:</h2>
 * <ul>
 *     <li><b>Normal connections</b> - Always visible, show skill prerequisites</li>
 *     <li><b>Exclusive connections</b> - Only visible when hovering over connected skills</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ConnectionsConfig config = new ConnectionsBuilder()
 *     .normal(normal -> normal
 *         .addBidirectional(skill1, skill2)
 *         .addUnidirectional(skill2, skill3, customStyle))
 *     .exclusive(exclusive -> exclusive
 *         .addUnidirectional(skill1, skill4))
 *     .build();
 * }</pre>
 *
 * @see ConnectionsConfig
 * @see ConnectionBuilder
 */
public class ConnectionsBuilder {

    /** Builder for normal connections (always visible) */
    private final ConnectionBuilder normal = new ConnectionBuilder();

    /** Builder for exclusive connections (visible on hover) */
    private final ConnectionBuilder exclusive = new ConnectionBuilder();

    /**
     * Configures normal connections.
     * <p>
     * Normal connections are always visible and typically represent
     * skill prerequisites or progression paths.
     *
     * @param builder consumer that configures the normal connection builder
     * @return this builder for method chaining
     */
    public ConnectionsBuilder normal(Consumer<ConnectionBuilder> builder) {
        builder.accept(normal);
        return this;
    }

    /**
     * Configures exclusive connections.
     * <p>
     * Exclusive connections are only visible when hovering over one of the
     * connected skills, useful for showing alternative paths or related skills.
     *
     * @param builder consumer that configures the exclusive connection builder
     * @return this builder for method chaining
     */
    public ConnectionsBuilder exclusive(Consumer<ConnectionBuilder> builder) {
        builder.accept(exclusive);
        return this;
    }

    /**
     * Builds the final connections configuration.
     * <p>
     * Creates an immutable {@link ConnectionsConfig} containing all defined
     * normal and exclusive connections.
     *
     * @return the built connections configuration
     */
    public ConnectionsConfig build() {
        return new ConnectionsConfig(
                new ConnectionsConfig.ConnectionCategory(exclusive.bi, exclusive.uni),
                new ConnectionsConfig.ConnectionCategory(normal.bi, normal.uni)
        );
    }

    /**
     * Builder for individual connection categories (normal or exclusive).
     * <p>
     * Provides methods to add bidirectional and unidirectional connections
     * between skills, with optional custom styling.
     *
     * <h2>Connection Directions:</h2>
     * <ul>
     *     <li><b>Bidirectional</b> - Arrow points both ways, indicating two-way relationship</li>
     *     <li><b>Unidirectional</b> - Arrow points from first skill to second</li>
     * </ul>
     */
    public static class ConnectionBuilder {
        /** List of bidirectional connections */
        private final List<ConnectionsConfig.Connection> bi = new ArrayList<>();

        /** List of unidirectional connections */
        private final List<ConnectionsConfig.Connection> uni = new ArrayList<>();

        /**
         * Adds a bidirectional connection between two skills.
         * <p>
         * Creates a connection with arrows pointing both ways, using default styling.
         *
         * @param connection1 the first skill identifier
         * @param connection2 the second skill identifier
         * @return this builder for method chaining
         */
        private ConnectionBuilder addBidirectional(Identifier connection1, Identifier connection2) {
            bi.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.empty()));
            return this;
        }

        /**
         * Adds a unidirectional connection from one skill to another.
         * <p>
         * Creates a connection with an arrow pointing from the first skill
         * to the second, using default styling.
         *
         * @param connection1 the source skill identifier
         * @param connection2 the target skill identifier
         * @return this builder for method chaining
         */
        public ConnectionBuilder addUnidirectional(Identifier connection1, Identifier connection2) {
            uni.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.empty()));
            return this;
        }

        /**
         * Adds a bidirectional connection between two skills with custom styling.
         * <p>
         * Creates a connection with arrows pointing both ways, using the specified
         * style for custom colors or appearance.
         *
         * @param connection1 the first skill identifier
         * @param connection2 the second skill identifier
         * @param style the style identifier for custom appearance
         * @return this builder for method chaining
         */
        private ConnectionBuilder addBidirectional(Identifier connection1, Identifier connection2, Identifier style) {
            bi.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.of(style)));
            return this;
        }

        /**
         * Adds a unidirectional connection from one skill to another with custom styling.
         * <p>
         * Creates a connection with an arrow pointing from the first skill to the second,
         * using the specified style for custom colors or appearance.
         *
         * @param connection1 the source skill identifier
         * @param connection2 the target skill identifier
         * @param style the style identifier for custom appearance
         * @return this builder for method chaining
         */
        public ConnectionBuilder addUnidirectional(Identifier connection1, Identifier connection2, Identifier style) {
            uni.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.of(style)));
            return this;
        }
    }
}