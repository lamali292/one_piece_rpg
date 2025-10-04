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
 * Widget for displaying skill descriptions and information.
 * Shows title, description, extra info, and optionally the skill ID.
 */
public class DescriptionPanelWidget implements Drawable {
    private static final int LINE_HEIGHT = 5;
    private static final int TITLE_SPACING = 2;
    private static final int SECTION_SPACING = 4;
    private static final int PADDING = 6;

    private final int x;
    private final int y;
    private final int width;
    private final TextRenderer textRenderer;

    private Text title;
    private Text description;
    private Text extraDescription;
    private Text advanced;
    private boolean showAdvanced;

    public DescriptionPanelWidget(int x, int y, int width, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.textRenderer = textRenderer;
    }

    public void setContent(Text title, Text description, Text extraDescription, Text advanced) {
        this.title = title;
        this.description = description;
        this.extraDescription = extraDescription;
        this.advanced = advanced;
    }

    public void clearContent() {
        this.title = null;
        this.description = null;
        this.extraDescription = null;
        this.advanced = null;
    }

    public void setShowAdvanced(boolean showAdvanced) {
        this.showAdvanced = showAdvanced;
    }

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

    private void renderAdvanced(DrawContext context, int startY, int maxWidth) {
        Text advancedText = advanced.copy()
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START).withFormatting(Formatting.DARK_GRAY));

        context.drawText(textRenderer, advancedText, x + PADDING, startY, 0xFF666666, false);
    }

    public boolean hasContent() {
        return title != null;
    }
}