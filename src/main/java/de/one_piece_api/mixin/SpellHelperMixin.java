package de.one_piece_api.mixin;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.interfaces.IStaminaCost;
import de.one_piece_api.interfaces.IStaminaPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.Ammo;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.container.SpellContainerSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SpellHelper.class, remap = false)
public class SpellHelperMixin {

    @Inject(
            method = "consumeSpellCost",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void addStaminaCost(
            PlayerEntity player,
            float progress,
            SpellContainerSource.SourcedContainer spellSource,
            Identifier spellId,
            RegistryEntry<Spell> spellEntry,
            ItemStack heldItemStack,
            Ammo.Result ammoResult,
            boolean scheduled,
            CallbackInfo ci) {

        if (!(player instanceof IStaminaPlayer staminaPlayer)) {
            return;
        }
        Spell spell = spellEntry.value();
        float staminaCost = calculateStaminaCost(spell, progress);
        applyStaminaCost(staminaPlayer, staminaCost);
    }

    @Unique
    private static float calculateStaminaCost(Spell spell, float progress) {
        float baseCost = 1.0f;
        if (spell.cost instanceof IStaminaCost iStaminaCost) {
            baseCost = iStaminaCost.onepiece$getStaminaCost();
            baseCost *= progress;
        }
        return Math.max(baseCost, 1.0f); // Minimum 1 stamina
    }

    @Unique
    private static void applyStaminaCost(IStaminaPlayer staminaPlayer, float cost) {
        staminaPlayer.onepiece$removeStamina(cost);
    }

    @Inject(
            method = "attemptCasting*",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void checkStamina(
            PlayerEntity player,
            ItemStack itemStack,
            Identifier spellId,
            boolean checkAmmo,
            CallbackInfoReturnable<SpellCast.Attempt> cir) {
        if (cir.getReturnValue().isFail()) {
            return;
        }

        if (!(player instanceof IStaminaPlayer staminaPlayer)) {
            return;
        }

        var spellEntry = SpellRegistry.from(player.getWorld()).getEntry(spellId).orElse(null);
        if (spellEntry == null) {
            return;
        }

        Spell spell = spellEntry.value();
        if (spell.cost instanceof IStaminaCost iStaminaCost) {
            float requiredStamina = iStaminaCost.onepiece$getStaminaCost();
            if (staminaPlayer.onepiece$getStamina() < requiredStamina) {
                SpellCast.Attempt.Result insufficientStamina = SpellCast.Attempt.Result.valueOf("INSUFFICIENT_STAMINA");
                cir.setReturnValue(new SpellCast.Attempt(insufficientStamina, null, null));
            }
        }
    }

}