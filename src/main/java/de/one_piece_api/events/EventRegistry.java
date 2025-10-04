package de.one_piece_api.events;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class EventRegistry {

    private static final List<BiConsumer<ServerPlayerEntity, Identifier>> DEVIL_FRUIT_EATEN = new ArrayList<>();

    public static void registerDevilFruitEatenEvent(BiConsumer<ServerPlayerEntity, Identifier> listener) {
        DEVIL_FRUIT_EATEN.add(listener);
    }

    public static void fireDevilFruitEaten(ServerPlayerEntity player, Identifier fruit) {
        for (BiConsumer<ServerPlayerEntity, Identifier> listener : DEVIL_FRUIT_EATEN) {
            listener.accept(player, fruit);
        }
    }

}
