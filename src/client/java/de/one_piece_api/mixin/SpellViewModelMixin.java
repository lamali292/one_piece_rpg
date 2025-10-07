package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IStaminaCost;
import net.spell_engine.client.gui.HudRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin for {@link HudRenderHelper.SpellHotBarWidget.SpellViewModel} that adds stamina cost tracking.
 * <p>
 * This mixin implements {@link IStaminaCost} to store and retrieve stamina cost information
 * for spell view models, allowing the HUD to display stamina requirements for each spell.
 *
 * @see HudRenderHelper.SpellHotBarWidget.SpellViewModel
 * @see IStaminaCost
 */
@Mixin(HudRenderHelper.SpellHotBarWidget.SpellViewModel.class)
public class SpellViewModelMixin implements IStaminaCost {

    /**
     * The stamina cost associated with this spell view model.
     */
    @Unique
    private float stamina;

    /**
     * Sets the stamina cost for this spell view model.
     *
     * @param value the stamina cost value to set
     */
    @Override
    public void onepiece$setStaminaCost(float value) {
        this.stamina = value;
    }

    /**
     * Gets the stamina cost for this spell view model.
     *
     * @return the stamina cost value
     */
    @Override
    public float onepiece$getStaminaCost() {
        return this.stamina;
    }
}