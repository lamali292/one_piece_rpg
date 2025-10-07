package de.one_piece_api.gui.widgets;

import de.one_piece_api.registries.MyFonts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Widget for displaying skill descriptions and detailed information.
 * <p>
 * This widget renders multi-line skill information in a formatted panel with
 * automatic text wrapping. It supports hierarchical information display with
 * different colors and styles for each section.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Custom pixel font (Press Start) for retro aesthetic</li>
 *     <li>Automatic text wrapping to fit panel width</li>
 *     <li>Color-coded information hierarchy</li>
 *     <li>Optional advanced tooltip (F3+H)</li>
 *     <li>Graceful handling of missing content</li>
 * </ul>
 *
 * <h2>Content Sections:</h2>
 * <ol>
 *     <li>Title - White, bold prominence</li>
 *     <li>Description - Light gray, primary information</li>
 *     <li>Extra Description - Medium gray, supplementary details</li>
 *     <li>Advanced Info - Dark gray, technical details (F3+H only)</li>
 * </ol>
 *
 * @see Drawable
 * @see TextRenderer
 */
public class DescriptionPanelWidget implements Drawable {

    // ==================== Constants ====================

    /** Vertical spacing between lines of text in pixels */
    private static final int LINE_HEIGHT = 5;

    /** Spacing after the title section in pixels */
    private static final int TITLE_SPACING = 2;

    /** Spacing between content sections in pixels */
    private static final int SECTION_SPACING = 4;

    /** Padding around the panel content in pixels */
    private static final int PADDING = 6;

    // ==================== Fields ====================

    /** X-coordinate of the panel's left edge */
    private final int x;

    /** Y-coordinate of the panel's top edge */
    private final int y;

    /** Total width of the panel in pixels */
    private final int width;

    /** Text renderer for drawing all text content */
    private final TextRenderer textRenderer;

    /** The skill title text */
    private Text title;

    /** The skill description text */
    private Text description;

    /** Additional skill information text */
    private Text extraDescription;

    /** Advanced tooltip information (skill ID, etc.) */
    private Text advanced;

    /** Whether to show advanced information (F3+H state) */
    private boolean showAdvanced;

    // ==================== Constructor ====================

    /**
     * Creates a new description panel widget.
     * <p>
     * The position and width are immutable after construction to ensure
     * consistent layout.
     *
     * @param x the x-coordinate of the panel's left edge
     * @param y the y-coordinate of the panel's top edge
     * @param width the total width of the panel in pixels
     * @param textRenderer the text renderer for drawing content
     */
    public DescriptionPanelWidget(int x, int y, int width, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.textRenderer = textRenderer;
    }

    // ==================== Configuration ====================

    /**
     * Sets the content to display in the panel.
     * <p>
     * All parameters can be {@code null} or empty. Empty sections are
     * automatically skipped during rendering.
     *
     * @param title the skill title
     * @param description the skill description
     * @param extraDescription additional information about the skill
     * @param advanced advanced information (e.g., skill ID)
     */
    public void setContent(Text title, Text description, Text extraDescription, Text advanced) {
        this.title = title;
        this.description = description;
        this.extraDescription = extraDescription;
        this.advanced = advanced;
    }

    /**
     * Clears all content from the panel.
     * <p>
     * After calling this method, the panel will not render anything
     * until new content is set via {@link #setContent}.
     */
    public void clearContent() {
        this.title = null;
        this.description = null;
        this.extraDescription = null;
        this.advanced = null;
    }

    /**
     * Sets whether advanced information should be shown.
     * <p>
     * This is typically controlled by the F3+H advanced tooltips setting.
     * When {@code false}, the advanced section is hidden even if content exists.
     *
     * @param showAdvanced {@code true} to show advanced info, {@code false} to hide it
     */
    public void setShowAdvanced(boolean showAdvanced) {
        this.showAdvanced = showAdvanced;
    }

    // ==================== Rendering ====================

