package de.one_piece_api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.gui.OnePieceScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.util.SpellRender;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Modern overlay widget for spell selection with visual polish.
 *
 * Features:
 * - Semi-transparent dark background overlay
 * - Animated fade-in effect
 * - Hover highlighting for spell icons
 * - Smooth grid layout with proper spacing
 * - Click outside to cancel
 * - Centered on screen
 *
 * Thread Safety: Not thread-safe. Should only be accessed from the render thread.
 */
public class SpellSelectionOverlay implements Drawable, Element {

    // ==================== Constants ====================

    private static final int COLUMNS = 8; // Reduced for better visual balance
    private static final int ICON_SIZE = 24; // Larger for better visibility
    private static final int ICON_PADDING = 4;
    private static final int PANEL_PADDING = 16;
    private static final int TITLE_HEIGHT = 20;

    // Colors (ARGB format)
    private static final int BACKGROUND_OVERLAY = 0xC0000000; // Semi-transparent black
    private static final int PANEL_BACKGROUND = 0xE0202020;   // Dark gray panel
    private static final int PANEL_BORDER = 0xFF4A4A4A;       // Light gray border
    private static final int HOVER_HIGHLIGHT = 0x80FFFFFF;    // Semi-transparent white
    private static final int TITLE_COLOR = 0xFFFFFFFF;        // White

    // Animation
    private static final float FADE_IN_DURATION = 0.15f; // seconds

    // ==================== Immutable State ====================

    private final int targetSlotIndex;
    private final List<RegistryEntry<Spell>> availableSpells;
    private final BiConsumer<Integer, RegistryEntry<Spell>> onSpellSelected;
    private final Runnable onCancel;

    // ==================== Mutable State ====================

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int gridStartX;
    private int gridStartY;
    private int rows;

    private long openTime;
    private float fadeAlpha = 0f;

    // ==================== Constructor ====================

    /**
     * Creates a new spell selection overlay.
     *
     * @param targetSlotIndex The slot index to assign the selected spell to
     * @param availableSpells List of spells the player can choose from
     * @param onSpellSelected Callback when a spell is selected
     * @param onCancel Callback when selection is cancelled
     */
    public SpellSelectionOverlay(int targetSlotIndex,
                                 List<RegistryEntry<Spell>> availableSpells,
                                 BiConsumer<Integer, RegistryEntry<Spell>> onSpellSelected,
                                 Runnable onCancel) {
        this.targetSlotIndex = targetSlotIndex;
        this.availableSpells = Objects.requireNonNull(availableSpells, "Available spells cannot be null");
        this.onSpellSelected = Objects.requireNonNull(onSpellSelected, "Selection callback cannot be null");
        this.onCancel = Objects.requireNonNull(onCancel, "Cancel callback cannot be null");
        this.openTime = System.currentTimeMillis();

        calculateLayout();
    }

    // ==================== Layout ====================

    /**
     * Calculates the layout dimensions and positions for the overlay.
     */
    private void calculateLayout() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int spellCount = availableSpells.size();
        if (spellCount == 0) {
            // Empty state
            panelWidth = 200;
            panelHeight = 100;
            rows = 0;
        } else {
            // Calculate grid dimensions
            int columns = Math.min(spellCount, COLUMNS);
            rows = (int) Math.ceil((double) spellCount / COLUMNS);

            int gridWidth = columns * (ICON_SIZE + ICON_PADDING) - ICON_PADDING;
            int gridHeight = rows * (ICON_SIZE + ICON_PADDING) - ICON_PADDING;

            panelWidth = gridWidth + PANEL_PADDING * 2;
            panelHeight = gridHeight + PANEL_PADDING * 2 + TITLE_HEIGHT;
        }

        // Center on screen
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;

