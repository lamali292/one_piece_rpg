package de.one_piece_api.mixin;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.hud.CustomSpellHotBarWidget;
import net.minecraft.client.gui.DrawContext;
import net.spell_engine.client.gui.HudRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HudRenderHelper.SpellHotBarWidget.class, remap = false)
public class SpellHotBarWidgetMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IILnet/spell_engine/client/gui/HudRenderHelper$SpellHotBarWidget$ViewModel;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onRender(
            DrawContext context,
            int screenWidth,
            int screenHeight,
            HudRenderHelper.SpellHotBarWidget.ViewModel viewModel,
            CallbackInfo ci) {

        //OnePieceRPG.LOGGER.info("=== CUSTOM HOTBAR RENDERING ===");
        CustomSpellHotBarWidget.render(context, screenWidth, screenHeight, viewModel);
        ci.cancel();
    }
}