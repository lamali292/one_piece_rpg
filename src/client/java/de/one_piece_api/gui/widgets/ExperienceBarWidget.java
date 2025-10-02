package de.one_piece_api.gui.widgets;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.registries.MyFonts;
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
 * Shows level text and an experience progress bar.
 */
public class ExperienceBarWidget implements Drawable, Element {
    private static final Identifier LVL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/lvl_bar.png");
    private static final Identifier EXP_BAR_BACKGROUND = Identifier.of("hud/experience_bar_background");
    private static final Identifier EXP_BAR_PROGRESS = Identifier.of("hud/experience_bar_progress");

    private static final int EXP_BAR_WIDTH = 182;
    private static final int EXP_BAR_HEIGHT = 5;
    private static final int EXP_BAR_MAX_WIDTH = 183;
    private static final int PRIMARY_TEXT_COLOR = 0xFFFFFFFF;

    private final TextRenderer textRenderer;
    private final float scale;

    private int x;
    private int y;
    private int levelBarWidth;
    private int levelBarHeight;
    private int levelPartWidth;
    private ClientCategoryData categoryData;
    private Consumer<List<OrderedText>> tooltipSetter;

    public ExperienceBarWidget(TextRenderer textRenderer, float scale) {
        this.textRenderer = textRenderer;
        this.scale = scale;
        calculateDimensions();
    }

    private void calculateDimensions() {
        final int LEVEL_BAR_IMAGE_WIDTH = 1018;
        final int LEVEL_BAR_IMAGE_HEIGHT = 31;
        final int LEVEL_PART_IMAGE_WIDTH = 122;

        this.levelBarWidth = (int) (LEVEL_BAR_IMAGE_WIDTH * scale);
        this.levelBarHeight = (int) (LEVEL_BAR_IMAGE_HEIGHT * scale);
        this.levelPartWidth = (int) (LEVEL_PART_IMAGE_WIDTH * scale);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setCategoryData(ClientCategoryData categoryData) {
        this.categoryData = categoryData;
    }

    public void setTooltipSetter(Consumer<List<OrderedText>> tooltipSetter) {
        this.tooltipSetter = tooltipSetter;
    }

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

        // Render experience bar
        int barX = x + 42;
        int barY = y + levelBarHeight / 2 - EXP_BAR_HEIGHT / 2;
        renderExperienceBar(context, barX, barY);

        // Handle hover tooltip
        if (isMouseOver(mouseX, mouseY) && tooltipSetter != null) {
            showTooltip();
        }
    }

    private void renderLevelText(DrawContext context, int lvlX) {
        Text levelText = Text.literal("Lvl " + categoryData.getCurrentLevel())
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));

        int textWidth = textRenderer.getWidth(levelText);
        int textHeight = textRenderer.fontHeight;

        int textX = lvlX + (levelPartWidth - textWidth) / 2;
        int textY = y + (levelBarHeight - textHeight) / 2;

        context.drawText(textRenderer, levelText, textX, textY, PRIMARY_TEXT_COLOR, false);
    }

    private void renderExperienceBar(DrawContext context, int barX, int barY) {
        // Background
        context.drawGuiTexture(EXP_BAR_BACKGROUND, barX, barY, EXP_BAR_WIDTH, EXP_BAR_HEIGHT);

        // Progress
        int progressWidth = Math.min(EXP_BAR_WIDTH,
                (int) (categoryData.getExperienceProgress() * EXP_BAR_MAX_WIDTH));

        if (progressWidth > 0) {
            context.drawGuiTexture(
                    EXP_BAR_PROGRESS,
                    EXP_BAR_WIDTH, EXP_BAR_HEIGHT,
                    0, 0,
                    barX, barY,
                    progressWidth, EXP_BAR_HEIGHT
            );
        }
    }

    private void showTooltip() {
        var currentXP = categoryData.getCurrentExperience();
        var requiredXP = categoryData.getRequiredExperience();
        var lines = new ArrayList<OrderedText>();
        lines.add(Text.literal(currentXP + "/" + requiredXP).asOrderedText());
        tooltipSetter.accept(lines);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + levelBarWidth &&
                mouseY >= y && mouseY < y + levelBarHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false; // Non-interactive
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }
}