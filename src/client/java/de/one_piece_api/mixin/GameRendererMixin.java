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

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Final
    @Shadow
    MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void applyPostEffect(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        PostProcessingManager.applyPostEffect(client, tickCounter, tick);
    }


    @Inject(method = "onResized", at = @At("TAIL"))
    private void onResized(int width, int height, CallbackInfo ci) {
        PostProcessingManager.onResized(width, height);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        PostProcessingManager.onClose();
    }

}
