package de.one_piece_api.screen.widget.main;

import de.one_piece_api.init.MyFonts;
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
 * <p>
 * This widget renders the player's current skill points with configurable
 * colors from the category configuration. The text uses a custom font and
 * is underlined for emphasis.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Displays current skill points from category data</li>
 *     <li>Uses custom font (Montserrat) and formatting</li>
 *     <li>Configurable position for flexible layout</li>
 *     <li>Handles missing or null category data gracefully</li>
 *     <li>Retrieves colors from category configuration</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * Not thread-safe. Should only be accessed from the render thread.
 *
 * @see Drawable
 * @see ClientCategoryData
 */
public class SkillPointsWidget implements Drawable {

    // ==================== Constants ====================

    /** Label text displayed before the skill points number */
    private static final String SKILL_POINTS_TEXT = "Skill points: ";

    /** Default text color (white) used when configuration is unavailable */
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    // ==================== Fields ====================

    /** Text renderer for drawing the skill points text */
    private final TextRenderer textRenderer;

    /** X-coordinate where the widget is rendered */
    private int x;

    /** Y-coordinate where the widget is rendered */
    private int y;

    /** Category data containing skill points information */
    private ClientCategoryData categoryData;

    // ==================== Constructor ====================

    /**
     * Creates a new skill points widget.
     *
     * @param textRenderer the text renderer to use for drawing
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
     * <p>
     * The position represents the top-left corner of the text.
     *
     * @param x x-coordinate in screen space
     * @param y y-coordinate in screen space
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Updates the category data to display.
     * <p>
     * When category data changes (e.g., after spending skill points),
     * call this method to update the display.
     *
     * @param categoryData the category data containing skill point information,
     *                     or {@code null} to clear and stop rendering
     */
    public void setCategoryData(ClientCategoryData categoryData) {
        this.categoryData = categoryData;
    }

    /**
     * Checks if the widget has valid data to display.
     *
     * @return {@code true} if category data is available, {@code false} otherwise
     */
    public boolean hasData() {
        return categoryData != null;
    }

    // ==================== Rendering ====================

    /**
     * Renders the skill points widget.
     * <p>
     * If no category data is available, nothing is rendered. Otherwise,
     * displays "Skill points: X" where X is the current available points,
     * using the configured color and custom font styling.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate (unused)
     * @param mouseY the mouse y-coordinate (unused)
     * @param delta the frame delta time (unused)
     * @throws NullPointerException if context is null
     */
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
     * <p>
     * Constructs a text component with the skill points label and current
     * count, styled with the Montserrat font and underline formatting.
     *
     * @return styled text showing skill points
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
     * <p>
     * Attempts to retrieve the fill color from the category's points color
     * configuration. Falls back to {@link #DEFAULT_COLOR} (white) if:
     * <ul>
     *     <li>Configuration is null or incomplete</li>
     *     <li>Any exception occurs during retrieval</li>
     * </ul>
     *
     * @return ARGB color value for the text
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
     * @param context the drawing context
     * @param text the text to render
     * @param color the ARGB text color
     */
    private void renderText(DrawContext context, Text text, int color) {
        context.drawText(textRenderer, text, x, y, color, false);
    }

    // ==================== Getters ====================

    /**
     * Gets the current x-coordinate where the widget is rendered.
     *
     * @return the x-coordinate in screen space
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the current y-coordinate where the widget is rendered.
     *
     * @return the y-coordinate in screen space
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the current number of available skill points.
     *
     * @return the skill points remaining, or 0 if no data is available
     */
    public int getPointsLeft() {
        return hasData() ? categoryData.getPointsLeft() : 0;
    }
}