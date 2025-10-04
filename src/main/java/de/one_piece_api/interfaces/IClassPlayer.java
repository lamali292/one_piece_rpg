package de.one_piece_api.interfaces;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;

import java.util.List;

public interface IClassPlayer {
    void onepiece$setOnePieceClass(Identifier className);
    Identifier onepiece$getOnePieceClass();
}
