package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.ICombatPlayer;
import de.one_piece_api.interfaces.ISpellPlayer;
import de.one_piece_api.util.SpellUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.input.Keybindings;
import net.spell_engine.client.input.SpellHotbar;
import net.spell_engine.client.input.WrappedKeybinding;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.mixin.client.control.KeybindingAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SpellHotbar.class)
public abstract class SpellHotbarMixin {


    @Shadow
    public static SpellHotbar.ItemUseExpectation expectedUseStack(PlayerEntity player) {
        return null;
    }

    @Unique
    private SpellHotbar onepiece$getSelf() {
        return (SpellHotbar) (Object) this;
    }


    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void update(ClientPlayerEntity player, GameOptions options, CallbackInfoReturnable<Boolean> cir) {
        var changed = false;
        var initialSlotCount = onepiece$getSelf().slots.size();

        var slots = new ArrayList<SpellHotbar.Slot>();
        var otherSlots = new ArrayList<SpellHotbar.Slot>();
        SpellHotbar.Slot onUseKey = null;

        var allBindings = Keybindings.Wrapped.all();
        var useKey = ((KeybindingAccessor) options.useKey).getBoundKey();

        // block ALL spells when not in combat mode

        if (player instanceof ISpellPlayer iOnePiecePlayer && player instanceof ICombatPlayer iCombatPlayer &&  iCombatPlayer.onepiece$isCombatMode()) {
            List<RegistryEntry<Spell>> spells = iOnePiecePlayer.onepiece$getSelectedSpells();
            List<RegistryEntry<Spell>> learned = SpellUtil.getLearnedSpells(player);

            for (int i = 0; i < spells.size(); i++) {
                RegistryEntry<Spell> spellEntry = spells.get(i);
                if (spellEntry == null) continue;
                var spell = spellEntry.value();
                if (spell == null) continue;

                // skip spells that aren’t learned while in combat
                if (!learned.contains(spellEntry)) {
                    continue;
                }

                WrappedKeybinding keyBinding;
                if (i < allBindings.size()) {
                    keyBinding = allBindings.get(i);
                } else {
                    continue;
                }
                var slot = new SpellHotbar.Slot(spellEntry, SpellCast.Mode.from(spell), null, keyBinding, null);
                if (keyBinding != null) {
                    var unwrapped = keyBinding.get(options);
                    if (unwrapped != null) {
                        var hotbarKey = ((KeybindingAccessor) unwrapped.keyBinding()).getBoundKey();

                        if (hotbarKey.equals(useKey)) {
                            onUseKey = slot;
                        } else {
                            otherSlots.add(slot);
                        }
                    }
                }
                slots.add(slot);
            }
        }

        changed = initialSlotCount != slots.size();
        onepiece$getSelf().structuredSlots = new SpellHotbar.StructuredSlots(onUseKey, otherSlots);
        onepiece$getSelf().slots = slots;
        cir.setReturnValue(changed);
    }
}
