package de.one_piece_api.screen.widget.main;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
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
 * <p>
 * This overlay provides an elegant interface for selecting spells to assign to
 * spell slots. It features smooth animations, hover effects, and a centered grid
 * layout that adapts to the number of available spells.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Semi-transparent dark background overlay</li>
 *     <li>Animated fade-in effect for smooth appearance</li>
 *     <li>Hover highlighting for spell icons</li>
 *     <li>Smooth grid layout with proper spacing</li>
 *     <li>Click outside to cancel selection</li>
 *     <li>Centered on screen for accessibility</li>
 *     <li>Empty state handling with message</li>
 * </ul>
 *
 * <h2>User Interaction:</h2>
 * <ul>
 *     <li>Click on a spell icon to select it</li>
 *     <li>Click outside the panel to cancel</li>
 *     <li>Hover over icons for visual feedback</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * Not thread-safe. Should only be accessed from the render thread.
 *
 * @see Drawable
 * @see Element
 * @see Spell
 */
public class SpellSelectionOverlay implements Drawable, Element {

    // ==================== Constants ====================

    /** Number of spell icons per row in the grid */
    private static final int COLUMNS = 8;

    /** Size of each spell icon in pixels */
    private static final int ICON_SIZE = 24;

    /** Spacing between icons in pixels */
    private static final int ICON_PADDING = 4;

    /** Padding around the panel content in pixels */
    private static final int PANEL_PADDING = 16;

    /** Height reserved for the title in pixels */
    private static final int TITLE_HEIGHT = 20;

    // Colors (ARGB format)
    /** Semi-transparent black for full-screen overlay */
    private static final int BACKGROUND_OVERLAY = 0xC0000000;

    /** Dark gray for panel background */
    private static final int PANEL_BACKGROUND = 0xE0202020;

    /** Light gray for panel border */
    private static final int PANEL_BORDER = 0xFF4A4A4A;

    /** Semi-transparent white for hover highlight */
    private static final int HOVER_HIGHLIGHT = 0x80FFFFFF;

    /** White for title text */
    private static final int TITLE_COLOR = 0xFFFFFFFF;

    // Animation
    /** Duration of fade-in animation in seconds */
    private static final float FADE_IN_DURATION = 0.15f;

    // ==================== Immutable State ====================

    /** The spell slot index where the selected spell will be assigned */
    private final int targetSlotIndex;

    /** List of spells available for selection */
    private final List<RegistryEntry<Spell>> availableSpells;

    /** Callback invoked when a spell is selected */
    private final BiConsumer<Integer, RegistryEntry<Spell>> onSpellSelected;

    /** Callback invoked when selection is cancelled */
    private final Runnable onCancel;

    // ==================== Mutable State ====================

    /** X-coordinate of the panel's left edge */
    private int panelX;

    /** Y-coordinate of the panel's top edge */
    private int panelY;

    /** Width of the panel in pixels */
    private int panelWidth;

    /** Height of the panel in pixels */
    private int panelHeight;

    /** X-coordinate where the spell grid begins */
    private int gridStartX;

    /** Y-coordinate where the spell grid begins */
    private int gridStartY;

    /** Number of rows in the spell grid */
    private int rows;

    /** Timestamp when the overlay was opened (for animation) */
    private final long openTime;

    /** Current fade animation alpha value (0.0 to 1.0) */
    private float fadeAlpha = 0f;

    // ==================== Constructor ====================

