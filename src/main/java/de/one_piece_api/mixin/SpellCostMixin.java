package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.IStaminaCost;
import net.spell_engine.api.spell.Spell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Spell.Cost.class, remap = false)
public class SpellCostMixin implements IStaminaCost {
    @Unique
    public float stamina;

    @Override
    public void onepiece$setStaminaCost(float value) {
        stamina = value;
    }

    @Override
    public float onepiece$getStaminaCost() {
        return stamina;
    }
}
