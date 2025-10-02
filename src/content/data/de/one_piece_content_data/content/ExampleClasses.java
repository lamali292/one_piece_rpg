package de.one_piece_content_data.content;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.builder.OnePieceClassBuilder;
import de.one_piece_content_data.registry.Registries;
import net.minecraft.util.Identifier;

public class ExampleClasses {

    public static final Entry<ClassConfig> FISHMAN = register("fishman",builder -> builder
            .primary("fishman_primary")
            .passive("fishman_passive")
            .color(0x2E86C1, 0x1B4F72)
            .background(
                    ExampleMod.id("textures/classes/fishman.png"),
                    ExampleMod.id("textures/classes/fishman_name.png")
            )
    );

    public static final Entry<ClassConfig> HUMAN = register("human", builder -> builder
            .primary("human_primary")
            .passive("human_passive")
            .color(0xE74C3C, 0xA93226)
            .background(
                    ExampleMod.id("textures/classes/human.png"),
                    ExampleMod.id("textures/classes/human_name.png")
            )
    );

    public static final Entry<ClassConfig> MINK = register("mink", builder -> builder
            .primary("mink_primary")
            .passive("mink_passive")
            .color(0xF39C12, 0xD68910)
            .background(
                    ExampleMod.id("textures/classes/mink.png"),
                    ExampleMod.id("textures/classes/mink_name.png")
            )
    );

    private static Entry<ClassConfig> register(String name,
                                        java.util.function.Function<OnePieceClassBuilder, OnePieceClassBuilder> config) {
        Identifier id = ExampleMod.id(name);
        OnePieceClassBuilder builder = new OnePieceClassBuilder(id);
        ClassConfig classConfig = config.apply(builder).build();
        return Registries.CLASSES.register(id, classConfig);
    }

    public static void init() {
    }
}