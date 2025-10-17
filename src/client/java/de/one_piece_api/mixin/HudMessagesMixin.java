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

/**
 * Mixin for {@link HudMessages} that adds custom error handling for stamina-related spell cast failures.
 * <p>
 * This mixin intercepts cast attempt errors and provides a custom localized message
 * when a spell fails due to insufficient stamina.
 *
 * @see HudMessages
 * @see SpellCast.Attempt
 */
@Mixin(value = HudMessages.class, remap = false)
public class HudMessagesMixin {

    /**
     * Handles stamina-specific error messages when spell casting fails.
     * <p>
     * When a spell cast attempt fails due to insufficient stamina, this method
     * intercepts the default error handling and displays a custom aqua-colored
     * localized message instead.
     *
     * @param attempt the spell cast attempt containing the failure reason
     * @param ci callback info from the mixin injection, used to cancel default behavior
     */
    @Inject(method = "castAttemptError", at = @At("HEAD"), cancellable = true, remap = false)
    private void handleStaminaError(SpellCast.Attempt attempt, CallbackInfo ci) {
        if (attempt.result().toString().equals("INSUFFICIENT_STAMINA")) {
            MutableText message = Text.translatable("hud.cast_attempt_error.insufficient_stamina")
                    .formatted(Formatting.AQUA);
            ((HudMessages)(Object)this).error(message);
            ci.cancel();
        }
    }
}