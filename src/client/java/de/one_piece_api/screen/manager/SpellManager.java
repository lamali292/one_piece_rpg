package de.one_piece_api.screen.manager;

import de.one_piece_api.mixin_interface.ISpellPlayer;
import de.one_piece_api.network.payload.SetSpellsPayload;
import de.one_piece_api.util.SpellUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.spell_engine.api.spell.Spell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpellManager {

    private final ClientPlayerEntity player;

    public SpellManager(ClientPlayerEntity player) {
        this.player = player;
    }

    public List<RegistryEntry<Spell>> getPlayerSpells() {
        return SpellUtil.getPlayerSpells(player);
    }

    public List<RegistryEntry<Spell>> getLearnedSpells() {
        return SpellUtil.getLearnedSpells(player);
    }

    public void updateSpellSlot(int slotIndex, RegistryEntry<Spell> newSpell) {
        if (!(player instanceof ISpellPlayer onePiecePlayer)) {
            return;
        }

        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());

        while (current.size() <= slotIndex) {
            current.add(null);
        }

        RegistryEntry<Spell> oldValue = current.get(slotIndex);

        if (Objects.equals(oldValue, newSpell)) {
            return;
        }

        int existingIndex = findSpellIndex(current, newSpell);
        if (existingIndex != -1 && existingIndex != slotIndex) {
            current.set(existingIndex, oldValue);
        }

        current.set(slotIndex, newSpell);

        syncSpells(onePiecePlayer, current);
        playClickSound();

        // REMOVED: Don't trigger immediate invalidation
        // Let the server response trigger the update via packet handler
    }

    public void removeSpellFromSlot(int slotIndex) {
        if (!(player instanceof ISpellPlayer onePiecePlayer)) {
            return;
        }

        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());

        while (current.size() <= slotIndex) {
            current.add(null);
        }

        if (current.get(slotIndex) != null) {
            current.set(slotIndex, null);
            syncSpells(onePiecePlayer, current);
            playClickSound();

            // REMOVED: Don't trigger immediate invalidation
            // Let the server response trigger the update via packet handler
        }
    }

    private int findSpellIndex(List<RegistryEntry<Spell>> spells, RegistryEntry<Spell> targetSpell) {
        for (int i = 0; i < spells.size(); i++) {
            if (Objects.equals(spells.get(i), targetSpell)) {
                return i;
            }
        }
        return -1;
    }

    private void syncSpells(ISpellPlayer onePiecePlayer, List<RegistryEntry<Spell>> spells) {
        List<String> spellIds = spells.stream()
                .map(spell -> spell != null ? spell.getIdAsString() : "")
                .toList();

        onePiecePlayer.onepiece$setSelectedSpellIds(spellIds);
        ClientPlayNetworking.send(new SetSpellsPayload(spellIds));
    }

    private void playClickSound() {
        if (player != null) {
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
    }
}