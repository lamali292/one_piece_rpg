package de.one_piece_api.screen.widget.main;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.init.MyFonts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.data.ClientCategoryData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Widget for displaying player level and experience progress.
 * <p>
 * This widget renders a styled level display with an integrated experience bar,
 * showing the player's current level and progress toward the next level. It
 * provides hover tooltips with detailed XP information.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Custom-styled level text using pixel font</li>
 *     <li>Animated experience progress bar</li>
 *     <li>Hover tooltip showing current/required XP</li>
 *     <li>Responsive scaling that matches screen dimensions</li>
 *     <li>Graceful handling of missing experience data</li>
 * </ul>
 *
 * <h2>Visual Components:</h2>
 * <ul>
 *     <li>Level background texture with custom font text</li>
 *     <li>Experience bar with background and progress overlay</li>
 *     <li>Tooltip displaying numerical XP values</li>
 * </ul>
 *
 * @see Drawable
 * @see Element
 * @see ClientCategoryData
 */
public class ExperienceBarWidget implements Drawable, Element {

    // ==================== Constants ====================

    /** Background texture for the level display */
    private static final Identifier LVL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/lvl_bar.png");

    /** Background texture for the experience bar */
    private static final Identifier EXP_BAR_BACKGROUND = Identifier.of("hud/experience_bar_background");

    /** Progress texture for the experience bar */
    private static final Identifier EXP_BAR_PROGRESS = Identifier.of("hud/experience_bar_progress");

    /** Reference width of the experience bar in pixels */
    private static final int EXP_BAR_REF_WIDTH = 850;

    /** Reference height of the experience bar in pixels */
    private static final int EXP_BAR_REF_HEIGHT = 25;

    /** Reference max width for the experience bar progress */
    private static final int EXP_BAR_REF_MAX_WIDTH = 850;

    /** Reference image dimensions */
    private static final int LEVEL_BAR_IMAGE_WIDTH = 1018;
    private static final int LEVEL_BAR_IMAGE_HEIGHT = 31;
    private static final int LEVEL_PART_IMAGE_WIDTH = 122;

    /** Color for the level text (white) */
    private static final int PRIMARY_TEXT_COLOR = 0xFFFFFFFF;

    // ==================== Fields ====================

    /** Text renderer for drawing level text */
    private final TextRenderer textRenderer;

    /** X-coordinate where the widget is rendered */
    private int x;

    /** Y-coordinate where the widget is rendered */
    private int y;

    /** Current scale factor */
    private float currentScale;

    /** Scaled width of the level bar background */
    private int levelBarWidth;

    /** Scaled height of the level bar background */
    private int levelBarHeight;

    /** Scaled width of the level text section */
    private int levelPartWidth;

    /** Scaled experience bar width */
    private int expBarWidth;

    /** Scaled experience bar height */
    private int expBarHeight;

    /** Scaled experience bar max width */
    private int expBarMaxWidth;

    /** Category data containing level and experience information */
    private ClientCategoryData categoryData;

    /** Consumer for displaying tooltips */
    private Consumer<List<OrderedText>> tooltipSetter;

    // ==================== Constructor ====================

    /**
     * Creates a new experience bar widget.
     *
     * @param textRenderer the text renderer for drawing level text
     * @param initialScale the initial scaling factor for the widget dimensions
     */
    public ExperienceBarWidget(TextRenderer textRenderer, float initialScale) {
        this.textRenderer = textRenderer;
        this.currentScale = initialScale;
        calculateDimensions();
    }

    /**
     * Calculates scaled dimensions for the widget components.
     * <p>
     * Applies the current scale factor to the base image dimensions to determine
     * the actual render size of the level bar and its components.
     */
    private void calculateDimensions() {
        this.levelBarWidth = (int) (LEVEL_BAR_IMAGE_WIDTH * currentScale);
        this.levelBarHeight = (int) (LEVEL_BAR_IMAGE_HEIGHT * currentScale);
        this.levelPartWidth = (int) (LEVEL_PART_IMAGE_WIDTH * currentScale);
        this.expBarWidth = (int) (EXP_BAR_REF_WIDTH * currentScale);
        this.expBarHeight = (int) (EXP_BAR_REF_HEIGHT * currentScale);
        this.expBarMaxWidth = (int) (EXP_BAR_REF_MAX_WIDTH * currentScale);
    }

    // ==================== Configuration ====================

    /**
     * Updates the scale factor and recalculates dimensions.
     * <p>
     * Call this when the screen is resized to ensure the widget
     * scales properly with the rest of the UI.
     *
     * @param scale the new scale factor
     */
    public void updateScale(float scale) {
        this.currentScale = scale;
        calculateDimensions();
    }

    /**
     * Sets the position where the widget should be rendered.
     * <p>
     * The position represents the top-left corner of the widget.
     *
     * @param x the x-coordinate in screen space
     * @param y the y-coordinate in screen space
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Updates the category data to display.
     * <p>
     * The category data contains the player's current level, experience,
     * and experience requirements for the next level.
     *
     * @param categoryData the category data, or {@code null} to stop rendering
     */
    public void setCategoryData(ClientCategoryData categoryData) {
        this.categoryData = categoryData;
    }