    /**
     * Creates a new spell selection overlay.
     * <p>
     * Initializes the overlay with the target slot and available spells,
     * calculates the layout to center it on screen, and starts the fade-in animation.
     *
     * @param targetSlotIndex the slot index to assign the selected spell to
     * @param availableSpells list of spells the player can choose from
     * @param onSpellSelected callback invoked when a spell is selected (slot index, spell)
     * @param onCancel callback invoked when selection is cancelled
     * @throws NullPointerException if availableSpells, onSpellSelected, or onCancel is null
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
     * <p>
     * Determines the panel size based on the number of spells, arranging
     * them in a grid with {@link #COLUMNS} per row. Centers the panel on
     * the screen and calculates the starting position for the spell grid.
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

    /**
     * Renders the spell selection overlay.
     * <p>
     * Rendering order:
     * <ol>
     *     <li>Full-screen semi-transparent background overlay</li>
     *     <li>Panel background with border</li>
     *     <li>Title text centered at top</li>
     *     <li>Spell grid or empty state message</li>
     * </ol>
     * All elements respect the fade-in animation alpha value.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     * @throws NullPointerException if context is null
     */
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
     * Updates the fade-in animation based on elapsed time.
     * <p>
     * Calculates the current alpha value by interpolating from 0 to 1
     * over {@link #FADE_IN_DURATION} seconds.
     *
     * @param delta the frame delta time (unused, animation is time-based)
     */
    private void updateAnimation(float delta) {
        float elapsed = (System.currentTimeMillis() - openTime) / 1000f;
        fadeAlpha = MathHelper.clamp(elapsed / FADE_IN_DURATION, 0f, 1f);
    }

    /**
     * Renders the semi-transparent background overlay.
     * <p>
     * Fills the entire screen with a dark overlay to dim the content behind
     * the spell selection panel. Alpha is animated during fade-in.
     *
     * @param context the drawing context
     */
    private void renderBackgroundOverlay(DrawContext context) {
        int alpha = (int) (fadeAlpha * ((BACKGROUND_OVERLAY >> 24) & 0xFF));
        int color = (alpha << 24) | (BACKGROUND_OVERLAY & 0x00FFFFFF);

        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), color);
    }

    /**
     * Renders the main panel with border.
     * <p>
     * Draws a bordered rectangle containing the spell selection interface.
     * Both border and background colors are animated during fade-in.
     *
     * @param context the drawing context
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
     * Renders the title text at the top of the panel.
     * <p>
     * Displays "Select Spell" centered horizontally within the panel.
     * Text alpha is animated during fade-in.
     *
     * @param context the drawing context
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
     * Renders the grid of spell icons or empty state message.
     * <p>
     * If spells are available, renders them in a grid layout with hover
     * effects. If no spells are available, displays a centered message.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
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
     * <p>
     * Draws a semi-transparent highlight behind hovered icons for visual
     * feedback. Icon opacity respects the fade-in animation.
     *
     * @param context the drawing context
     * @param spell the spell to render
     * @param x the icon x-coordinate
     * @param y the icon y-coordinate
     * @param hovered whether the mouse is hovering over this icon
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
     * <p>
     * Displays "No spells available" centered in the panel's content area.
     * Text alpha is animated during fade-in.
     *
     * @param context the drawing context
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

    /**
     * Handles mouse click events.
     * <p>
     * Processes clicks in the following priority:
     * <ol>
     *     <li>Click on spell icon: Selects the spell and invokes callback</li>
     *     <li>Click outside icons: Cancels selection and invokes cancel callback</li>
     * </ol>
     * Only left mouse button clicks are handled.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
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
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param iconX the icon x-coordinate
     * @param iconY the icon y-coordinate
     * @return {@code true} if mouse is within icon bounds, {@code false} otherwise
     */
    private boolean isMouseOverIcon(double mouseX, double mouseY, int iconX, int iconY) {
        return mouseX >= iconX && mouseX < iconX + ICON_SIZE &&
                mouseY >= iconY && mouseY < iconY + ICON_SIZE;
    }

    // ==================== Element Interface ====================

    /**
     * Sets focus state for this element.
     * <p>
     * The overlay is always focused when visible to capture all input.
     *
     * @param focused the focus state (ignored)
     */
    @Override
    public void setFocused(boolean focused) {
        // Always focused when visible
    }

    /**
     * Checks if this element has focus.
     * <p>
     * The overlay is always focused when visible to capture all input.
     *
     * @return {@code true} always
     */
    @Override
    public boolean isFocused() {
        return true;
    }

    // ==================== Getters ====================

    /**
     * Gets the target slot index for the selected spell.
     *
     * @return the slot index where the selected spell will be assigned
     */
    public int getTargetSlotIndex() {
        return targetSlotIndex;
    }

    /**
     * Gets the number of available spells.
     *
     * @return the count of spells in the selection grid
     */
    public int getSpellCount() {
        return availableSpells.size();
    }
}