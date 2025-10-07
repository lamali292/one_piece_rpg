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
 * Manages spell slot widgets and their layout positioning.
 * <p>
 * This manager handles the creation, positioning, and data updates for all spell slot
 * widgets displayed in the skill tree screen. It employs lazy layout calculation to
 * minimize performance impact by only recalculating positions when necessary.
 *
 * <h2>Layout Strategy:</h2>
 * <ul>
 *     <li>Distributes N-1 slots evenly across available width</li>
 *     <li>Positions the last slot at the right edge for visual balance</li>
 *     <li>Caches layout parameters to avoid unnecessary recalculations</li>
 *     <li>Updates spell data independently of layout changes</li>
 * </ul>
 *
 * @see SpellSlotWidget
 * @see SpellManager
 */
public class SpellSlotManager {

    /** List of managed spell slot widgets */
    private final List<SpellSlotWidget> slots = new ArrayList<>();

    /** Spell manager for handling spell operations */
    private final SpellManager spellManager;

    /** Dimension of each spell slot rectangle in pixels */
    private final int rectDim;

    /** Total number of spell slots to display */
    private final int spellSlotCount;

    // Track layout state to avoid unnecessary recalculation
    /** Whether the layout has been initialized */
    private boolean layoutInitialized = false;

    /** Last base X position used for layout calculation */
    private int lastBaseX = -1;

    /** Last base Y position used for layout calculation */
    private int lastBaseY = -1;

    /** Last skill tree width used for layout calculation */
    private int lastSkillTreeWidth = -1;

    /**
     * Creates a new spell slot manager.
     *
     * @param player the client player entity
     * @param rectDim the dimension of each spell slot rectangle in pixels
     * @param skilltreeWidth the width of the skill tree area
     */
    public SpellSlotManager(ClientPlayerEntity player, int rectDim, int skilltreeWidth) {
        this.spellManager = new SpellManager(player);
        this.spellSlotCount = OnePieceRPG.getSpellSlots(player);
        this.rectDim = rectDim;
    }

    /**
     * Updates the positions of all spell slots if the layout parameters have changed.
     * <p>
     * This method uses caching to avoid unnecessary recalculations. Layout is only
     * rebuilt when baseX, baseY, or skilltreeWidth differ from the last call.
     * This optimization significantly reduces CPU usage during rendering.
     *
     * @param baseX the left edge x-coordinate for slot positioning
     * @param baseY the top edge y-coordinate for slot positioning
     * @param skilltreeWidth the total width available for slot distribution
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
     * <p>
     * This method is useful after screen resize or significant UI changes when
     * the layout must be recalculated even if the parameters appear unchanged.
     *
     * @param baseX the left edge x-coordinate for slot positioning
     * @param baseY the top edge y-coordinate for slot positioning
     * @param skilltreeWidth the total width available for slot distribution
     */
    public void forceLayoutUpdate(int baseX, int baseY, int skilltreeWidth) {
        lastBaseX = baseX;
        lastBaseY = baseY;
        lastSkillTreeWidth = skilltreeWidth;
        layoutInitialized = true;
        rebuildLayout(baseX, baseY, skilltreeWidth);
    }

    /**
     * Performs the actual layout calculation and widget creation.
     * <p>
     * This method implements a custom spacing algorithm:
     * <ol>
     *     <li>Calculates even spacing between the first N-1 slots</li>
     *     <li>Positions the last slot flush against the right edge</li>
     * </ol>
     * This creates a visually balanced layout with clear boundaries.
     *
     * @param baseX the left edge x-coordinate for slot positioning
     * @param baseY the top edge y-coordinate for slot positioning
     * @param skilltreeWidth the total width available for slot distribution
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
     * Updates spell data for all slots from the spell manager.
     * <p>
     * This method synchronizes the displayed spell data with the current player
     * spell selection. It should be called when spells change, not every frame.
     * Each slot is updated with its corresponding spell and whether that spell
     * has been learned by the player.
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
     * Sets up event handlers for a spell slot widget.
     * <p>
     * Configures left-click (for spell selection) and right-click (for spell
     * removal) handlers for the given slot.
     *
     * @param slot the slot widget to configure
     */
    private void setupSlotHandlers(SpellSlotWidget slot) {
        slot.setOnLeftClick(s -> onSlotLeftClick(s.getSlotIndex()));
        slot.setOnRightClick(s -> onSlotRightClick(s.getSlotIndex()));
    }

    /**
     * Handles left-click events on spell slots.
     * <p>
     * Left-clicking a slot is intended to open the spell selection overlay.
     * This is handled by the parent screen, so this method serves as a hook point.
     *
     * @param slotIndex the index of the clicked slot
     */
    private void onSlotLeftClick(int slotIndex) {
        // Handled by the screen to show overlay
    }

    /**
     * Handles right-click events on spell slots.
     * <p>
     * Right-clicking a slot removes the spell from that slot and updates
     * the display to reflect the change.
     *
     * @param slotIndex the index of the clicked slot
     */
    private void onSlotRightClick(int slotIndex) {
        spellManager.removeSpellFromSlot(slotIndex);
        updateSpellData();
    }

    /**
     * Gets the list of all spell slot widgets.
     *
     * @return an immutable view of the slot widget list
     */
    public List<SpellSlotWidget> getSlots() {
        return slots;
    }

    /**
     * Gets the spell manager instance.
     *
     * @return the spell manager handling spell operations
     */
    public SpellManager getSpellManager() {
        return spellManager;
    }

    /**
     * Gets the position of a specific spell slot.
     *
     * @param index the index of the slot (0-based)
     * @return the position vector of the slot, or (0,0) if index is invalid
     */
    public Vector2i getSlotPosition(int index) {
        if (index >= 0 && index < slots.size()) {
            return slots.get(index).getPosition();
        }
        return new Vector2i(0, 0);
    }

    /**
     * Invalidates the layout cache, forcing a rebuild on the next update.
     * <p>
     * Call this method when the screen is resized or when layout parameters
     * may have changed in a way that bypasses normal change detection.
     */
    public void invalidateLayout() {
        layoutInitialized = false;
    }
}