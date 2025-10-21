package de.one_piece_content_data.builder;

import de.one_piece_api.config.ClassConfig;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating character class configurations with level-based rewards.
 * <p>
 * This builder provides a fluent API for constructing {@link ClassConfig} instances,
 * automatically generating translatable text keys and managing reward progression.
 * <p>
 * The builder automatically creates translation keys in the format:
 * <ul>
 *   <li>Name: {@code "class.<namespace>.<path>.name"}</li>
 *   <li>Description: {@code "class.<namespace>.<path>.description"}</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Define a skill reward first
 * public static final Entry<SkillDefinitionConfig> MASTER_OF_THE_BLADE =
 *     Registries.SKILL_DEFINITION.register(ModId.id("master_of_the_blade"), masterOfTheBlade());
 *
 * static SkillDefinitionConfig masterOfTheBlade() {
 *     SkillRewardConfig reward = new SkillRewardConfig(
 *         PassiveAbilityReward.ID,
 *         new PassiveAbilityReward(List.of(ModId.id("master_of_the_blade")))
 *     );
 *     return new SkillDefinitionConfig(
 *         Text.literal("Unlock Master of the Blade"),
 *         Text.literal("Grants enhanced sword damage"),
 *         null,
 *         Icon.item(Items.IRON_SWORD),
 *         null,
 *         1,
 *         List.of(reward),
 *         0, 0, 0, 0, 0
 *     );
 * }
 *
 * // Register the class with rewards
 * public static final Entry<ClassConfig> SWORDSMAN = register("swordsman", builder -> builder
 *     .addReward(1, MASTER_OF_THE_BLADE.id())
 *     .addReward(10, ADVANCED_SWORD_TECHNIQUE.id())
 *     .background(
 *         ModId.id("textures/classes/swordsman.png"),
 *         ModId.id("textures/classes/swordsman_name.png")
 *     )
 * );
 *
 * private static Entry<ClassConfig> register(String name,
 *                                            Function<OnePieceClassBuilder, OnePieceClassBuilder> config) {
 *     Identifier id = Identifier.of("<mod_id>",name);
 *     OnePieceClassBuilder builder = new OnePieceClassBuilder(id);
 *     ClassConfig classConfig = config.apply(builder).build();
 *     return Registries.CLASSES.register(id, classConfig);
 * }
 * }</pre>
 * <p>
 * The above example would require corresponding translation entries:
 * <pre>{@code
 * {
 *   "class.mod_id.swordsman.name": "Swordsman",
 *   "class.mod_id.swordsman.description": "Master of the blade, wielding legendary sword techniques"
 * }
 * }</pre>
 *
 * @see ClassConfig
 * @see ClassConfig.LevelReward
 */
public class OnePieceClassBuilder {
    private final Text name;
    private final Text description;
    private final List<ClassConfig.LevelReward> rewards = new ArrayList<>();
    private Identifier backTexture;
    private Identifier nameTexture;

    /**
     * Creates a new builder for a character class.
     * <p>
     * Automatically generates translation keys based on the identifier:
     * <ul>
     *   <li>Name key: {@code "class.<namespace>.<path>.name"}</li>
     *   <li>Description key: {@code "class.<namespace>.<path>.description"}</li>
     * </ul>
     *
     * @param id the unique identifier for this class (e.g., {@code "mod_id:swordsman"})
     */
    public OnePieceClassBuilder(Identifier id) {
        name = Text.translatable("class." + id.getNamespace() + "." + id.getPath() + ".name");
        description = Text.translatable("class." + id.getNamespace() + "." + id.getPath() + ".description");
    }

    /**
     * Adds a level-gated reward to this class.
     * <p>
     * When a player reaches the specified level, the reward will be automatically granted.
     * Rewards should be added in ascending level order for clarity, though this is not enforced.
     * <p>
     * Example:
     * <pre>{@code
     * builder.addReward(1, MASTER_OF_THE_SEAS.id())
     *        .addReward(10, ADVANCED_SWIMMING.id())
     *        .addReward(20, WATER_BREATHING.id());
     * }</pre>
     *
     * @param level the level at which this reward is granted (must be >= 1)
     * @param rewardConfig the identifier of the skill definition to grant as a reward
     * @return this builder for method chaining
     * @throws IllegalArgumentException if level < 1
     */
    public OnePieceClassBuilder addReward(int level, Identifier rewardConfig) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be >= 1, got: " + level);
        }
        rewards.add(new ClassConfig.LevelReward(level, rewardConfig));
        return this;
    }

    /**
     * Sets the visual assets for this class.
     * <p>
     * Both textures are required for the class selection screen:
     * <ul>
     *   <li>{@code backTexture}: The background image displayed for this class</li>
     *   <li>{@code nameTexture}: The name badge overlay displayed on top</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     * builder.background(
     *     ModId.id("textures/classes/fishman.png"),
     *     ModId.id("textures/classes/fishman_name.png")
     * );
     * }</pre>
     *
     * @param backTexture the identifier for the background texture
     * @param nameTexture the identifier for the name badge texture
     * @return this builder for method chaining
     */
    public OnePieceClassBuilder background(Identifier backTexture, Identifier nameTexture) {
        this.backTexture = backTexture;
        this.nameTexture = nameTexture;
        return this;
    }

    /**
     * Builds and returns the final {@link ClassConfig} instance.
     * <p>
     * This method validates that all required fields have been set and creates
     * an immutable configuration object.
     *
     * @return the constructed class configuration
     * @throws IllegalStateException if background and name textures have not been set via {@link #background}
     */
    public ClassConfig build() {
        if (backTexture == null || nameTexture == null) {
            throw new IllegalStateException("Background and name textures must be set");
        }

        return new ClassConfig(
                name,
                description,
                List.copyOf(rewards),
                backTexture,
                nameTexture
        );
    }
}