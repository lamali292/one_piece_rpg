package de.one_piece_api.config;

import net.spell_engine.api.spell.Spell;

public record SpellConfig(Spell spell, String title, String description) {
}