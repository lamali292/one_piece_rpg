package de.one_piece_content_data.builder;

import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.config.DevilFruitPathConfig;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Builder for creating Devil Fruit configurations.
 * <p>
 * This builder provides a fluent API for defining Devil Fruit abilities including
 * skill paths, instant passives, and 3D model references. Devil Fruits can have
 * multiple progression paths that players unlock sequentially.
 *
 * <h2>Configuration Components:</h2>
 * <ul>
 *     <li><b>Paths</b> - Sequential skill progression chains</li>
 *     <li><b>Instant Passives</b> - Skills granted immediately upon consumption</li>
 *     <li><b>Model ID</b> - Reference to the 3D fruit model</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * DevilFruitConfig config = new DevilFruitBuilder()
 *     .modelId(Identifier.of("onepiece", "gomu_gomu"))
 *     .addPassive(Identifier.of("onepiece", "rubber_body"))
 *     .newPath(path -> path
 *         .add(skill1, skill2, skill3))
 *     .newPath(path -> path
 *         .add(skill4, skill5))
 *     .build();
 * }</pre>
 *
 * @see DevilFruitConfig
 * @see DevilFruitPathConfig
 * @see PathBuilder
 */
public class DevilFruitBuilder {
    /** Identifier for the 3D fruit model */
    private Identifier modelId;

    /** List of skill progression paths */
    private final List<DevilFruitPathConfig> paths = new ArrayList<>();

    /** List of skills granted immediately upon fruit consumption */
    private final List<Identifier> instantPassives = new ArrayList<>();

    /**
     * Creates a new Devil Fruit builder.
     */
    public DevilFruitBuilder() {
    }

    /**
     * Adds an instant passive skill to the fruit.
     * <p>
     * Instant passives are granted immediately when the player consumes
     * the Devil Fruit, without requiring progression through paths.
     *
     * @param skillId the skill identifier to grant instantly
     * @return this builder for method chaining
     */
    public DevilFruitBuilder addPassive(Identifier skillId) {
        instantPassives.add(skillId);
        return this;
    }

    /**
     * Builder for creating individual skill progression paths.
     * <p>
     * A path represents a linear sequence of skills that are unlocked
     * one after another as the player progresses. Each Devil Fruit can
     * have multiple independent paths.
     *
     * <h2>Example:</h2>
     * <pre>{@code
     * new PathBuilder()
     *     .add(basicSkill, intermediateSkill, advancedSkill)
     *     .build();
     * }</pre>
     */
    public static class PathBuilder {
        /** Ordered list of skill identifiers in this path */
        private final List<Identifier> path = new ArrayList<>();

        /**
         * Adds one or more skills to this path.
         * <p>
         * Skills are added in the order provided and will be unlocked
         * sequentially during gameplay.
         *
         * @param skillIds the skill identifiers to add to the path
         * @return this builder for method chaining
         */
        public PathBuilder add(Identifier... skillIds) {
            path.addAll(Arrays.asList(skillIds));
            return this;
        }

        /**
         * Builds the path configuration.
         *
         * @return the completed path configuration
         */
        public DevilFruitPathConfig build() {
            return new DevilFruitPathConfig(path);
        }
    }

    /**
     * Sets the 3D model identifier for this Devil Fruit.
     * <p>
     * The model ID references the 3D mesh used to render the fruit
     * item in the world and inventory.
     *
     * @param modelId the model identifier
     * @return this builder for method chaining
     */
    public DevilFruitBuilder modelId(Identifier modelId) {
        this.modelId = modelId;
        return this;
    }

    /**
     * Adds a new skill progression path to the fruit.
     * <p>
     * Paths are independent progression chains. Players can progress
     * through multiple paths simultaneously, with each path tracking
     * its own unlock state.
     *
     * @param builder function that configures and builds the path
     * @return this builder for method chaining
     */
    public DevilFruitBuilder newPath(Function<PathBuilder, PathBuilder> builder) {
        paths.add(builder.apply(new PathBuilder()).build());
        return this;
    }

    /**
     * Builds the final Devil Fruit configuration.
     * <p>
     * Creates an immutable {@link DevilFruitConfig} containing all defined
     * paths, instant passives, and model reference.
     *
     * @return the completed Devil Fruit configuration
     */
    public DevilFruitConfig build() {
        return new DevilFruitConfig(paths, instantPassives, modelId);
    }
}