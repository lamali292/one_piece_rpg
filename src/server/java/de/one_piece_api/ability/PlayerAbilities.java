package de.one_piece_api.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks which passive abilities a player has active.
 * You'll want to implement this using Fabric's component system for persistence.
 */
public class PlayerAbilities {
    private final Set<Identifier> activeAbilities = new HashSet<>();

    /**
     * Activates a passive ability for the player.
     */
    public void activate(Identifier abilityId) {
        activeAbilities.add(abilityId);
    }

    /**
     * Deactivates a passive ability.
     */
    public void deactivate(Identifier abilityId) {
        activeAbilities.remove(abilityId);
    }

    /**
     * Checks if an ability is active.
     */
    public boolean isActive(Identifier abilityId) {
        return activeAbilities.contains(abilityId);
    }

    /**
     * Gets all active ability IDs.
     */
    public Set<Identifier> getActiveAbilities() {
        return new HashSet<>(activeAbilities);
    }

    /**
     * Ticks all active abilities for the player.
     */
    public void tick(PlayerEntity player, long tickCount) {
        for (Identifier abilityId : activeAbilities) {
            PassiveAbilityRegistry.get(abilityId).ifPresent(ability -> {
                ability.tick(player, tickCount);
            });
        }
    }
}