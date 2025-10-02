package de.one_piece_api.gui.widgets;

import de.one_piece_api.registries.MyFonts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.puffish.skillsmod.client.data.ClientCategoryData;

/**
 * Widget for displaying available skill points.
 */
public class SkillPointsWidget implements Drawable {
    private final TextRenderer textRenderer;
    private int x;
    private int y;
    private ClientCategoryData categoryData;

    public SkillPointsWidget(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setCategoryData(ClientCategoryData categoryData) {
        this.categoryData = categoryData;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (categoryData == null) {
            return;
        }

        Text pointsText = Text.literal("Skill points: " + categoryData.getPointsLeft())
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withFormatting(Formatting.UNDERLINE));

        int fillColor = categoryData.getConfig().colors().points().fill().argb();

        context.drawText(textRenderer, pointsText, x, y, fillColor, false);
    }
}