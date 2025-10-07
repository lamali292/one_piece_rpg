package de.one_piece_api.util;

import de.one_piece_api.interfaces.ISpellPlayer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for retrieving and managing player spell data.
 * <p>
 * This class provides helper methods to access both selected spells (from the player's
 * spell selection) and learned spells (from spell containers). It integrates with both
 * the One Piece RPG spell system and the Spell Engine mod.
 *
 * @see ISpellPlayer
 * @see SpellRegistry
 */
public class SpellUtil {

    /**
     * Gets the spells currently selected by the player.
     * <p>
     * This retrieves the player's active spell selection, which represents the spells
     * they have chosen to use from their learned spells. If the player does not
     * implement {@link ISpellPlayer}, an empty list is returned.
     *
     * @param player the client player entity
     * @return a list of selected spell registry entries, or an empty list if not available
     */
    public static List<RegistryEntry<Spell>> getPlayerSpells(ClientPlayerEntity player) {
        if (player instanceof ISpellPlayer onePiecePlayer) {
            return onePiecePlayer.onepiece$getSelectedSpells();
        }
        return List.of();
    }

    /**
     * Gets all spells that the player has learned.
     * <p>
     * This method queries all spell containers associated with the player and
     * collects all spell identifiers, resolving them to their registry entries.
     * This represents the complete set of spells available to the player,
     * regardless of which ones are currently selected.
     *
     * @param player the client player entity
     * @return a list of all learned spell registry entries
     */
    public static List<RegistryEntry<Spell>> getLearnedSpells(ClientPlayerEntity player) {
        return SpellContainerSource.getSpellsOf(player).sources().stream()
                .flatMap(container -> container.container().spell_ids().stream()
                        .map(id -> (RegistryEntry<Spell>) SpellRegistry.from(player.getWorld())
                                .getEntry(Identifier.of(id))
                                .orElse(null)))
                .filter(Objects::nonNull)
                .toList();
    }
}