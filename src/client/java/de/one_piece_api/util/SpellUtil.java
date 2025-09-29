package de.one_piece_api.util;

import de.one_piece_api.interfaces.IOnePiecePlayer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SpellUtil {
    public static List<RegistryEntry<Spell>> getPlayerSpells(ClientPlayerEntity player) {
        if (player instanceof IOnePiecePlayer onePiecePlayer) {
            return onePiecePlayer.onepiece$getSelectedSpells();
        }
        return List.of();
    }

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
