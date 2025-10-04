package de.one_piece_api.gui.managers;

import de.one_piece_api.interfaces.ISpellPlayer;
import de.one_piece_api.network.SetSpellsPayload;
import de.one_piece_api.util.SpellUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.spell_engine.api.spell.Spell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages spell selection and updates for the player.
 * Handles adding, removing, and swapping spells in slots.
 */
public class SpellManager {
    private final ClientPlayerEntity player;

    public SpellManager(ClientPlayerEntity player) {
        this.player = player;
    }

    /**
     * Gets all spells available to the player (their equipped spell list).
     */
    public List<RegistryEntry<Spell>> getPlayerSpells() {
        return SpellUtil.getPlayerSpells(player);
    }

    /**
     * Gets all spells the player has learned.
     */
    public List<RegistryEntry<Spell>> getLearnedSpells() {
        return SpellUtil.getLearnedSpells(player);
    }

    /**
     * Updates a spell in a specific slot.
     * Handles slot swapping if the spell already exists elsewhere.
     */
    public void updateSpellSlot(int slotIndex, RegistryEntry<Spell> newSpell) {
        if (!(player instanceof ISpellPlayer onePiecePlayer)) {
            return;
        }

        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());

        // Ensure list is large enough
        while (current.size() <= slotIndex) {
            current.add(null);
        }

        RegistryEntry<Spell> oldValue = current.get(slotIndex);

        // No change needed
        if (Objects.equals(oldValue, newSpell)) {
            return;
        }

        // Handle spell swapping if spell exists elsewhere
        int existingIndex = findSpellIndex(current, newSpell);
        if (existingIndex != -1 && existingIndex != slotIndex) {
            current.set(existingIndex, oldValue);
        }

        // Update the target slot
        current.set(slotIndex, newSpell);

        // Sync with server
        syncSpells(onePiecePlayer, current);
        playClickSound();
    }

    /**
     * Removes a spell from a specific slot.
     */
    public void removeSpellFromSlot(int slotIndex) {
        if (!(player instanceof ISpellPlayer onePiecePlayer)) {
            return;
        }

        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());

        // Ensure list is large enough
        while (current.size() <= slotIndex) {
            current.add(null);
        }

        if (current.get(slotIndex) != null) {
            current.set(slotIndex, null);
            syncSpells(onePiecePlayer, current);
            playClickSound();
        }

    }

    /**
     * Finds the index of a spell in the spell list.
     */
    private int findSpellIndex(List<RegistryEntry<Spell>> spells, RegistryEntry<Spell> targetSpell) {
        for (int i = 0; i < spells.size(); i++) {
            if (Objects.equals(spells.get(i), targetSpell)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Syncs spell changes with the server.
     */
    private void syncSpells(ISpellPlayer onePiecePlayer, List<RegistryEntry<Spell>> spells) {
        List<String> spellIds = spells.stream()
                .map(spell -> spell != null ? spell.getIdAsString() : "")
                .toList();

        onePiecePlayer.onepiece$setSelectedSpellIds(spellIds);
        ClientPlayNetworking.send(new SetSpellsPayload(spellIds));
    }

    /**
     * Plays a click sound.
     */
    private void playClickSound() {
        if (player != null) {
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
    }
}