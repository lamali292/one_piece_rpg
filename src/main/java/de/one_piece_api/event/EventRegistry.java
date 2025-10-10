package de.one_piece_api.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EventRegistry {


    public static final Event<DevilFruitEatenCallback> DEVIL_FRUIT_EATEN = Event.create(listeners ->
            (player, fruit) -> {
                for (DevilFruitEatenCallback listener : listeners) {
                    listener.onDevilFruitEaten(player, fruit);
                }
            }
    );

    @FunctionalInterface
    public interface DevilFruitEatenCallback {
        void onDevilFruitEaten(ServerPlayerEntity player, Identifier fruit);
    }

    public static final Event<LevelUpCallback> LEVEL_UP = Event.create(listeners ->
            (player, categoryId, oldLevel, newLevel) -> {
                for (LevelUpCallback listener : listeners) {
                    listener.onLevelUp(player, categoryId, oldLevel, newLevel);
                }
            }
    );

    @FunctionalInterface
    public interface LevelUpCallback {
        void onLevelUp(ServerPlayerEntity player, Identifier categoryId, int oldLevel, int newLevel);
    }


}
