package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

public class MyDataComponentTypes {


    public static final ComponentType<Identifier> DEVIL_FRUIT = register("devil_fruit", builder -> builder.codec(Identifier.CODEC));
    public static final ComponentType<Integer> XP = register("xp", builder -> builder.codec(Codecs.NONNEGATIVE_INT));

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builder) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, OnePieceRPG.id(name), builder.apply(ComponentType.builder()).build());
    }

    public static void register() {

    }
}
