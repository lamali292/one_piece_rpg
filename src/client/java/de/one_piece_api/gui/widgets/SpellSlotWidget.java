package de.one_piece_api.gui.widgets;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.registries.MyShaders;
import de.one_piece_api.util.RenderUtil;
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
 *
 * Features:
 * - Renders spell icon with learned/unlearned state
 * - Handles click interactions (left/right click)
 * - Provides hover information for tooltips
 * - Immutable position and size after construction
 *
 * Thread Safety: Not thread-safe. Should only be accessed from the render thread.
 */
public class SpellSlotWidget implements Drawable, Element {

    // ==================== Constants ====================

    private static final Identifier SLOT_BACKGROUND = OnePieceRPG.id("textures/gui/skill/small_rect.png");
    private static final int ICON_PADDING = 3;
    private static final String SPELL_NAME_PREFIX = "spell.";
    private static final String SPELL_NAME_SUFFIX = ".name";
    private static final String SPELL_DESC_SUFFIX = ".description";
    private static final String NOT_LEARNED_TEXT = "Not Learned!";

    // ==================== Immutable State ====================

    private final int x;
    private final int y;
    private final int size;
    private final int iconSize;
    private final int slotIndex;

    // ==================== Mutable State ====================

    private RegistryEntry<Spell> spell;
    private boolean learned;

    // Event handlers
    private Consumer<SpellSlotWidget> onLeftClick;
    private Consumer<SpellSlotWidget> onRightClick;
    private Consumer<HoverInfo> onHover;

    // ==================== Constructor ====================

    /**
     * Creates a new spell slot widget.
     *
     * @param x X position in screen coordinates
     * @param y Y position in screen coordinates
     * @param size Total size of the slot (including background)
     * @param slotIndex Index of this slot in the spell bar
     * @throws IllegalArgumentException if size is too small or slotIndex is negative
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
     *
     * @param spell The spell to display, or null to clear the slot
     * @param learned Whether the player has learned this spell
     */
    public void setSpell(RegistryEntry<Spell> spell, boolean learned) {
        this.spell = spell;
        this.learned = learned;
    }

    /**
     * Clears the spell from this slot.
     */
    public void clearSpell() {
        this.spell = null;
        this.learned = false;
    }

    /**
     * @return true if this slot has a spell assigned
     */
    public boolean hasSpell() {
        return spell != null;
    }

    /**
     * @return true if the spell in this slot is learned
     */
    public boolean isLearned() {
        return learned;
    }

    // ==================== Event Handlers ====================

    /**
     * Sets the handler for left-click events.
     *
     * @param handler Called when the slot is left-clicked
     */
    public void setOnLeftClick(Consumer<SpellSlotWidget> handler) {
        this.onLeftClick = handler;
    }

    /**
     * Sets the handler for right-click events.
     *
     * @param handler Called when the slot is right-clicked
     */
    public void setOnRightClick(Consumer<SpellSlotWidget> handler) {
        this.onRightClick = handler;
    }

    /**
     * Sets the handler for hover events.
     *
     * @param handler Called when the mouse hovers over the slot
     */
    public void setOnHover(Consumer<HoverInfo> handler) {
        this.onHover = handler;
    }

    // ==================== Rendering ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Objects.requireNonNull(context, "DrawContext cannot be null");

        renderSlotBackground(context);
        renderSpellIcon(context);
        handleHover(mouseX, mouseY);
    }

    /**
     * Renders the slot background texture.
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
     * Applies grayscale effect for unlearned spells.
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
     * Renders a learned spell icon (full color).
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
     * Renders an unlearned spell icon (grayscale).
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
     */
    private void handleHover(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY) || onHover == null) {
            return;
        }

        Optional<HoverInfo> info = createHoverInfo();
        info.ifPresent(onHover);
    }

    // ==================== Input Handling ====================

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
     * Handles left-click events.
     *
     * @return true if the click was handled
     */
    private boolean handleLeftClick() {
        if (onLeftClick != null) {
            onLeftClick.accept(this);
            return true;
        }
        return false;
    }

    /**
     * Handles right-click events.
     *
     * @return true if the click was handled
     */
    private boolean handleRightClick() {
        if (onRightClick != null) {
            onRightClick.accept(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + size &&
                mouseY >= y && mouseY < y + size;
    }

    // ==================== Hover Info ====================

    /**
     * Creates hover information for the current spell.
     *
     * @return HoverInfo if spell is present, empty otherwise
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
     * Creates the title text for the spell.
     */
    private Text createTitleText(String translationKey) {
        String nameKey = SPELL_NAME_PREFIX + translationKey + SPELL_NAME_SUFFIX;
        return Text.translatable(nameKey).formatted(Formatting.WHITE);
    }

    /**
     * Creates the description text for the spell.
     */
    private Text createDescriptionText(String translationKey) {
        String descKey = SPELL_NAME_PREFIX + translationKey + SPELL_DESC_SUFFIX;
        return Text.translatable(descKey).formatted(Formatting.GRAY);
    }

    /**
     * Creates the learned status text.
     */
    private Text createLearnedStatusText() {
        return learned ? Text.empty() : Text.literal(NOT_LEARNED_TEXT);
    }

    // ==================== Getters ====================

    /**
     * @return The slot index in the spell bar
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * @return The position of this slot in screen coordinates
     */
    public Vector2i getPosition() {
        return new Vector2i(x, y);
    }

    /**
     * @return The spell in this slot, or null if empty
     */
    public RegistryEntry<Spell> getSpell() {
        return spell;
    }

    /**
     * @return The size of this slot in pixels
     */
    public int getSize() {
        return size;
    }

    // ==================== Element Interface ====================

    @Override
    public void setFocused(boolean focused) {
        // Spell slots don't support focus
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    // ==================== Inner Classes ====================

    /**
     * Immutable hover information for tooltips.
     *
     * @param title The spell name
     * @param description The spell description
     * @param extraDescription Additional information (e.g., "Not Learned!")
     * @param advanced Advanced tooltip information (shown with F3+H)
     */
    public record HoverInfo(
            Text title,
            Text description,
            Text extraDescription,
            Text advanced
    ) {
        public HoverInfo {
            Objects.requireNonNull(title, "Title cannot be null");
            Objects.requireNonNull(description, "Description cannot be null");
            Objects.requireNonNull(extraDescription, "Extra description cannot be null");
            Objects.requireNonNull(advanced, "Advanced info cannot be null");
        }
    }
}