    /**
     * Renders the description panel with all content sections.
     * <p>
     * If no title is set, nothing is rendered. Otherwise, renders all
     * non-empty sections in order:
     * <ol>
     *     <li>Title (white, custom font)</li>
     *     <li>Description (light gray)</li>
     *     <li>Extra description (medium gray)</li>
     *     <li>Advanced info (dark gray, if enabled)</li>
     * </ol>
     * All text is automatically wrapped to fit the panel width minus padding.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate (unused)
     * @param mouseY the mouse y-coordinate (unused)
     * @param delta the frame delta time (unused)
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (title == null) {
            return;
        }

        int currentY = y + PADDING;
        int maxWidth = width - PADDING * 2;

        // Render title
        currentY = renderTitle(context, currentY, maxWidth);

        // Render description
        if (description != null && !description.getString().isEmpty()) {
            currentY = renderDescription(context, currentY, maxWidth);
        }

        // Render extra description
        if (extraDescription != null && !extraDescription.getString().isEmpty()) {
            currentY = renderExtraDescription(context, currentY, maxWidth);
        }

        // Render advanced info
        if (showAdvanced && advanced != null && !advanced.getString().isEmpty()) {
            renderAdvanced(context, currentY, maxWidth);
        }
    }

    /**
     * Renders the title section with word wrapping.
     * <p>
     * Uses white color and Press Start pixel font for prominence.
     *
     * @param context the drawing context
     * @param startY the y-coordinate to start rendering
     * @param maxWidth the maximum width for text wrapping
     * @return the y-coordinate after rendering this section
     */
    private int renderTitle(DrawContext context, int startY, int maxWidth) {
        MutableText titleText = MutableText.of(title.getContent())
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));

        List<OrderedText> lines = textRenderer.wrapLines(titleText, maxWidth);
        int currentY = startY;

        for (OrderedText line : lines) {
            context.drawText(textRenderer, line, x + PADDING, currentY, 0xFFFFFFFF, false);
            currentY += LINE_HEIGHT;
        }

        return currentY + TITLE_SPACING;
    }

    /**
     * Renders the description section with word wrapping.
     * <p>
     * Uses light gray color to differentiate from title while maintaining readability.
     *
     * @param context the drawing context
     * @param startY the y-coordinate to start rendering
     * @param maxWidth the maximum width for text wrapping
     * @return the y-coordinate after rendering this section
     */
    private int renderDescription(DrawContext context, int startY, int maxWidth) {
        MutableText descText = MutableText.of(description.getContent())
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));

        List<OrderedText> lines = textRenderer.wrapLines(descText, maxWidth);
        int currentY = startY;

        for (OrderedText line : lines) {
            context.drawText(textRenderer, line, x + PADDING, currentY, 0xFFCCCCCC, false);
            currentY += LINE_HEIGHT;
        }

        return currentY + SECTION_SPACING;
    }

    /**
     * Renders the extra description section with word wrapping.
     * <p>
     * Uses medium gray color to indicate supplementary information with
     * lower priority than the main description.
     *
     * @param context the drawing context
     * @param startY the y-coordinate to start rendering
     * @param maxWidth the maximum width for text wrapping
     * @return the y-coordinate after rendering this section
     */
    private int renderExtraDescription(DrawContext context, int startY, int maxWidth) {
        MutableText extraText = MutableText.of(extraDescription.getContent())
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));

        List<OrderedText> lines = textRenderer.wrapLines(extraText, maxWidth);
        int currentY = startY;

        for (OrderedText line : lines) {
            context.drawText(textRenderer, line, x + PADDING, currentY, 0xFF888888, false);
            currentY += LINE_HEIGHT;
        }

        return currentY + SECTION_SPACING;
    }

    /**
     * Renders the advanced information section.
     * <p>
     * Uses dark gray color to indicate technical/debug information.
     * Only shown when advanced tooltips are enabled (F3+H).
     *
     * @param context the drawing context
     * @param startY the y-coordinate to start rendering
     * @param maxWidth the maximum width for text wrapping (unused, single line)
     */
    private void renderAdvanced(DrawContext context, int startY, int maxWidth) {
        Text advancedText = advanced.copy()
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START).withFormatting(Formatting.DARK_GRAY));

        context.drawText(textRenderer, advancedText, x + PADDING, startY, 0xFF666666, false);
    }

    // ==================== State Queries ====================

    /**
     * Checks if the panel has content to display.
     * <p>
     * Content is considered present if a title has been set, regardless
     * of whether other sections are populated.
     *
     * @return {@code true} if content exists, {@code false} otherwise
     */
    public boolean hasContent() {
        return title != null;
    }
}