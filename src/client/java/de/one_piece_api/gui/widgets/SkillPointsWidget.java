package de.one_piece_api.gui.widgets;

import de.one_piece_api.registries.MyFonts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.colors.ClientFillStrokeColorsConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;

import java.util.Objects;

/**
 * Widget for displaying available skill points with custom styling.
 *
 * Features:
 * - Displays current skill points from category data
 * - Uses custom font and formatting
 * - Configurable position
 * - Handles missing or null category data gracefully
 *
 * Thread Safety: Not thread-safe. Should only be accessed from the render thread.
 */
public class SkillPointsWidget implements Drawable {

    // ==================== Constants ====================

    private static final String SKILL_POINTS_TEXT = "Skill points: ";
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    // ==================== Fields ====================

    private final TextRenderer textRenderer;

    private int x;
    private int y;
    private ClientCategoryData categoryData;

    // ==================== Constructor ====================

    /**
     * Creates a new skill points widget.
     *
     * @param textRenderer The text renderer to use for drawing
     * @throws NullPointerException if textRenderer is null
     */
    public SkillPointsWidget(TextRenderer textRenderer) {
        this.textRenderer = Objects.requireNonNull(textRenderer, "TextRenderer cannot be null");
        this.x = 0;
        this.y = 0;
    }

    // ==================== Configuration ====================

    /**
     * Sets the position where the widget should be rendered.
     *
     * @param x X coordinate in screen space
     * @param y Y coordinate in screen space
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Updates the category data to display.
     *
     * @param categoryData The category data containing skill point information, or null to clear
     */
    public void setCategoryData(ClientCategoryData categoryData) {
        this.categoryData = categoryData;
    }

    /**
     * Checks if the widget has valid data to display.
     *
     * @return true if category data is available
     */
    public boolean hasData() {
        return categoryData != null;
    }

    // ==================== Rendering ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Objects.requireNonNull(context, "DrawContext cannot be null");

        if (!hasData()) {
            return; // Nothing to render
        }

        Text displayText = createDisplayText();
        int textColor = getPointsColor();

        renderText(context, displayText, textColor);
    }

    /**
     * Creates the formatted text to display.
     *
     * @return Styled text showing skill points
     */
    private Text createDisplayText() {
        int pointsLeft = categoryData.getPointsLeft();
        String fullText = SKILL_POINTS_TEXT + pointsLeft;

        return Text.literal(fullText)
                .setStyle(Style.EMPTY
                        .withFont(MyFonts.MONTSERRAT)
                        .withFormatting(Formatting.UNDERLINE));
    }

    /**
     * Gets the color to use for the skill points text from category configuration.
     *
     * @return ARGB color value, or default white if not configured
     */
    private int getPointsColor() {
        try {
            ClientCategoryConfig config = categoryData.getConfig();
            if (config != null && config.colors() != null && config.colors().points() != null) {
                ClientFillStrokeColorsConfig pointsColor = config.colors().points();
                if (pointsColor.fill() != null) {
                    return pointsColor.fill().argb();
                }
            }
        } catch (Exception e) {
            // Fall back to default color if any error occurs
        }

        return DEFAULT_COLOR;
    }

    /**
     * Renders the text at the configured position.
     *
     * @param context The draw context
     * @param text The text to render
     * @param color The text color
     */
    private void renderText(DrawContext context, Text text, int color) {
        context.drawText(textRenderer, text, x, y, color, false);
    }

    // ==================== Getters ====================

    /**
     * @return The current X position
     */
    public int getX() {
        return x;
    }

    /**
     * @return The current Y position
     */
    public int getY() {
        return y;
    }

    /**
     * @return The current skill points, or 0 if no data
     */
    public int getPointsLeft() {
        return hasData() ? categoryData.getPointsLeft() : 0;
    }
}