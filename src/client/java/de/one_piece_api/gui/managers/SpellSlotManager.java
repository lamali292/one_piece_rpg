package de.one_piece_api.gui.managers;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.gui.widgets.SpellSlotWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.Spell;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the spell slot widgets and their layout.
 * Layout is only calculated when needed, not every frame.
 */
public class SpellSlotManager {

    private final List<SpellSlotWidget> slots = new ArrayList<>();
    private final SpellManager spellManager;
    private final int rectDim;
    private final int spellSlotCount;

    // Track layout state to avoid unnecessary recalculation
    private boolean layoutInitialized = false;
    private int lastBaseX = -1;
    private int lastBaseY = -1;
    private int lastSkillTreeWidth = -1;

    public SpellSlotManager(ClientPlayerEntity player, int rectDim, int skilltreeWidth) {
        this.spellManager = new SpellManager(player);
        this.spellSlotCount = OnePieceRPG.getSpellSlots(player);
        this.rectDim = rectDim;
    }

    /**
     * Updates the positions of all spell slots if the layout has changed.
     * Only recalculates when baseX, baseY, or skilltreeWidth differ from last call.
     */
    public void updateLayoutIfNeeded(int baseX, int baseY, int skilltreeWidth) {
        // Check if layout needs updating
        if (layoutInitialized &&
                lastBaseX == baseX &&
                lastBaseY == baseY &&
                lastSkillTreeWidth == skilltreeWidth) {
            return; // Layout unchanged, skip
        }

        // Store new values
        lastBaseX = baseX;
        lastBaseY = baseY;
        lastSkillTreeWidth = skilltreeWidth;
        layoutInitialized = true;

        // Rebuild layout
        rebuildLayout(baseX, baseY, skilltreeWidth);
    }

    /**
     * Forces a layout rebuild regardless of cached values.
     * Useful after screen resize or significant UI changes.
     */
    public void forceLayoutUpdate(int baseX, int baseY, int skilltreeWidth) {
        lastBaseX = baseX;
        lastBaseY = baseY;
        lastSkillTreeWidth = skilltreeWidth;
        layoutInitialized = true;
        rebuildLayout(baseX, baseY, skilltreeWidth);
    }

    /**
     * Internal method that performs the actual layout calculation.
     */
    private void rebuildLayout(int baseX, int baseY, int skilltreeWidth) {
        slots.clear();

        // Calculate spacing for first N-1 slots
        int rectOffset = (skilltreeWidth - spellSlotCount * rectDim) / (spellSlotCount - 1);

        // Create first N-1 slots with even spacing
        for (int i = 0; i < spellSlotCount - 1; i++) {
            int x = baseX + i * (rectDim + rectOffset);
            SpellSlotWidget slot = new SpellSlotWidget(x, baseY, rectDim, i);
            setupSlotHandlers(slot);
            slots.add(slot);
        }

        // Create last slot aligned to right edge
        int lastX = baseX + skilltreeWidth - rectDim;
        SpellSlotWidget lastSlot = new SpellSlotWidget(lastX, baseY, rectDim, spellSlotCount - 1);
        setupSlotHandlers(lastSlot);
        slots.add(lastSlot);
    }

    /**
     * Updates spell data for all slots.
     * This should be called when spells change, not every frame.
     */
    public void updateSpellData() {
        List<RegistryEntry<Spell>> playerSpells = spellManager.getPlayerSpells();
        List<RegistryEntry<Spell>> learnedSpells = spellManager.getLearnedSpells();

        for (int i = 0; i < slots.size(); i++) {
            SpellSlotWidget slot = slots.get(i);

            if (i < playerSpells.size()) {
                RegistryEntry<Spell> spell = playerSpells.get(i);
                boolean learned = spell == null || learnedSpells.contains(spell);
                slot.setSpell(spell, learned);
            } else {
                slot.setSpell(null, true);
            }
        }
    }

    /**
     * Sets up event handlers for a spell slot.
     */
    private void setupSlotHandlers(SpellSlotWidget slot) {
        slot.setOnLeftClick(s -> onSlotLeftClick(s.getSlotIndex()));
        slot.setOnRightClick(s -> onSlotRightClick(s.getSlotIndex()));
    }

    /**
     * Called when a slot is left-clicked (open selection).
     */
    private void onSlotLeftClick(int slotIndex) {
        // Handled by the screen to show overlay
    }

    /**
     * Called when a slot is right-clicked (remove spell).
     */
    private void onSlotRightClick(int slotIndex) {
        spellManager.removeSpellFromSlot(slotIndex);
        updateSpellData();
    }

    public List<SpellSlotWidget> getSlots() {
        return slots;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    /**
     * Gets the position of a specific slot.
     */
    public Vector2i getSlotPosition(int index) {
        if (index >= 0 && index < slots.size()) {
            return slots.get(index).getPosition();
        }
        return new Vector2i(0, 0);
    }

    /**
     * Invalidates the layout cache, forcing a rebuild on next update.
     * Call this when screen is resized.
     */
    public void invalidateLayout() {
        layoutInitialized = false;
    }
}