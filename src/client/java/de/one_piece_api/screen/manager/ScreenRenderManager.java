package de.one_piece_api.screen.manager;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.screen.OnePieceScreen;
import de.one_piece_api.screen.widget.main.DescriptionPanelWidget;
import de.one_piece_api.screen.widget.main.ExperienceBarWidget;
import de.one_piece_api.screen.widget.main.SkillPointsWidget;
import de.one_piece_api.screen.widget.main.SpellSlotWidget;
import de.one_piece_api.init.MyFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.data.ClientCategoryData;

import java.util.List;

/**
 * Manages all rendering operations for the OnePiece screen.
 * Separates rendering logic from data management.
 */
public class ScreenRenderManager {

    private static final class Textures {
        static final Identifier SKILL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/background.png");
        static final Identifier SKILL_WIDE_RECT = OnePieceRPG.id("textures/gui/skill/boutique.png");
        static final Identifier SKILLTREE_FRAME = OnePieceRPG.id("textures/gui/skill/skilltree.png");
        static final Identifier DESCRIPTION_FRAME = OnePieceRPG.id("textures/gui/skill/description.png");
    }

    private static final class Colors {
        static final int PRIMARY_TEXT = 0xFFFFFFFF;
        static final int DIVIDER = 0xFFE2E0B4;
    }

    private final MinecraftClient client;
    private final TextRenderer textRenderer;
    private final DescriptionPanelWidget descriptionPanel;
    private final ExperienceBarWidget experienceBar;
    private final SkillPointsWidget skillPointsWidget;

    // Cached position
    private ScreenPosition cachedScreenPosition;

    public ScreenRenderManager(MinecraftClient client, TextRenderer textRenderer,
                               DescriptionPanelWidget descriptionPanel,
                               ExperienceBarWidget experienceBar,
                               SkillPointsWidget skillPointsWidget) {
        this.client = client;
        this.textRenderer = textRenderer;
        this.descriptionPanel = descriptionPanel;
        this.experienceBar = experienceBar;
        this.skillPointsWidget = skillPointsWidget;
    }

    /**
     * Renders the main background texture
     */
    public void renderBackground(DrawContext context, int screenWidth, int screenHeight) {
        ScreenPosition pos = getScreenPosition(screenWidth, screenHeight);

        RenderSystem.enableBlend();
        try {
            // Main background
            context.drawTexture(
                    Textures.SKILL_BACKGROUND,
                    pos.x(), pos.y(),
                    0, 0,
                    OnePieceScreen.Layout.BACKGROUND_WIDTH,
                    OnePieceScreen.Layout.BACKGROUND_HEIGHT,
                    OnePieceScreen.Layout.BACKGROUND_WIDTH,
                    OnePieceScreen.Layout.BACKGROUND_HEIGHT
            );

            renderFrames(context, pos);
            renderDividerLine(context, pos);

        } finally {
            RenderSystem.disableBlend();
        }
    }

    private void renderFrames(DrawContext context, ScreenPosition screenPos) {
        // Skill tree frame
        context.drawTexture(
                Textures.SKILLTREE_FRAME,
                screenPos.x() + OnePieceScreen.Layout.SKILLTREE_OFFSET_X,
                screenPos.y() + OnePieceScreen.Layout.CONTENT_OFFSET_Y,
                0, 0,
                OnePieceScreen.Layout.SKILLTREE_WIDTH,
                OnePieceScreen.Layout.CONTENT_HEIGHT,
                OnePieceScreen.Layout.SKILLTREE_WIDTH,
                OnePieceScreen.Layout.CONTENT_HEIGHT
        );

        // Description frame
        context.drawTexture(
                Textures.DESCRIPTION_FRAME,
                screenPos.x() + OnePieceScreen.Layout.DESCRIPTION_OFFSET_X,
                screenPos.y() + OnePieceScreen.Layout.CONTENT_OFFSET_Y,
                0, 0,
                OnePieceScreen.Layout.DESCRIPTION_WIDTH,
                OnePieceScreen.Layout.CONTENT_HEIGHT,
                OnePieceScreen.Layout.DESCRIPTION_WIDTH,
                OnePieceScreen.Layout.CONTENT_HEIGHT
        );

        // Boutique frame
        int boutiqueX = screenPos.x() + OnePieceScreen.Layout.DESCRIPTION_OFFSET_X +
                (OnePieceScreen.Layout.DESCRIPTION_WIDTH - OnePieceScreen.Layout.BOUTIQUE_WIDTH) / 2;
        int boutiqueY = screenPos.y() + OnePieceScreen.Layout.CONTENT_OFFSET_Y2 +
                (OnePieceScreen.Layout.RECT_DIM - OnePieceScreen.Layout.BOUTIQUE_HEIGHT) / 2;

        context.drawTexture(
                Textures.SKILL_WIDE_RECT,
                boutiqueX, boutiqueY,
                0, 0,
                OnePieceScreen.Layout.BOUTIQUE_WIDTH,
                OnePieceScreen.Layout.BOUTIQUE_HEIGHT,
                OnePieceScreen.Layout.BOUTIQUE_WIDTH,
                OnePieceScreen.Layout.BOUTIQUE_HEIGHT
        );
    }

