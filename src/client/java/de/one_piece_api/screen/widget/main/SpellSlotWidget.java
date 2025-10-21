package de.one_piece_api.screen.widget.main;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.init.MyShaders;
import de.one_piece_api.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.util.SpellRender;
import org.joml.Vector2i;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Widget representing a single spell slot in the skill tree UI.
 * <p>
 * This widget displays a spell icon in a slot with visual feedback for learned/unlearned
 * states. It handles click interactions and provides hover information for tooltips.
 * The position and size are immutable after construction to ensure layout consistency.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Renders spell icon with learned/unlearned visual states</li>
 *     <li>Handles click interactions (left/right click with separate handlers)</li>
 *     <li>Provides hover information for tooltips</li>
 *     <li>Applies grayscale shader to unlearned spell icons</li>
 *     <li>Immutable position and size after construction</li>
 * </ul>
 *
 * <h2>Visual States:</h2>
 * <ul>
 *     <li>Learned: Full-color spell icon</li>
 *     <li>Unlearned: Grayscale spell icon with "Not Learned!" indicator</li>
 *     <li>Empty: Background only, no icon</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * Not thread-safe. Should only be accessed from the render thread.
 *
 * @see Drawable
 * @see Element
 * @see Spell
 */
public class SpellSlotWidget implements Drawable, Element {

    // ==================== Constants ====================

    /** Texture identifier for the slot background */
    private static final Identifier SLOT_BACKGROUND = OnePieceRPG.id("textures/gui/skill/small_rect.png");

    /** Padding between slot edge and icon in pixels */
    private static final int ICON_PADDING = 3;

    /** Prefix for spell translation keys */
    private static final String SPELL_NAME_PREFIX = "spell.";

    /** Suffix for spell name translation keys */
    private static final String SPELL_NAME_SUFFIX = ".name";

    /** Suffix for spell description translation keys */
    private static final String SPELL_DESC_SUFFIX = ".description";

    /** Text shown when spell is not learned */
    private static final String NOT_LEARNED_TEXT = "Not Learned!";

    // ==================== Immutable State ====================

    /** X-coordinate in screen space (immutable) */
    private final int x;

    /** Y-coordinate in screen space (immutable) */
    private final int y;

    /** Total size of the slot including background (immutable) */
    private final int size;

    /** Size of the icon after padding (immutable) */
    private final int iconSize;

    /** Index of this slot in the spell bar (immutable) */
    private final int slotIndex;

    // ==================== Mutable State ====================

    /** The spell currently assigned to this slot */
    private RegistryEntry<Spell> spell;

    /** Whether the player has learned the spell in this slot */
    private boolean learned;

    // Event handlers
    /** Handler called when the slot is left-clicked */
    private Consumer<SpellSlotWidget> onLeftClick;

    /** Handler called when the slot is right-clicked */
    private Consumer<SpellSlotWidget> onRightClick;

    /** Handler called when the mouse hovers over the slot */
    private Consumer<HoverInfo> onHover;

    // ==================== Constructor ====================

    /**
     * Creates a new spell slot widget.
     * <p>
     * The position and size are immutable after construction. Icon size
     * is automatically calculated based on the total size and padding.
     *
     * @param x x-position in screen coordinates
     * @param y y-position in screen coordinates
     * @param size total size of the slot (including background) in pixels
     * @param slotIndex index of this slot in the spell bar (0-based)
     * @throws IllegalArgumentException if size is too small (less than 2 * padding) or slotIndex is negative
     */
    public SpellSlotWidget(int x, int y, int size, int slotIndex) {
        if (size < ICON_PADDING * 2) {
            throw new IllegalArgumentException(
                    "Slot size must be at least " + (ICON_PADDING * 2) + ", got: " + size
            );
        }
        if (slotIndex < 0) {
            throw new IllegalArgumentException(
                    "Slot index cannot be negative, got: " + slotIndex
            );
        }

        this.x = x;
        this.y = y;
        this.size = size;
        this.iconSize = size - ICON_PADDING * 2;
        this.slotIndex = slotIndex;
    }

    // ==================== State Management ====================

    /**
     * Updates the spell displayed in this slot.
     * <p>
     * This should be called whenever the player's spell selection changes
     * or when spell data is synced from the server.
     *
     * @param spell the spell to display, or {@code null} to clear the slot
     * @param learned whether the player has learned this spell
     */
    public void setSpell(RegistryEntry<Spell> spell, boolean learned) {
        this.spell = spell;
        this.learned = learned;
    }

    /**
     * Clears the spell from this slot.
     * <p>
     * Equivalent to calling {@code setSpell(null, false)}.
     */
    public void clearSpell() {
        this.spell = null;
        this.learned = false;
    }

    /**
     * Checks if this slot has a spell assigned.
     *
     * @return {@code true} if a spell is assigned, {@code false} if slot is empty
     */
    public boolean hasSpell() {
        return spell != null;
    }

    /**
     * Checks if the spell in this slot is learned.
     *
     * @return {@code true} if the spell is learned, {@code false} otherwise
     */
    public boolean isLearned() {
        return learned;
    }

