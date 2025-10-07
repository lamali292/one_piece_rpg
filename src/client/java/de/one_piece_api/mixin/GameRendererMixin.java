package de.one_piece_api.mixin;

import de.one_piece_api.PostProcessingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for {@link GameRenderer} that integrates custom post-processing effects.
 * <p>
 * This mixin hooks into the game renderer's lifecycle to apply, resize, and clean up
 * post-processing effects managed by {@link PostProcessingManager}.
 *
 * @see GameRenderer
 * @see PostProcessingManager
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Reference to the Minecraft client instance.
     */
    @Final
    @Shadow
    MinecraftClient client;

    /**
     * Applies post-processing effects after the main render pass.
     * <p>
     * This method is injected at the tail of the render method to ensure all
     * game rendering is complete before applying custom effects.
     *
     * @param tickCounter the render tick counter for timing
     * @param tick whether this is a tick frame
     * @param ci callback info from the mixin injection
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void applyPostEffect(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        PostProcessingManager.applyPostEffect(client, tickCounter, tick);
    }

    /**
     * Handles post-processing effect resize when the game window is resized.
     * <p>
     * This ensures that post-processing framebuffers and shaders are updated
     * to match the new window dimensions.
     *
     * @param width the new window width in pixels
     * @param height the new window height in pixels
     * @param ci callback info from the mixin injection
     */
    @Inject(method = "onResized", at = @At("TAIL"))
    private void onResized(int width, int height, CallbackInfo ci) {
        PostProcessingManager.onResized(width, height);
    }

    /**
     * Cleans up post-processing resources when the game renderer is closed.
     * <p>
     * This method is injected at the head of the close method to ensure proper
     * cleanup of shaders and framebuffers before the renderer shuts down.
     *
     * @param ci callback info from the mixin injection
     */
    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        PostProcessingManager.onClose();
    }

}