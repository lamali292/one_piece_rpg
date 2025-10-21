package de.one_piece_api.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.hud.HudRendererHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
 * @see HudRendererHelper
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {


    @Shadow
    @Final
    private static Identifier AIR_TEXTURE;

    @Shadow
    @Final
    private static Identifier AIR_BURSTING_TEXTURE;

    @Shadow
    private int ticks;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private LivingEntity getRiddenEntity() {
        throw new AssertionError();
    }

    @Shadow
    private int getHeartCount(@Nullable LivingEntity entity) {
        throw new AssertionError();
    }

    @Shadow
    private PlayerEntity getCameraPlayer() {
        throw new AssertionError();
    }

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
        LivingEntity livingEntity = this.getRiddenEntity();
        int t = this.getHeartCount(livingEntity);
        boolean hasFoodBar = t == 0;
        HudRendererHelper.render(context, ticks, tickCounter, hasFoodBar);
    }


    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        HudRendererHelper.renderHealthBar(context, player, maxHealth, lastHealth, absorption, blinking);
        ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void renderFood(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        HudRendererHelper.renderFood(context, player, top, right);
        ci.cancel();
    }
    @Shadow
    @Final
    private static Identifier ARMOR_HALF_TEXTURE, ARMOR_FULL_TEXTURE, ARMOR_EMPTY_TEXTURE;

    @Inject(
            method = "renderArmor",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void lowerArmorBar(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        int l = player.getArmor();
        if (l <= 0) {
            ci.cancel();
            return;
        }
        i = context.getScaledWindowHeight() - 39;
        j = 0;
        k = 0;
        RenderSystem.enableBlend();
        for (int n = 0; n < 10; ++n) {
            int o = x + n * 8;
            if (n * 2 + 1 < l) {
                context.drawGuiTexture(ARMOR_FULL_TEXTURE, o, i, 9, 9);
            }
            if (n * 2 + 1 == l) {
                context.drawGuiTexture(ARMOR_HALF_TEXTURE, o, i, 9, 9);
            }
            if (n * 2 + 1 <= l) continue;
            context.drawGuiTexture(ARMOR_EMPTY_TEXTURE, o, i, 9, 9);
        }
        RenderSystem.disableBlend();
        ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 2), cancellable = true)
    private void customAirRendering(DrawContext context, CallbackInfo ci) {
        PlayerEntity playerEntity = this.getCameraPlayer();
        int m = context.getScaledWindowWidth() / 2 + 91;
        int n = context.getScaledWindowHeight() - 39;
        int u = playerEntity.getMaxAir();
        int v = Math.min(playerEntity.getAir(), u);
        if (playerEntity.isSubmergedIn(FluidTags.WATER) || v < u) {
            // Same level as armor
            int x = MathHelper.ceil((double)(v - 2) * 10.0 / (double)u);
            int y = MathHelper.ceil((double)v * 10.0 / (double)u) - x;
            RenderSystem.enableBlend();
            for (int z = 0; z < x + y; ++z) {
                if (z < x) {
                    context.drawGuiTexture(AIR_TEXTURE, m - z * 8 - 9, n, 9, 9);
                    continue;
                }
                context.drawGuiTexture(AIR_BURSTING_TEXTURE, m - z * 8 - 9, n, 9, 9);
            }
            RenderSystem.disableBlend();
        }

        this.client.getProfiler().pop();
        ci.cancel();
    }
}