package de.one_piece_api.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.one_piece_api.mixin_interface.IStaminaCost;
import net.minecraft.client.gui.DrawContext;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.gui.HudRenderHelper;
import net.spell_engine.client.input.SpellHotbar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin for {@link HudRenderHelper} that customizes spell hotbar rendering and adds stamina cost integration.
 */
@Mixin(value = HudRenderHelper.class, remap = false, priority = 2000)
public abstract class HudRenderHelperMixin {

    /**
     * Injects stamina cost data into spell view models before they are rendered.
     */
    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;FZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/spell_engine/client/gui/HudRenderHelper$SpellHotBarWidget$ViewModel;<init>(Ljava/util/List;)V",
                    remap = false
            ),
            remap = false
    )
    private static void injectStaminaIntoViewModels(
            DrawContext context,
            float tickDelta,
            boolean config,
            CallbackInfo ci,
            @Local List<HudRenderHelper.SpellHotBarWidget.SpellViewModel> spells) {

        List<SpellHotbar.Slot> slots = SpellHotbar.INSTANCE.slots;
        for (int i = 0; i < spells.size() && i < slots.size(); i++) {
            var viewModel = spells.get(i);
            var spellEntry = slots.get(i).spell();

            if (spellEntry != null) {
                Spell spell = spellEntry.value();
                if (spell.cost instanceof IStaminaCost cost) {
                    IStaminaCost staminaCost = (IStaminaCost) (Object) viewModel;
                    if (staminaCost == null) continue;
                    staminaCost.onepiece$setStaminaCost(cost.onepiece$getStaminaCost());
                }
            }
        }

    }
}