package de.one_piece_api.mixin;

import de.one_piece_api.hud.CustomSpellHotBarWidget;
import net.minecraft.client.gui.DrawContext;
import net.spell_engine.client.gui.HudRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HudRenderHelper.class)
public abstract class HudRenderHelperMixin {
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
}