        // Calculate grid start position
        gridStartX = panelX + PANEL_PADDING;
        gridStartY = panelY + PANEL_PADDING + TITLE_HEIGHT;
    }

    // ==================== Rendering ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Objects.requireNonNull(context, "DrawContext cannot be null");

        updateAnimation(delta);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        try {
            renderBackgroundOverlay(context);
            renderPanel(context);
            renderTitle(context);
            renderSpellGrid(context, mouseX, mouseY);
        } finally {
            RenderSystem.disableBlend();
        }
    }

    /**
     * Updates the fade-in animation.
     */
    private void updateAnimation(float delta) {
        float elapsed = (System.currentTimeMillis() - openTime) / 1000f;
        fadeAlpha = MathHelper.clamp(elapsed / FADE_IN_DURATION, 0f, 1f);
    }

    /**
     * Renders the semi-transparent background overlay.
     */
    private void renderBackgroundOverlay(DrawContext context) {
        int alpha = (int) (fadeAlpha * ((BACKGROUND_OVERLAY >> 24) & 0xFF));
        int color = (alpha << 24) | (BACKGROUND_OVERLAY & 0x00FFFFFF);

        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), color);
    }

    /**
     * Renders the main panel with border.
     */
    private void renderPanel(DrawContext context) {
        // Scale alpha for fade-in
        int bgAlpha = (int) (fadeAlpha * ((PANEL_BACKGROUND >> 24) & 0xFF));
        int bgColor = (bgAlpha << 24) | (PANEL_BACKGROUND & 0x00FFFFFF);

        int borderAlpha = (int) (fadeAlpha * ((PANEL_BORDER >> 24) & 0xFF));
        int borderColor = (borderAlpha << 24) | (PANEL_BORDER & 0x00FFFFFF);

        // Draw border
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, borderColor);

        // Draw background
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgColor);
    }

    /**
     * Renders the title text.
     */
    private void renderTitle(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        Text title = Text.literal("Select Spell");

        int titleX = panelX + (panelWidth - client.textRenderer.getWidth(title)) / 2;
        int titleY = panelY + PANEL_PADDING;

        int titleAlpha = (int) (fadeAlpha * 255);
        int titleColor = (titleAlpha << 24) | (TITLE_COLOR & 0x00FFFFFF);

        context.drawText(client.textRenderer, title, titleX, titleY, titleColor, false);
    }

    /**
     * Renders the grid of spell icons.
     */
    private void renderSpellGrid(DrawContext context, int mouseX, int mouseY) {
        if (availableSpells.isEmpty()) {
            renderEmptyState(context);
            return;
        }

        int index = 0;
        for (int row = 0; row < rows && index < availableSpells.size(); row++) {
            for (int col = 0; col < COLUMNS && index < availableSpells.size(); col++) {
                RegistryEntry<Spell> spell = availableSpells.get(index);
                if (spell != null) {
                    int x = gridStartX + col * (ICON_SIZE + ICON_PADDING);
                    int y = gridStartY + row * (ICON_SIZE + ICON_PADDING);

                    boolean hovered = isMouseOverIcon(mouseX, mouseY, x, y);
                    renderSpellIcon(context, spell, x, y, hovered);
                }
                index++;
            }
        }
    }

    /**
     * Renders a single spell icon with optional hover effect.
     */
    private void renderSpellIcon(DrawContext context, RegistryEntry<Spell> spell, int x, int y, boolean hovered) {
        try {
            Identifier spellId = Identifier.of(spell.getIdAsString());
            Identifier iconId = SpellRender.iconTexture(spellId);

            // Draw hover highlight background
            if (hovered) {
                int highlightAlpha = (int) (fadeAlpha * ((HOVER_HIGHLIGHT >> 24) & 0xFF));
                int highlightColor = (highlightAlpha << 24) | (HOVER_HIGHLIGHT & 0x00FFFFFF);
                context.fill(x - 2, y - 2, x + ICON_SIZE + 2, y + ICON_SIZE + 2, highlightColor);
            }

            // Draw icon with fade-in alpha
            RenderSystem.setShaderColor(1f, 1f, 1f, fadeAlpha);
            context.drawTexture(iconId, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        } catch (Exception e) {
            OnePieceRPG.LOGGER.warn("Failed to render spell icon: {}", spell.getIdAsString(), e);
        }
    }

    /**
     * Renders a message when no spells are available.
     */
    private void renderEmptyState(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        Text message = Text.literal("No spells available");

        int messageX = panelX + (panelWidth - client.textRenderer.getWidth(message)) / 2;
        int messageY = gridStartY;

        int alpha = (int) (fadeAlpha * 255);
        int color = (alpha << 24) | 0x00AAAAAA;

        context.drawText(client.textRenderer, message, messageX, messageY, color, false);
    }

    // ==================== Input Handling ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) { // Only handle left clicks
            return false;
        }

        // Check if clicked on a spell icon
        int index = 0;
        for (int row = 0; row < rows && index < availableSpells.size(); row++) {
            for (int col = 0; col < COLUMNS && index < availableSpells.size(); col++) {
                RegistryEntry<Spell> spell = availableSpells.get(index);
                if (spell != null) {
                    int x = gridStartX + col * (ICON_SIZE + ICON_PADDING);
                    int y = gridStartY + row * (ICON_SIZE + ICON_PADDING);

                    if (isMouseOverIcon(mouseX, mouseY, x, y)) {
                        onSpellSelected.accept(targetSlotIndex, spell);
                        return true;
                    }
                }
                index++;
            }
        }

        // Clicked outside spell icons - cancel
        onCancel.run();
        return true;
    }

    /**
     * Checks if the mouse is over a specific icon.
     */
    private boolean isMouseOverIcon(double mouseX, double mouseY, int iconX, int iconY) {
        return mouseX >= iconX && mouseX < iconX + ICON_SIZE &&
                mouseY >= iconY && mouseY < iconY + ICON_SIZE;
    }

    // ==================== Element Interface ====================

    @Override
    public void setFocused(boolean focused) {
        // Always focused when visible
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    // ==================== Getters ====================

    /**
     * @return The target slot index for the selected spell
     */
    public int getTargetSlotIndex() {
        return targetSlotIndex;
    }

    /**
     * @return Number of available spells
     */
    public int getSpellCount() {
        return availableSpells.size();
    }
}