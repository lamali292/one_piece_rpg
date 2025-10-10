package de.one_piece_api.ability;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for managing passive abilities.
 */
public class PassiveAbilityRegistry {
    private static final Map<Identifier, PassiveAbility> ABILITIES = new HashMap<>();

    /**
     * Registers a passive ability.
     */
    public static void register(PassiveAbility ability) {
        ABILITIES.put(ability.getId(), ability);
    }

    /**
     * Gets a passive ability by ID.
     */
    public static Optional<PassiveAbility> get(Identifier id) {
        return Optional.ofNullable(ABILITIES.get(id));
    }

    /**
     * Gets all registered abilities.
     */
    public static Map<Identifier, PassiveAbility> getAll() {
        return new HashMap<>(ABILITIES);
    }

    /**
     * Checks if an ability is registered.
     */
    public static boolean isRegistered(Identifier id) {
        return ABILITIES.containsKey(id);
    }
}