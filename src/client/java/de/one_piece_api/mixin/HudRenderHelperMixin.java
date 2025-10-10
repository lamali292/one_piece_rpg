package de.one_piece_api.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.one_piece_api.hud.CustomSpellHotBarWidget;
import de.one_piece_api.mixin_interface.IStaminaCost;
import net.minecraft.client.gui.DrawContext;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.gui.HudRenderHelper;
import net.spell_engine.client.input.SpellHotbar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin for {@link HudRenderHelper} that customizes spell hotbar rendering and adds stamina cost integration.
 * <p>
 * This mixin replaces the default spell hotbar widget with a custom implementation and
 * injects stamina cost data into spell view models for display purposes.
 *
 * @see HudRenderHelper
 * @see CustomSpellHotBarWidget
 * @see IStaminaCost
 */
@Mixin(value = HudRenderHelper.class, remap = false)
public abstract class HudRenderHelperMixin {

    /**
     * Redirects the default spell hotbar rendering to use the custom implementation.
     * <p>
     * This replaces the vanilla spell hotbar widget with {@link CustomSpellHotBarWidget},
     * which provides enhanced visuals and combat mode animations.
     *
     * @param context the drawing context used for rendering
     * @param screenWidth the width of the screen in pixels
     * @param screenHeight the height of the screen in pixels
     * @param viewModel the view model containing spell data to display
     */
    @Redirect(
            method = "render(Lnet/minecraft/client/gui/DrawContext;FZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/spell_engine/client/gui/HudRenderHelper$SpellHotBarWidget;render(Lnet/minecraft/client/gui/DrawContext;IILnet/spell_engine/client/gui/HudRenderHelper$SpellHotBarWidget$ViewModel;)V"
            )
    )
    private static void replaceHotbarRender(DrawContext context, int screenWidth, int screenHeight,
                                            HudRenderHelper.SpellHotBarWidget.ViewModel viewModel) {
        CustomSpellHotBarWidget.render(context, screenWidth, screenHeight, viewModel);
    }

    /**
     * Injects stamina cost data into spell view models before they are rendered.
     * <p>
     * This method iterates through all spell view models and populates them with
     * stamina cost information from the corresponding spell entries. This allows
     * the hotbar to display stamina requirements for each spell.
     *
     * @param context the drawing context
     * @param tickDelta the partial tick time for smooth animations
     * @param config whether configuration mode is active
     * @param ci callback info from the mixin injection
     * @param spells the list of spell view models to populate with stamina data
     */
    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;FZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/spell_engine/client/gui/HudRenderHelper$SpellHotBarWidget$ViewModel;<init>(Ljava/util/List;)V"
            )
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