    private void renderDividerLine(DrawContext context, ScreenPosition screenPos) {
        int lineY = screenPos.y() + OnePieceScreen.Layout.CONTENT_OFFSET_Y2 +
                OnePieceScreen.Layout.RECT_DIM / 2;
        int lineStartX = screenPos.x() + OnePieceScreen.Layout.SKILLTREE_OFFSET_X +
                OnePieceScreen.Layout.RECT_DIM / 2;
        int lineEndX = screenPos.x() + OnePieceScreen.Layout.SKILLTREE_OFFSET_X +
                OnePieceScreen.Layout.SKILLTREE_WIDTH - OnePieceScreen.Layout.RECT_DIM / 2;

        context.drawHorizontalLine(lineStartX, lineEndX, lineY, Colors.DIVIDER);
    }

    /**
     * Renders spell slots
     */
    public void renderSpellSlots(DrawContext context, List<SpellSlotWidget> slots,
                                 int mouseX, int mouseY, int screenWidth, int screenHeight) {
        ScreenPosition screenPos = getScreenPosition(screenWidth, screenHeight);

        for (SpellSlotWidget slot : slots) {
            slot.render(context, mouseX, mouseY, 0);
        }
    }

    /**
     * Renders the description section
     */
    public void renderDescriptionSection(DrawContext context, int mouseX, int mouseY,
                                         int screenWidth, int screenHeight) {
        ScreenPosition screenPos = getScreenPosition(screenWidth, screenHeight);

        // Title
        Text title = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".skill.description")
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT));

        context.drawText(
                textRenderer,
                title,
                screenPos.x() + OnePieceScreen.Layout.DESCRIPTION_OFFSET_X + 4,
                screenPos.y() + OnePieceScreen.Layout.CONTENT_OFFSET_Y + 4,
                Colors.PRIMARY_TEXT,
                false
        );

        // Description panel
        if (client != null) {
            descriptionPanel.setShowAdvanced(client.options.advancedItemTooltips);
        }

        context.getMatrices().push();
        context.getMatrices().translate(screenPos.x(), screenPos.y(), 0);
        try {
            descriptionPanel.render(context, mouseX - screenPos.x(), mouseY - screenPos.y(), 0);
        } finally {
            context.getMatrices().pop();
        }
    }

    /**
     * Renders the experience section
     */
    public void renderExperienceSection(DrawContext context, ClientCategoryData categoryData,
                                        int mouseX, int mouseY, int screenWidth, int screenHeight) {
        ScreenPosition screenPos = getScreenPosition(screenWidth, screenHeight);

        // Update widget data
        experienceBar.setCategoryData(categoryData);
        skillPointsWidget.setCategoryData(categoryData);

        // Update positions
        int barX = screenPos.x() + OnePieceScreen.Layout.SKILLTREE_OFFSET_X + 10;
        int barY = screenPos.y() + OnePieceScreen.Layout.CONTENT_OFFSET_Y + OnePieceScreen.Layout.CONTENT_HEIGHT - 6;
        experienceBar.setPosition(barX, barY);

        int pointsX = screenPos.x() + OnePieceScreen.Layout.SKILLTREE_OFFSET_X + 5;
        int pointsY = screenPos.y() + OnePieceScreen.Layout.CONTENT_OFFSET_Y + 4;
        skillPointsWidget.setPosition(pointsX, pointsY);

        // Render
        experienceBar.render(context, mouseX, mouseY, 0);
        skillPointsWidget.render(context, mouseX, mouseY, 0);
    }

    /**
     * Gets screen position and caches it
     */
    public ScreenPosition getScreenPosition(int screenWidth, int screenHeight) {
        int x = (screenWidth - OnePieceScreen.Layout.BACKGROUND_WIDTH) / 2;
        int y = (screenHeight - OnePieceScreen.Layout.BACKGROUND_HEIGHT) / 2;
        ScreenPosition pos = new ScreenPosition(x, y);
        cachedScreenPosition = pos;
        return pos;
    }

    /**
     * Gets cached screen position or calculates new one
     */
    public ScreenPosition getCachedScreenPosition(int screenWidth, int screenHeight) {
        if (cachedScreenPosition == null) {
            return getScreenPosition(screenWidth, screenHeight);
        }
        return cachedScreenPosition;
    }

    /**
     * Invalidates position cache
     */
    public void invalidateCache() {
        cachedScreenPosition = null;
    }

    /**
     * Screen position record
     */
    public record ScreenPosition(int x, int y) {}
}