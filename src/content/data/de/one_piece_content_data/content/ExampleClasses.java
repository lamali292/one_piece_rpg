package de.one_piece_content_data.content;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.reward.PassiveAbilityReward;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.builder.OnePieceClassBuilder;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Registry for character class definitions.
 * <p>
 * This class defines all available character classes (races) that players can
 * select at the start of the game. Each class has unique skills, colors, and
 * visual assets that define its identity and playstyle.
 *
 * <h2>Class Components:</h2>
 * Each class configuration includes:
 * <ul>
 *     <li>Primary skill - Main active ability</li>
 *     <li>Passive skill - Automatic passive ability</li>
 *     <li>Theme colors - Primary and secondary UI colors</li>
 *     <li>Textures - Background image and name badge</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * Call {@link #init()} during mod initialization to register all classes.
 *
 * @see ClassConfig
 * @see OnePieceClassBuilder
 */
public class ExampleClasses {

    /**
     * Fishman class configuration.
     */
    public static final Entry<ClassConfig> FISHMAN = register("fishman", builder -> builder
            .addReward(1,
                    PassiveAbilityReward.ID,
                    new PassiveAbilityReward(List.of(ExampleMod.id("master_of_the_seas")))
            )
            .background(
                    ExampleMod.id("textures/classes/fishman.png"),
                    ExampleMod.id("textures/classes/fishman_name.png")
            )
    );

    /**
     * Human class configuration.
     */
    public static final Entry<ClassConfig> HUMAN = register("human", builder -> builder
            .background(
                    ExampleMod.id("textures/classes/human.png"),
                    ExampleMod.id("textures/classes/human_name.png")
            )
    );

    /**
     * Mink class configuration.
     */
    public static final Entry<ClassConfig> MINK = register("mink", builder -> builder
            .background(
                    ExampleMod.id("textures/classes/mink.png"),
                    ExampleMod.id("textures/classes/mink_name.png")
            )
    );

    /**
     * Registers a new character class.
     * <p>
     * Creates a class configuration using the builder pattern and registers it
     * with the mod's class registry. The class will be available for selection
     * in the class selection screen.
     *
     * @param name   the class name (used for ID and translation keys)
     * @param config function that configures the class builder
     * @return the registry entry for the configured class
     */
    private static Entry<ClassConfig> register(String name,
                                               java.util.function.Function<OnePieceClassBuilder, OnePieceClassBuilder> config) {
        Identifier id = ExampleMod.id(name);
        OnePieceClassBuilder builder = new OnePieceClassBuilder(id);
        ClassConfig classConfig = config.apply(builder).build();
        return Registries.CLASSES.register(id, classConfig);
    }

    /**
     * Initializes all character classes.
     * <p>
     * This method should be called during mod initialization to ensure all
     * classes are registered. The actual registration happens during static
     * initialization of the class fields, but calling this method forces
     * the class to load.
     */
    public static void init() {
        // Static initialization registers all classes
    }
}