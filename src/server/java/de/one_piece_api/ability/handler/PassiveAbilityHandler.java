package de.one_piece_api.ability.handler;

import de.one_piece_api.ability.PlayerAbilities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles ticking of passive abilities for all players.
 */
public class PassiveAbilityHandler {
    private static long tickCount = 0;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCount++;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerAbilities abilities = getPlayerAbilities(player);
                abilities.tick(player, tickCount);
            }
        });
    }

    /**
     * Gets the PlayerAbilities for a player.
     * You should implement this using Fabric's component system for proper persistence.
     * For now, this is a simple in-memory solution.
     */
    private static final Map<UUID, PlayerAbilities> PLAYER_DATA = new HashMap<>();

    public static PlayerAbilities getPlayerAbilities(ServerPlayerEntity player) {
        return PLAYER_DATA.computeIfAbsent(player.getUuid(), k -> new PlayerAbilities());
    }
}