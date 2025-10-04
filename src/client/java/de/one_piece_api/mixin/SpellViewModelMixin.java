package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IStaminaCost;
import net.spell_engine.client.gui.HudRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HudRenderHelper.SpellHotBarWidget.SpellViewModel.class)
public class SpellViewModelMixin implements IStaminaCost {

    @Unique
    private float stamina;
    @Override
    public void onepiece$setStaminaCost(float value) {
        this.stamina = value;
    }

    @Override
    public float onepiece$getStaminaCost() {
        return this.stamina;
    }
}
