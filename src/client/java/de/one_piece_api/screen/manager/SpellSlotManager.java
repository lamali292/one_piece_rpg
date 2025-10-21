package de.one_piece_api.screen.manager;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.screen.widget.main.SpellSlotWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.Spell;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class SpellSlotManager {

    private final List<SpellSlotWidget> slots = new ArrayList<>();
    private final SpellManager spellManager;
    private final int rectDim;
    private final int spellSlotCount;

    // Layout tracking - keep this for position management
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
     * Updates layout and spell data if positions have changed
     */
    public void updateLayoutIfNeeded(int baseX, int baseY, int skilltreeWidth) {
        if (layoutInitialized &&
                lastBaseX == baseX &&
                lastBaseY == baseY &&
                lastSkillTreeWidth == skilltreeWidth) {
            return; // Layout unchanged
        }

        lastBaseX = baseX;
        lastBaseY = baseY;
        lastSkillTreeWidth = skilltreeWidth;
        layoutInitialized = true;

        rebuildLayout(baseX, baseY, skilltreeWidth);
    }

    /**
     * Forces a complete layout rebuild
     */
    public void forceLayoutUpdate(int baseX, int baseY, int skilltreeWidth) {
        lastBaseX = baseX;
        lastBaseY = baseY;
        lastSkillTreeWidth = skilltreeWidth;
        layoutInitialized = true;
        rebuildLayout(baseX, baseY, skilltreeWidth);
    }

    /**
     * Rebuilds the layout and loads spell data
     */
    private void rebuildLayout(int baseX, int baseY, int skilltreeWidth) {
        slots.clear();

        int rectOffset = (skilltreeWidth - spellSlotCount * rectDim) / (spellSlotCount - 1);

        // Create all slots except the last one
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

        // Load spell data immediately after creating slots
        updateSpellData();
    }

    /**
     * Updates spell data for all slots - call this whenever spells change
     */
    public void updateSpellData() {
        if (slots.isEmpty()) {
            return; // No slots created yet
        }

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
     * Sets up event handlers for a spell slot
     */
    private void setupSlotHandlers(SpellSlotWidget slot) {
        slot.setOnLeftClick(s -> onSlotLeftClick(s.getSlotIndex()));
        slot.setOnRightClick(s -> onSlotRightClick(s.getSlotIndex()));
    }

    /**
     * Handles left-click on a slot (opens spell selection)
     */
    private void onSlotLeftClick(int slotIndex) {
        // Handled by the screen to show overlay
    }

    /**
     * Handles right-click on a slot (removes spell)
     */
    private void onSlotRightClick(int slotIndex) {
        spellManager.removeSpellFromSlot(slotIndex);
        // Spell data will be updated by screen's processUpdates via SPELL_DATA invalidation
    }

    public List<SpellSlotWidget> getSlots() {
        return slots;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    public Vector2i getSlotPosition(int index) {
        if (index >= 0 && index < slots.size()) {
            return slots.get(index).getPosition();
        }
        return new Vector2i(0, 0);
    }

    public void invalidateLayout() {
        layoutInitialized = false;
    }
}