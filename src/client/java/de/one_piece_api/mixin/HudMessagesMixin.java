package de.one_piece_api.mixin;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.spell_engine.client.gui.HudMessages;
import net.spell_engine.internals.casting.SpellCast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HudMessages.class, remap = false)
public class HudMessagesMixin {
    @Inject(method = "castAttemptError", at = @At("HEAD"), cancellable = true)
    private void handleStaminaError(SpellCast.Attempt attempt, CallbackInfo ci) {
        if (attempt.result().toString().equals("INSUFFICIENT_STAMINA")) {
            MutableText message = Text.translatable("hud.cast_attempt_error.insufficient_stamina")
                    .formatted(Formatting.AQUA);
            ((HudMessages)(Object)this).error(message);
            ci.cancel();
        }
    }
}