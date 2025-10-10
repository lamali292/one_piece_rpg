package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.ICombatPlayer;
import de.one_piece_api.mixin_interface.ISpellPlayer;
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

/**
 * Mixin for {@link SpellHotbar} that restricts spell availability based on combat mode and learned spells.
 * <p>
 * This mixin overrides the hotbar update logic to only display spells when the player is in
 * combat mode and has actually learned those spells. It filters out unlearned spells and
 * organizes them based on their keybindings.
 *
 * @see SpellHotbar
 * @see ICombatPlayer
 * @see ISpellPlayer
 */
@Mixin(value = SpellHotbar.class)
public abstract class SpellHotbarMixin {

    /**
     * Determines which item stack is expected to be used based on the player's current state.
     *
     * @param player the player entity
     * @return the expected item use information, or {@code null}
     */
    @Shadow
    public static SpellHotbar.ItemUseExpectation expectedUseStack(PlayerEntity player) {
        return null;
    }

    /**
     * Gets a typed reference to this mixin instance as a {@link SpellHotbar}.
     *
     * @return this instance cast to {@link SpellHotbar}
     */
    @Unique
    private SpellHotbar onepiece$getSelf() {
        return (SpellHotbar) (Object) this;
    }

    /**
     * Updates the spell hotbar with combat mode restrictions and learned spell filtering.
     * <p>
     * This method completely replaces the default update behavior. It only populates the
     * hotbar when the player is in combat mode, and filters the spell list to include
     * only spells that the player has learned. Spells are organized into slots based on
     * their keybindings, with special handling for spells bound to the use key.
     *
     * @param player the client player entity
     * @param options the game options containing keybindings
     * @param cir callback info returning whether the hotbar changed
     */
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void update(ClientPlayerEntity player, GameOptions options, CallbackInfoReturnable<Boolean> cir) {
        var changed = false;
        var initialSlotCount = onepiece$getSelf().slots.size();

        var slots = new ArrayList<SpellHotbar.Slot>();
        var otherSlots = new ArrayList<SpellHotbar.Slot>();
        SpellHotbar.Slot onUseKey = null;

        var allBindings = Keybindings.Wrapped.all();
        var useKey = ((KeybindingAccessor) options.useKey).getBoundKey();

        // Block ALL spells when not in combat mode
        if (player instanceof ISpellPlayer iOnePiecePlayer && player instanceof ICombatPlayer iCombatPlayer &&  iCombatPlayer.onepiece$isCombatMode()) {
            List<RegistryEntry<Spell>> spells = iOnePiecePlayer.onepiece$getSelectedSpells();
            List<RegistryEntry<Spell>> learned = SpellUtil.getLearnedSpells(player);

            for (int i = 0; i < spells.size(); i++) {
                RegistryEntry<Spell> spellEntry = spells.get(i);
                if (spellEntry == null) continue;
                var spell = spellEntry.value();
                if (spell == null) continue;

                // Skip spells that aren't learned while in combat
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