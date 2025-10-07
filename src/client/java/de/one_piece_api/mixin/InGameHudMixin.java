package de.one_piece_api.mixin;

import de.one_piece_api.hud.StaminaBar;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for {@link InGameHud} that integrates the custom stamina bar into the HUD.
 * <p>
 * This mixin injects the stamina bar rendering into the main HUD render cycle,
 * displaying it after the mount health bar.
 *
 * @see InGameHud
 * @see StaminaBar
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    /**
     * Renders the stamina bar on the in-game HUD.
     * <p>
     * This method is injected after the mount health rendering to ensure proper
     * layering and positioning. The stamina bar appears centered above the hotbar.
     *
     * @param context the drawing context used for rendering
     * @param tickCounter the render tick counter for animation timing
     * @param ci callback info from the mixin injection
     */
    @Inject(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountHealth(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER))
    private void renderStamina(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        StaminaBar.render(context, tickCounter);
    }
}