    // ==================== Event Handlers ====================

    /**
     * Sets the handler for left-click events.
     * <p>
     * Typically used to open spell selection overlay.
     *
     * @param handler the handler to call when the slot is left-clicked, or {@code null} to remove
     */
    public void setOnLeftClick(Consumer<SpellSlotWidget> handler) {
        this.onLeftClick = handler;
    }

    /**
     * Sets the handler for right-click events.
     * <p>
     * Typically used to remove spell from slot.
     *
     * @param handler the handler to call when the slot is right-clicked, or {@code null} to remove
     */
    public void setOnRightClick(Consumer<SpellSlotWidget> handler) {
        this.onRightClick = handler;
    }

    /**
     * Sets the handler for hover events.
     * <p>
     * Called when the mouse hovers over the slot, providing information
     * for tooltip display.
     *
     * @param handler the handler to call when hovering, or {@code null} to remove
     */
    public void setOnHover(Consumer<HoverInfo> handler) {
        this.onHover = handler;
    }

    // ==================== Rendering ====================

    /**
     * Renders the spell slot including background and icon.
     * <p>
     * Rendering order:
     * <ol>
     *     <li>Slot background texture</li>
     *     <li>Spell icon (if present) - full color or grayscale based on learned state</li>
     *     <li>Hover handling for tooltip display</li>
     * </ol>
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time (unused)
     * @throws NullPointerException if context is null
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Objects.requireNonNull(context, "DrawContext cannot be null");

        renderSlotBackground(context);
        renderSpellIcon(context);
        handleHover(mouseX, mouseY);
    }

    /**
     * Renders the slot background texture.
     *
     * @param context the drawing context
     */
    private void renderSlotBackground(DrawContext context) {
        context.drawTexture(
                SLOT_BACKGROUND,
                x, y,
                0, 0,
                size, size,
                size, size
        );
    }

    /**
     * Renders the spell icon if a spell is assigned.
     * <p>
     * Applies a grayscale shader effect for unlearned spells to provide
     * visual feedback about the spell's availability.
     *
     * @param context the drawing context
     */
    private void renderSpellIcon(DrawContext context) {
        if (spell == null) {
            return;
        }

        Optional<Identifier> iconId = getSpellIconId();
        if (iconId.isEmpty()) {
            return;
        }

        int iconX = x + ICON_PADDING;
        int iconY = y + ICON_PADDING;

        if (learned) {
            renderLearnedIcon(context, iconId.get(), iconX, iconY);
        } else {
            renderUnlearnedIcon(context, iconId.get(), iconX, iconY);
        }
    }

    /**
     * Gets the icon identifier for the current spell.
     * <p>
     * Uses {@link SpellRender} to resolve the spell ID to its icon texture.
     * Logs a warning if the icon cannot be retrieved.
     *
     * @return an {@link Optional} containing the icon identifier, or empty if unavailable
     */
    private Optional<Identifier> getSpellIconId() {
        try {
            String spellIdString = spell.getIdAsString();
            Identifier spellId = Identifier.of(spellIdString);
            return Optional.of(SpellRender.iconTexture(spellId));
        } catch (Exception e) {
            OnePieceRPG.LOGGER.warn("Failed to get spell icon for: {}", spell.getIdAsString(), e);
            return Optional.empty();
        }
    }

    /**
     * Renders a learned spell icon in full color.
     *
     * @param context the drawing context
     * @param iconId the icon texture identifier
     * @param iconX the icon x-coordinate
     * @param iconY the icon y-coordinate
     */
    private void renderLearnedIcon(DrawContext context, Identifier iconId, int iconX, int iconY) {
        context.drawTexture(
                iconId,
                iconX, iconY,
                0, 0,
                iconSize, iconSize,
                iconSize, iconSize
        );
    }

    /**
     * Renders an unlearned spell icon with grayscale effect.
     * <p>
     * Uses a custom shader to desaturate the icon, providing clear visual
     * feedback that the spell is not yet available.
     *
     * @param context the drawing context
     * @param iconId the icon texture identifier
     * @param iconX the icon x-coordinate
     * @param iconY the icon y-coordinate
     */
    private void renderUnlearnedIcon(DrawContext context, Identifier iconId, int iconX, int iconY) {
        RenderUtil.drawTexture(
                context,
                MyShaders::getGrayscaleShader,
                iconId,
                iconX, iconY,
                0, 0,
                iconSize, iconSize,
                iconSize, iconSize
        );
    }

