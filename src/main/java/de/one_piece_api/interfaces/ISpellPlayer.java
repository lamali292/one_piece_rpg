package de.one_piece_api.interfaces;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;

import java.util.List;

public interface ISpellPlayer {
    void onepiece$setSelectedSpellIds(List<String> spells);
    List<String> onepiece$getSelectedSpellIds();
    List<RegistryEntry<Spell>> onepiece$getSelectedSpells();
}
