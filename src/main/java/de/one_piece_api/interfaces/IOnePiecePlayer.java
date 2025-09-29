package de.one_piece_api.interfaces;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;

import java.util.List;
import java.util.Optional;

public interface IOnePiecePlayer {
    void onepiece$setOnePieceClass(Identifier className);
    Identifier onepiece$getOnePieceClass();
    boolean onepiece$isCombatMode();
    void onepiece$setCombatMode(boolean combatMode);
    void onepiece$setSelectedSpellIds(List<String> spells);
    List<String> onepiece$getSelectedSpellIds();
    List<RegistryEntry<Spell>> onepiece$getSelectedSpells();
    String onepiece$getDevilFruit();
    void onepiece$setDevilFruit(String fruit);

}