    /**
     * Handles hover state and triggers hover callback.
     * <p>
     * Creates and passes {@link HoverInfo} to the hover handler if the
     * mouse is over the slot and a handler is registered.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     */
    private void handleHover(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY) || onHover == null) {
            return;
        }

        Optional<HoverInfo> info = createHoverInfo();
        info.ifPresent(onHover);
    }

    // ==================== Input Handling ====================

    /**
     * Handles mouse click events on the slot.
     * <p>
     * Supports left-click (typically for spell selection) and right-click
     * (typically for spell removal). Only processes clicks when the mouse
     * is within the slot bounds.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        return switch (button) {
            case 0 -> handleLeftClick();  // Left mouse button
            case 1 -> handleRightClick(); // Right mouse button
            default -> false;
        };
    }

    /**
     * Handles left-click events by invoking the registered handler.
     *
     * @return {@code true} if a handler was called, {@code false} otherwise
     */
    private boolean handleLeftClick() {
        if (onLeftClick != null) {
            onLeftClick.accept(this);
            return true;
        }
        return false;
    }

    /**
     * Handles right-click events by invoking the registered handler.
     *
     * @return {@code true} if a handler was called, {@code false} otherwise
     */
    private boolean handleRightClick() {
        if (onRightClick != null) {
            onRightClick.accept(this);
            return true;
        }
        return false;
    }

    /**
     * Checks if the mouse is over this slot.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if mouse is within slot bounds, {@code false} otherwise
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + size &&
                mouseY >= y && mouseY < y + size;
    }

    // ==================== Hover Info ====================

    /**
     * Creates hover information for the current spell.
     * <p>
     * Constructs a {@link HoverInfo} object containing the spell's translated
     * name, description, and learned status. Returns empty if no spell is
     * assigned or if an error occurs during translation.
     *
     * @return an {@link Optional} containing hover info, or empty if unavailable
     */
    private Optional<HoverInfo> createHoverInfo() {
        if (spell == null) {
            return Optional.empty();
        }

        try {
            String spellId = spell.getIdAsString();
            String translationKey = spellId.replace(":", ".");

            Text title = createTitleText(translationKey);
            Text description = createDescriptionText(translationKey);
            Text learnedStatus = createLearnedStatusText();

            return Optional.of(new HoverInfo(title, description, learnedStatus, Text.empty()));

        } catch (Exception e) {
            OnePieceRPG.LOGGER.warn("Failed to create hover info for spell: {}", spell.getIdAsString(), e);
            return Optional.empty();
        }
    }

    /**
     * Creates the title text for the spell tooltip.
     *
     * @param translationKey the spell's translation key base
     * @return white-colored translated spell name
     */
    private Text createTitleText(String translationKey) {
        String nameKey = SPELL_NAME_PREFIX + translationKey + SPELL_NAME_SUFFIX;
        return Text.translatable(nameKey).formatted(Formatting.WHITE);
    }

    /**
     * Creates the description text for the spell tooltip.
     *
     * @param translationKey the spell's translation key base
     * @return gray-colored translated spell description
     */
    private Text createDescriptionText(String translationKey) {
        String descKey = SPELL_NAME_PREFIX + translationKey + SPELL_DESC_SUFFIX;
        return Text.translatable(descKey).formatted(Formatting.GRAY);
    }

    /**
     * Creates the learned status text for the spell tooltip.
     *
     * @return empty text if learned, or "Not Learned!" text if not learned
     */
    private Text createLearnedStatusText() {
        return learned ? Text.empty() : Text.literal(NOT_LEARNED_TEXT);
    }

    // ==================== Getters ====================

    /**
     * Gets the slot index in the spell bar.
     *
     * @return the slot index (0-based)
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Gets the position of this slot in screen coordinates.
     *
     * @return the position as a 2D vector
     */
    public Vector2i getPosition() {
        return new Vector2i(x, y);
    }

    /**
     * Gets the spell currently in this slot.
     *
     * @return the spell registry entry, or {@code null} if slot is empty
     */
    public RegistryEntry<Spell> getSpell() {
        return spell;
    }

    /**
     * Gets the total size of this slot.
     *
     * @return the size in pixels
     */
    public int getSize() {
        return size;
    }

    // ==================== Element Interface ====================

    /**
     * Sets focus state for this element.
     * <p>
     * Spell slots don't support focus, so this method does nothing.
     *
     * @param focused the focus state (ignored)
     */
    @Override
    public void setFocused(boolean focused) {
        // Spell slots don't support focus
    }

    /**
     * Checks if this element has focus.
     * <p>
     * Spell slots don't support focus, so this always returns {@code false}.
     *
     * @return {@code false} always
     */
    @Override
    public boolean isFocused() {
        return false;
    }

    // ==================== Inner Classes ====================

    /**
     * Immutable record containing hover information for tooltips.
     * <p>
     * This record holds all text components needed to display a complete
     * tooltip when hovering over a spell slot.
     *
     * @param title the spell name (typically white)
     * @param description the spell description (typically gray)
     * @param extraDescription additional information such as learned status
     * @param advanced advanced tooltip information shown with F3+H enabled
     */
    public record HoverInfo(
            Text title,
            Text description,
            Text extraDescription,
            Text advanced
    ) {
        /**
         * Compact constructor validating all fields are non-null.
         */
        public HoverInfo {
            Objects.requireNonNull(title, "Title cannot be null");
            Objects.requireNonNull(description, "Description cannot be null");
            Objects.requireNonNull(extraDescription, "Extra description cannot be null");
            Objects.requireNonNull(advanced, "Advanced info cannot be null");
        }
    }
}