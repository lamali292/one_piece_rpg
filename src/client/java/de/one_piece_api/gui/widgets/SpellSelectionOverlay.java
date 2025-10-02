package de.one_piece_api.gui.widgets;

import de.one_piece_api.gui.OnePieceScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.util.SpellRender;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Overlay widget for spell selection.
 * Displays a grid of learned spells for the player to choose from.
 */
public class SpellSelectionOverlay implements Drawable, Element {

    private static final int COLUMNS = 10;
    private static final int ICON_SIZE = 10;

    private final int targetSlotIndex;
    private final List<RegistryEntry<Spell>> availableSpells;
    private final BiConsumer<Integer, RegistryEntry<Spell>> onSpellSelected;
    private final Runnable onCancel;

    private int baseX;
    private int baseY;
    private int gridWidth;
    private int gridHeight;
    private int iconMargin;
    private int rows;

    public SpellSelectionOverlay(int targetSlotIndex,
                                 List<RegistryEntry<Spell>> availableSpells,
                                 BiConsumer<Integer, RegistryEntry<Spell>> onSpellSelected,
                                 Runnable onCancel) {
        this.targetSlotIndex = targetSlotIndex;
        this.availableSpells = availableSpells;
        this.onSpellSelected = onSpellSelected;
        this.onCancel = onCancel;
        calculateLayout();
    }

    private void calculateLayout() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int maxWidth = OnePieceScreen.skilltreeWidth;
        int size = availableSpells.size();

        if (size < COLUMNS) {
            rows = 1;
            iconMargin = (maxWidth - ICON_SIZE * size) / (size + 1);
            gridWidth = (ICON_SIZE + iconMargin) * size + iconMargin;
        } else {
            rows = size / COLUMNS + (size % COLUMNS == 0 ? 0 : 1);
            iconMargin = (maxWidth - ICON_SIZE * COLUMNS) / (COLUMNS + 1);
            gridWidth = maxWidth;
        }

        gridHeight = (ICON_SIZE + iconMargin) * rows + iconMargin;
        baseX = (screenWidth - gridWidth) / 2;
        baseY = (screenHeight - gridHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background frame
        context.drawTexture(
                getSkillTreeFrame(),
                baseX, baseY,
                0, 0,
                gridWidth, gridHeight,
                gridWidth, gridHeight
        );

        // Draw spell icons
        int currentRow = 0;
        int currentCol = 0;

        for (RegistryEntry<Spell> spell : availableSpells) {
            if (spell == null) continue;

            Identifier spellId = Identifier.of(spell.getIdAsString());
            Identifier iconId = SpellRender.iconTexture(spellId);

            int x = baseX + iconMargin + currentCol * (ICON_SIZE + iconMargin);
            int y = baseY + iconMargin + currentRow * (ICON_SIZE + iconMargin);

            context.drawTexture(iconId, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

            currentCol++;
            if (currentCol >= COLUMNS) {
                currentCol = 0;
                currentRow++;
                if (currentRow >= rows) break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int currentRow = 0;
        int currentCol = 0;
        int index = 0;

        for (RegistryEntry<Spell> spell : availableSpells) {
            if (spell == null) {
                index++;
                continue;
            }

            int x = baseX + iconMargin + currentCol * (ICON_SIZE + iconMargin);
            int y = baseY + iconMargin + currentRow * (ICON_SIZE + iconMargin);

            if (mouseX >= x && mouseX < x + ICON_SIZE &&
                    mouseY >= y && mouseY < y + ICON_SIZE) {
                onSpellSelected.accept(targetSlotIndex, spell);
                return true;
            }

            index++;
            currentCol++;
            if (currentCol >= COLUMNS) {
                currentCol = 0;
                currentRow++;
                if (currentRow >= rows) break;
            }
        }

        // Clicked outside - cancel
        onCancel.run();
        return true;
    }

    private Identifier getSkillTreeFrame() {
        return de.one_piece_api.OnePieceRPG.id("textures/gui/skill/skilltree.png");
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return true; // Always focused when visible
    }
}