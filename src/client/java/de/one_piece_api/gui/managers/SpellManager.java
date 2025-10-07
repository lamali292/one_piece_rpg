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
 * <p>
 * This manager handles all operations related to the player's equipped spell slots,
 * including adding, removing, and swapping spells. It ensures consistency between
 * client and server state by synchronizing changes via network packets.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Automatic spell slot swapping when a spell is moved</li>
 *     <li>Dynamic slot list expansion as needed</li>
 *     <li>Server synchronization for all spell changes</li>
 *     <li>Audio feedback for user actions</li>
 * </ul>
 *
 * @see ISpellPlayer
 * @see SpellUtil
 */
public class SpellManager {

    /** The client player entity whose spells are being managed */
    private final ClientPlayerEntity player;

    /**
     * Creates a new spell manager for the given player.
     *
     * @param player the client player entity
     */
    public SpellManager(ClientPlayerEntity player) {
        this.player = player;
    }

    /**
     * Gets all spells currently equipped by the player.
     * <p>
     * This returns the player's active spell selection that appears in their
     * spell hotbar, not the full list of learned spells.
     *
     * @return a list of equipped spell registry entries
     */
    public List<RegistryEntry<Spell>> getPlayerSpells() {
        return SpellUtil.getPlayerSpells(player);
    }

    /**
     * Gets all spells the player has learned.
     * <p>
     * This returns the complete set of spells available to the player,
     * including both equipped and unequipped spells from all spell containers.
     *
     * @return a list of all learned spell registry entries
     */
    public List<RegistryEntry<Spell>> getLearnedSpells() {
        return SpellUtil.getLearnedSpells(player);
    }

    /**
     * Updates a spell in a specific slot.
     * <p>
     * This method handles intelligent spell placement:
     * <ul>
     *     <li>If the spell is already in another slot, it swaps the spells</li>
     *     <li>If the slot already contains the same spell, no action is taken</li>
     *     <li>The spell list is automatically expanded if needed</li>
     *     <li>Changes are synchronized with the server</li>
     * </ul>
     *
     * @param slotIndex the index of the slot to update (0-based)
     * @param newSpell the spell to place in the slot, or {@code null} to clear it
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
     * <p>
     * If the slot contains a spell, it is removed and the change is synchronized
     * with the server. If the slot is already empty, no action is taken.
     *
     * @param slotIndex the index of the slot to clear (0-based)
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
     * <p>
     * Uses equality comparison to locate the spell. Useful for detecting
     * spell conflicts when assigning spells to slots.
     *
     * @param spells the list of spells to search
     * @param targetSpell the spell to find
     * @return the index of the spell, or -1 if not found
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
     * Synchronizes spell changes with the server.
     * <p>
     * Converts the spell list to a list of spell IDs (or empty strings for null slots)
     * and sends them to the server via network packet. Also updates the local player
     * state to reflect the changes immediately.
     *
     * @param onePiecePlayer the player interface for accessing spell data
     * @param spells the updated list of equipped spells
     */
    private void syncSpells(ISpellPlayer onePiecePlayer, List<RegistryEntry<Spell>> spells) {
        List<String> spellIds = spells.stream()
                .map(spell -> spell != null ? spell.getIdAsString() : "")
                .toList();

        onePiecePlayer.onepiece$setSelectedSpellIds(spellIds);
        ClientPlayNetworking.send(new SetSpellsPayload(spellIds));
    }

    /**
     * Plays a UI click sound for user feedback.
     * <p>
     * Called after successful spell slot updates to provide audio confirmation
     * of the action.
     */
    private void playClickSound() {
        if (player != null) {
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
    }
}