    /**
     * Sets the consumer for displaying tooltips.
     * <p>
     * The consumer is called when the mouse hovers over the widget,
     * allowing the parent screen to display the tooltip.
     *
     * @param tooltipSetter the tooltip consumer, or {@code null} to disable tooltips
     */
    public void setTooltipSetter(Consumer<List<OrderedText>> tooltipSetter) {
        this.tooltipSetter = tooltipSetter;
    }

    // ==================== Rendering ====================

    /**
     * Renders the experience bar widget.
     * <p>
     * If no category data is available or the category doesn't have experience,
     * nothing is rendered. Otherwise, displays:
     * <ol>
     *     <li>Level background texture</li>
     *     <li>Level text centered in the level section</li>
     *     <li>Experience bar with progress</li>
     *     <li>Tooltip on hover (if tooltip setter is configured)</li>
     * </ol>
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time (unused)
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (categoryData == null || !categoryData.hasExperience()) {
            return;
        }

        // Render level background
        int lvlX = x;
        int lvlY = y;
        context.drawTexture(LVL_BACKGROUND, lvlX, lvlY, 0, 0,
                levelBarWidth, levelBarHeight, levelBarWidth, levelBarHeight);

        // Render level text
        renderLevelText(context, lvlX);

        // Render experience bar (scaled offset)
        int barX = x + (int)(152 * currentScale);
        int barY = y + levelBarHeight / 2 - expBarHeight / 2;
        renderExperienceBar(context, barX, barY);

        // Handle hover tooltip
        if (isMouseOver(mouseX, mouseY) && tooltipSetter != null) {
            showTooltip();
        }
    }

    /**
     * Renders the level text centered in the level section.
     * <p>
     * Uses the Press Start pixel font for a retro game aesthetic.
     * Text format: "Lvl X" where X is the current level.
     *
     * @param context the drawing context
     * @param lvlX the x-coordinate of the level background
     */
    private void renderLevelText(DrawContext context, int lvlX) {
        Text levelText = Text.literal("Lvl " + categoryData.getCurrentLevel())
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));

        int textWidth = textRenderer.getWidth(levelText);
        int textHeight = textRenderer.fontHeight;

        int textX = lvlX + (levelPartWidth - textWidth) / 2;
        int textY = y + (levelBarHeight - textHeight) / 2;

        context.drawText(textRenderer, levelText, textX, textY, PRIMARY_TEXT_COLOR, false);
    }

    /**
     * Renders the experience bar with background and progress overlay.
     * <p>
     * The progress width is calculated as a percentage of the maximum width
     * based on the experience progress value (0.0 to 1.0).
     *
     * @param context the drawing context
     * @param barX the x-coordinate of the experience bar
     * @param barY the y-coordinate of the experience bar
     */
    private void renderExperienceBar(DrawContext context, int barX, int barY) {
        // Background
        context.drawGuiTexture(EXP_BAR_BACKGROUND, barX, barY, expBarWidth, expBarHeight);

        // Progress
        int progressWidth = Math.min(expBarWidth,
                (int) (categoryData.getExperienceProgress() * expBarMaxWidth));

        if (progressWidth > 0) {
            context.drawGuiTexture(
                    EXP_BAR_PROGRESS,
                    expBarWidth, expBarHeight,
                    0, 0,
                    barX, barY,
                    progressWidth, expBarHeight
            );
        }
    }

    /**
     * Shows the experience tooltip via the configured tooltip setter.
     * <p>
     * Displays the current XP and required XP in the format "X/Y"
     * where X is current experience and Y is required experience.
     */
    private void showTooltip() {
        var currentXP = categoryData.getCurrentExperience();
        var requiredXP = categoryData.getRequiredExperience();
        var lines = new ArrayList<OrderedText>();
        lines.add(Text.literal(currentXP + "/" + requiredXP).asOrderedText());
        tooltipSetter.accept(lines);
    }

    // ==================== Input Handling ====================

    /**
     * Checks if the mouse is over the widget.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if mouse is within widget bounds, {@code false} otherwise
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + levelBarWidth &&
                mouseY >= y && mouseY < y + levelBarHeight;
    }

    /**
     * Handles mouse click events.
     * <p>
     * This widget is non-interactive and does not respond to clicks.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button
     * @return {@code false} always (widget is non-interactive)
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false; // Non-interactive
    }

    // ==================== Element Interface ====================

    /**
     * Sets focus state for this widget.
     * <p>
     * This widget does not support focus as it is non-interactive.
     *
     * @param focused the focus state (ignored)
     */
    @Override
    public void setFocused(boolean focused) {}

    /**
     * Checks if this widget has focus.
     * <p>
     * This widget never has focus as it is non-interactive.
     *
     * @return {@code false} always
     */
    @Override
    public boolean isFocused() {
        return false;
    }
}