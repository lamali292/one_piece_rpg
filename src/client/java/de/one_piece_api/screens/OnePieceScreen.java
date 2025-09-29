package de.one_piece_api.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.PostProcessingManager;
import de.one_piece_api.interfaces.IOnePiecePlayer;
import de.one_piece_api.items.DevilFruitItem;
import de.one_piece_api.network.SetSpellsPayload;
import de.one_piece_api.network.UiPayload;
import de.one_piece_api.registries.MyFonts;
import de.one_piece_api.registries.MyKeys;
import de.one_piece_api.registries.MyShaders;
import de.one_piece_api.screens.util.CustomButton;
import de.one_piece_api.screens.util.GenericTabbedScreen;
import de.one_piece_api.util.OnePieceCategory;
import de.one_piece_api.util.RenderUtil;
import de.one_piece_api.util.SpellUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.util.SpellRender;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OnePieceScreen extends GenericTabbedScreen {

    // Singleton instance
    public static OnePieceScreen INSTANCE = null;

    // Texture constants
    private static final Identifier SKILL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/background.png");
    private static final Identifier EXPERIENCE_BAR_BACKGROUND = Identifier.of("hud/experience_bar_background");
    private static final Identifier EXPERIENCE_BAR_PROGRESS = Identifier.of("hud/experience_bar_progress");
    private static final Identifier SKILL_SMALL_RECT = OnePieceRPG.id("textures/gui/skill/small_rect.png");
    private static final Identifier SKILL_WIDE_RECT = OnePieceRPG.id("textures/gui/skill/boutique.png");
    private static final Identifier SKILLTREE_FRAME = OnePieceRPG.id("textures/gui/skill/skilltree.png");
    private static final Identifier DESCRIPTION_FRAME = OnePieceRPG.id("textures/gui/skill/description.png");
    private static final Identifier RESET_BUTTON = OnePieceRPG.id("textures/gui/skill/reskill.png");
    private static final Identifier RESET_BUTTON_HOVERED = OnePieceRPG.id("textures/gui/skill/reskill_hovered.png");
    private static final Identifier LVL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/lvl_bar.png");

    // Layout constants
    private static final float SCALE = 0.23F;
    private static final int BUTTON_MARGIN = 3;
    private static final int RECT_COUNT = 6;

    // Original dimensions
    private static final int BACKGROUND_IMAGE_WIDTH = 1785;
    private static final int BACKGROUND_IMAGE_HEIGHT = 985;
    private static final int SKILLTREE_IMAGE_WIDTH = 1202;
    private static final int CONTENT_IMAGE_HEIGHT = 688;
    private static final int DESCRIPTION_IMAGE_WIDTH = 456;
    private static final int BOUTIQUE_IMAGE_HEIGHT = 83;
    private static final int BOUTIQUE_IMAGE_WIDTH = 363;
    private static final int RECT_IMAGE_DIM = 100;
    private static final int RESET_IMAGE_DIM = 68;

    // Calculated dimensions
    public static final int backgroundWidth = (int) (BACKGROUND_IMAGE_WIDTH * SCALE);
    public static final int backgroundHeight = (int) (BACKGROUND_IMAGE_HEIGHT * SCALE);
    public static final int skilltreeWidth = (int) (SKILLTREE_IMAGE_WIDTH * SCALE);
    public static final int contentHeight = (int) (CONTENT_IMAGE_HEIGHT * SCALE);
    public static final int descriptionWidth = (int) (DESCRIPTION_IMAGE_WIDTH * SCALE);
    public static final int boutiqueHeight = (int) (BOUTIQUE_IMAGE_HEIGHT * SCALE);
    public static final int boutiqueWidth = (int) (BOUTIQUE_IMAGE_WIDTH * SCALE);
    public static final int rectDim = (int) (RECT_IMAGE_DIM * SCALE);
    public static final int resetDim = (int) (RESET_IMAGE_DIM * SCALE);

    // Calculated offsets
    public static final int contentOffsetY = (int) ((BACKGROUND_IMAGE_HEIGHT - CONTENT_IMAGE_HEIGHT) / 2F * SCALE);
    public static final int skilltreeOffsetX = (backgroundWidth - skilltreeWidth - descriptionWidth) / 3;
    public static final int descriptionOffsetX = 2 * skilltreeOffsetX + skilltreeWidth;
    public static final int topMargin = (backgroundHeight - contentHeight - contentOffsetY - rectDim) / 2;
    public static final int contentOffsetY2 = contentOffsetY + contentHeight + topMargin;
    public static final int rectOffset = (skilltreeWidth - RECT_COUNT * rectDim) / (RECT_COUNT - 1);
    private static final int iconPadding = 3;
    private static final int iconSize = rectDim - iconPadding * 2;

    // UI colors
    private static final int PRIMARY_TEXT_COLOR = 0xFFFFFFFF;
    private static final int DIVIDER_COLOR = 0xFFE2E0B4;

    // Experience bar constants
    private static final int EXP_BAR_WIDTH = 182;
    private static final int EXP_BAR_HEIGHT = 5;
    private static final int EXP_BAR_MAX_WIDTH = 183;

    // State
    private List<RegistryEntry<Spell>> learned;
    ClientCategoryData categoryData = null;
    protected ClientSkillScreenData data;
    private CustomButton resetButton;

    record TextData(Text title, Text description, Text extraDescription, Text advanced) {
    }

    // Skill hover state for description box
    private TextData hoveredTextData = null;
    private final List<Vector2i> spellPositions = new ArrayList<>();

    record SpellViewModel(String spellId, Identifier iconId, boolean learned) {
    }

    private List<SpellViewModel> spellViewModels;

    public OnePieceScreen(ClientPlayerEntity player) {
        super(Text.literal("Skill Tree"), player);
        INSTANCE = this;
        updateLearned();
        initializeData();
        createResetButton();
        setupTabs(player);
    }

    private void initializeData() {
        this.data = loadSkillScreenData();
        updateCategory();
    }

    void updateLearned() {
        learned = SpellUtil.getLearnedSpells(player);
    }

    private void createResetButton() {

        int buttonX = skilltreeOffsetX + skilltreeWidth - resetDim - BUTTON_MARGIN;
        int buttonY = contentOffsetY + BUTTON_MARGIN;

        ButtonTextures buttonTextures = new ButtonTextures(RESET_BUTTON, RESET_BUTTON_HOVERED);

        this.resetButton = new CustomButton(
                buttonX, buttonY, resetDim, resetDim,
                Text.literal(""),
                buttonTextures,
                this::handleResetClick
        );
    }

    private void handleResetClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        float mouseX = (float) client.mouse.getX() / client.getWindow().getWidth();
        float mouseY = (float) client.mouse.getY() / client.getWindow().getHeight();

        PostProcessingManager.click(client, mouseX, mouseY);
        ClientPlayNetworking.send(new UiPayload("reset"));
    }

    private void setupTabs(ClientPlayerEntity player) {
        int tabWidth = rectDim * 4;

        // Add Devil Fruit tab if player has one
        if (player instanceof IOnePiecePlayer iOnePiecePlayer) {
            String fruit = iOnePiecePlayer.onepiece$getDevilFruit();
            Identifier fruitId = Identifier.of(fruit);

            if (!DevilFruitItem.DEFAULT_DEVIL_FRUIT.equals(fruitId)) {
                addTab("Devil Fruit", new DevilFruitScreen(this, player, fruitId), tabWidth);
                currentTab = 1;
            }
        }

        // Add standard tabs
        addTab("Class", new SkillsScreen(this, player), tabWidth);
        addTab("Haki", new HakiScreen(this, player), tabWidth);
    }

    public void reloadCategoryData() {
        if (categoryData == null) return;

        // Preserve current view state
        ViewState viewState = new ViewState(
                categoryData.getX(),
                categoryData.getY(),
                categoryData.getScale()
        );

        // Reload category data
        categoryData = data.getCategory(OnePieceCategory.ID).orElse(null);

        // Restore view state
        if (categoryData != null) {
            viewState.applyTo(categoryData);
        }
    }

    public void updateCategory() {
        if (categoryData == null) {
            categoryData = data.getCategory(OnePieceCategory.ID).orElse(null);
        }

        if (categoryData != null) {
            categoryData.updateLastOpen();
            categoryData.updateUnseenPoints();
        }
    }

    public void setHoveredSkillInfo(TextData textData) {
        this.hoveredTextData = textData;
    }

    public void clearHoveredSkillInfo() {
        this.hoveredTextData = null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (client != null && MyKeys.getKeyBinding(MyKeys.OPEN_SKILL_TREE_KEY).matchesKey(keyCode, scanCode)) {
            client.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        ScreenPosition screenPos = getScreenPosition();

        RenderSystem.enableBlend();

        // Main background elements
        renderMainBackground(context, screenPos);
        renderFrames(context, screenPos);
        renderDividerLine(context, screenPos);
        renderSkillRects(context, screenPos, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    private void renderMainBackground(DrawContext context, ScreenPosition screenPos) {
        context.drawTexture(
                SKILL_BACKGROUND,
                screenPos.x(), screenPos.y(),
                0, 0,
                backgroundWidth, backgroundHeight,
                backgroundWidth, backgroundHeight
        );
    }

    private void renderFrames(DrawContext context, ScreenPosition screenPos) {
        // Skill tree frame
        context.drawTexture(
                SKILLTREE_FRAME,
                screenPos.x() + skilltreeOffsetX,
                screenPos.y() + contentOffsetY,
                0, 0,
                skilltreeWidth, contentHeight,
                skilltreeWidth, contentHeight
        );

        // Description frame
        context.drawTexture(
                DESCRIPTION_FRAME,
                screenPos.x() + descriptionOffsetX,
                screenPos.y() + contentOffsetY,
                0, 0,
                descriptionWidth, contentHeight,
                descriptionWidth, contentHeight
        );

        // Wide rect for description area
        context.drawTexture(
                SKILL_WIDE_RECT,
                screenPos.x() + descriptionOffsetX + (descriptionWidth - boutiqueWidth) / 2,
                screenPos.y() + contentOffsetY2 + (rectDim - boutiqueHeight) / 2,
                0, 0,
                boutiqueWidth, boutiqueHeight,
                boutiqueWidth, boutiqueHeight
        );
    }

    private void updateLayout(ScreenPosition screenPos) {
        this.spellPositions.clear();
        List<Vector2i> pos = new ArrayList<>();
        for (int i = 0; i < RECT_COUNT - 1; i++) {
            int offsetX = skilltreeOffsetX + i * (rectDim + rectOffset);
            int posX = screenPos.x() + offsetX;
            int posY = screenPos.y() + contentOffsetY2;
            this.spellPositions.add(new Vector2i(posX, posY));
        }

        int posX = screenPos.x() + skilltreeOffsetX + skilltreeWidth - rectDim;
        int posY = screenPos.y() + contentOffsetY2;
        this.spellPositions.add(new Vector2i(posX, posY));
    }


    private void renderSkillRects(DrawContext context, ScreenPosition screenPos, int mouseX, int mouseY) {
        // Render small rectangles
        updateLayout(screenPos);
        updateSpellViewModels();
        int i = 0;
        int size = spellViewModels.size();
        for (Vector2i p : this.spellPositions) {
            int x = p.x;
            int y = p.y;
            context.drawTexture(
                    SKILL_SMALL_RECT, x, y, 0, 0, rectDim, rectDim, rectDim, rectDim
            );
            if (i < size) {
                var spell = spellViewModels.get(i);
                if (spell == null) continue;
                var iconId = spell.iconId();
                String spellId = spell.spellId();
                Text available = Text.empty();
                if (iconId != null) {
                    if (spell.learned()) {
                        context.drawTexture(iconId, x + iconPadding, y + iconPadding, 0, 0, iconSize, iconSize, iconSize, iconSize);
                    } else {
                        RenderUtil.drawTexture(context, MyShaders::getGrayscaleShaderProgram, spell.iconId(), x + iconPadding, y + iconPadding, 0, 0, iconSize, iconSize, iconSize, iconSize);
                        available = Text.literal("Not Learned!");
                    }
                }
                if (mouseX >= x && mouseX < x + rectDim && mouseY >= y && mouseY < y + rectDim) {
                    if (spellId == null) {
                        clearHoveredSkillInfo();
                    } else {
                        String nameKey = "spell." + spellId.replace(":", ".") + ".name";
                        Text title = Text.translatable(nameKey).formatted(Formatting.WHITE);
                        String descKey = "spell." + spellId.replace(":", ".") + ".description";
                        Text descText = Text.translatable(descKey).formatted(Formatting.GRAY);
                        TextData textData = new TextData(title, descText, available, Text.empty());
                        setHoveredSkillInfo(textData);
                    }

                }
            }
            i++;
        }


    }

    private void updateSpellViewModels() {
        List<RegistryEntry<Spell>> playerSpells = SpellUtil.getPlayerSpells(player);
        List<SpellViewModel> viewModels = new ArrayList<>();
        for (RegistryEntry<Spell> spell : playerSpells) {
            if (spell == null) {
                viewModels.add(new SpellViewModel(null, null, true));
            } else {
                Identifier spellId = Identifier.of(spell.getIdAsString());
                Identifier iconId = SpellRender.iconTexture(spellId);
                SpellViewModel viewModel = new SpellViewModel(spell.getIdAsString(), iconId, learned.contains(spell));
                viewModels.add(viewModel);
            }
        }
        this.spellViewModels = viewModels;
    }

    private void renderDividerLine(DrawContext context, ScreenPosition screenPos) {
        int lineY = screenPos.y() + contentOffsetY2 + rectDim / 2;
        int lineStartX = screenPos.x() + skilltreeOffsetX + rectDim / 2;
        int lineEndX = screenPos.x() + skilltreeOffsetX + skilltreeWidth - rectDim / 2;

        context.drawHorizontalLine(lineStartX, lineEndX, lineY, DIVIDER_COLOR);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateCategory();
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        ScreenPosition screenPos = getScreenPosition();

        renderDescriptionLabel(context, screenPos);
        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 200f);
        renderExperienceInfo(context, screenPos, mouseX, mouseY);
        renderResetButton(context, mouseX, mouseY, screenPos);
        context.getMatrices().pop();

        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 250f);
        if (selectingSpell) {
            int col = 10;
            int iconS = 10;
            int iW = skilltreeWidth;
            int iconM = (iW - iconS*col)/(col+1) ;
            int size = learned.size();
            int row;
            if (size < col) {
                row = 1;
                iW = (iconS+iconM)*size + iconM;
            } else {
                row = size / col + (size % col == 0 ? 0 : 1);
            }
            int iH = (iconS+iconM)*row + iconM;
            int currentRow = 0;
            int currentCol = 0;

            int baseX = (width -iW)/2;
            int baseY = (height -iH)/2;
            context.drawTexture(
                    SKILLTREE_FRAME,
                    baseX,
                    baseY,
                    0, 0,
                    iW, iH,
                    iW, iH
            );
            for (RegistryEntry<Spell> spell : learned) {
                if (spell == null) continue;
                Identifier spellId = Identifier.of(spell.getIdAsString());
                Identifier iconId = SpellRender.iconTexture(spellId);
                int x = baseX + iconM + currentCol * (iconS + iconM);
                int y = baseY + iconM + currentRow * (iconS + iconM);
                context.drawTexture(iconId, x, y, 0, 0, iconS, iconS, iconS, iconS);
                currentCol++;
                if (currentCol >= col) {
                    currentCol = 0;
                    currentRow++;
                }
                if (currentRow >= row) {
                    break;
                }
            }
        }
        context.getMatrices().pop();


    }

    private void renderDescriptionLabel(DrawContext context, ScreenPosition screenPos) {
        Text descriptionText = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".skill.description")
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT));
        context.drawText(
                this.textRenderer,
                descriptionText,
                screenPos.x() + descriptionOffsetX + 4,
                screenPos.y() + contentOffsetY + 4,
                PRIMARY_TEXT_COLOR,
                false
        );

        // Render skill information in description box if a skill is hovered
        if (hoveredTextData != null) {
            renderSkillDescription(context, screenPos);
        }
    }

    private void renderSkillDescription(DrawContext context, ScreenPosition screenPos) {
        int descriptionX = screenPos.x() + descriptionOffsetX + 6;
        int descriptionY = screenPos.y() + contentOffsetY + 15;
        int maxWidth = descriptionWidth - 12;
        int lineHeight = 5;


        var titleText = MutableText.of(hoveredTextData.title().getContent()).setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));

        // Title
        var titleLines = textRenderer.wrapLines(titleText, maxWidth);
        for (OrderedText line : titleLines) {
            context.drawText(textRenderer, line, descriptionX, descriptionY, 0xFFFFFFFF, false);
            descriptionY += lineHeight;
        }
        descriptionY += 2;


        var descriptionText = MutableText.of(hoveredTextData.description().getContent()).setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));

        // Description
        var descriptionLines = textRenderer.wrapLines(descriptionText, maxWidth);
        for (var line : descriptionLines) {
            context.drawText(textRenderer, line, descriptionX, descriptionY, 0xFFCCCCCC, false);
            descriptionY += lineHeight;
        }

        descriptionY += 4;
        // Extra description (if Shift is held)
        var extraDescriptionText = MutableText.of(hoveredTextData.extraDescription().getContent()).setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));
        var extraDescriptionLines = textRenderer.wrapLines(extraDescriptionText, maxWidth);
        for (var line : extraDescriptionLines) {
            context.drawText(textRenderer, line, descriptionX, descriptionY, 0xFF888888, false);
            descriptionY += lineHeight;
        }

        // Skill ID (if advanced tooltips are enabled)
        if (client != null && client.options.advancedItemTooltips) {
            descriptionY += 4;
            Text skillId = hoveredTextData.advanced().copy().setStyle(Style.EMPTY.withFormatting((Formatting.DARK_GRAY)));
            context.drawText(textRenderer, skillId, descriptionX, descriptionY, 0xFF666666, false);
        }
    }

    private void renderExperienceInfo(DrawContext context, ScreenPosition screenPos, double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (categoryData == null || !categoryData.hasExperience() || client == null) {
            return;
        }
        renderSkillPoints(context, screenPos);


        final int level_bar_IMAGE_height = 31;
        final int level_bar_IMAGE_width = 1018;
        final int level_part_IMAGE_width = 122;
        final int level_bar_height = (int) (level_bar_IMAGE_height * SCALE);
        final int level_bar_width = (int) (level_bar_IMAGE_width * SCALE);
        final int level_part_width = (int) (level_part_IMAGE_width * SCALE);


        // LVL
        Text levelText = Text.literal("Lvl " + categoryData.getCurrentLevel())
                .setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));
        int levelTextWidth = textRenderer.getWidth(levelText);
        int levelTextHeight = textRenderer.fontHeight;

        int lvl_x = screenPos.x() + skilltreeOffsetX + 10;
        int lvl_y = screenPos.y() + contentOffsetY + contentHeight - level_bar_height / 2 - 1;
        int lvl_text_x = lvl_x + (level_part_width - levelTextWidth) / 2;
        int lvl_text_y = screenPos.y() + contentOffsetY + contentHeight - levelTextHeight / 2 - 1;

        if (mouseX >= lvl_x && mouseX < lvl_x + level_bar_width &&
                mouseY >= lvl_y && mouseY < lvl_y + level_bar_height) {
            var currentXP = categoryData.getCurrentExperience();
            var requiredXP = categoryData.getRequiredExperience();
            var lines = new ArrayList<OrderedText>();
            lines.add(Text.literal(currentXP + "/" + requiredXP).asOrderedText());
            setTooltip(lines);
        }


        context.drawTexture(LVL_BACKGROUND, lvl_x, lvl_y, 0, 0, level_bar_width, level_bar_height, level_bar_width, level_bar_height);
        context.drawText(this.textRenderer, levelText, lvl_text_x, lvl_text_y, PRIMARY_TEXT_COLOR, false);

        int bar_x = screenPos.x() + skilltreeOffsetX + 52;
        int bar_y = screenPos.y() + contentOffsetY + contentHeight - 3;
        // Experience bar background
        context.drawGuiTexture(EXPERIENCE_BAR_BACKGROUND, bar_x, bar_y, EXP_BAR_WIDTH, EXP_BAR_HEIGHT);

        // Experience bar progress
        int progressWidth = Math.min(EXP_BAR_WIDTH, (int) (categoryData.getExperienceProgress() * EXP_BAR_MAX_WIDTH));
        if (progressWidth > 0) {
            context.drawGuiTexture(
                    EXPERIENCE_BAR_PROGRESS,
                    EXP_BAR_WIDTH, EXP_BAR_HEIGHT,
                    0, 0,
                    bar_x, bar_y,
                    progressWidth, EXP_BAR_HEIGHT
            );
        }


    }

    private void renderSkillPoints(DrawContext context, ScreenPosition screenPos) {
        if (categoryData == null) return;
        Text pointsText = Text.literal("Skill points: " + categoryData.getPointsLeft())
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withFormatting(Formatting.UNDERLINE));
        var pointsColor = categoryData.getConfig().colors().points();
        int fillColor = pointsColor.fill().argb();
        context.drawText(
                this.textRenderer,
                pointsText,
                screenPos.x() + skilltreeOffsetX + 5,
                screenPos.y() + contentOffsetY + 4,
                fillColor,
                false
        );
    }


    private void renderResetButton(DrawContext context, int mouseX, int mouseY, ScreenPosition screenPos) {
        resetButton.render(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
        resetButton.renderTooltip(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
    }

    private boolean selectingSpell = false;
    private int selectedSpell = -1;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ScreenPosition screenPos = getScreenPosition();
        int i = 0;
        if (selectingSpell) {
            return selectSpell(mouseX, mouseY);
        } else {
            if (resetButton.handleClick(mouseX, mouseY, button, screenPos.x(), screenPos.y())) {
                return true;
            }
            for (Vector2i p : this.spellPositions) {
                if (mouseX >= p.x && mouseX <= p.x + rectDim && mouseY >= p.y && mouseY <=p.y + rectDim) {
                    return clickedOnSpell(i, button);
                }
                i++;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    private boolean selectSpell(double mouseX, double mouseY) {
        int col = 10;
        int iconS = 10;
        int iW = skilltreeWidth;
        int iconM = (iW - iconS*col)/(col+1) ;
        int size = learned.size();
        int row;
        if (size < col) {
            row = 1;
            iW = (iconS+iconM)*size + iconM;
        } else {
            row = size / col + (size % col == 0 ? 0 : 1);
        }
        int iH = (iconS+iconM)*row + iconM;
        int currentRow = 0;
        int currentCol = 0;

        int baseX = (width -iW)/2;
        int baseY = (height -iH)/2;
        int i = 0;
        for (RegistryEntry<Spell> spell : learned) {
            if (spell == null) continue;
            int x = baseX + iconM + currentCol * (iconS + iconM);
            int y = baseY + iconM + currentRow * (iconS + iconM);
            if (mouseX >= x && mouseX < x + iconS && mouseY >= y && mouseY < y + iconS) {
                updatePlayerSpell(selectedSpell, SpellUtil.getLearnedSpells(player).get(i));
                selectingSpell = false;
                selectedSpell = -1;
                return true;
            }
            i++;
            currentCol++;
            if (currentCol >= col) {
                currentCol = 0;
                currentRow++;
            }
            if (currentRow >= row) {
                break;
            }
        }
        return true;
    }

    private void updatePlayerSpell(int slotIndex, RegistryEntry<Spell> newSpell) {
        if (!(player instanceof IOnePiecePlayer onePiecePlayer)) return;

        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());
        while (current.size() <= slotIndex) current.add(null);

        RegistryEntry<Spell> oldValue = current.get(slotIndex);
        if (Objects.equals(oldValue, newSpell)) return;

        int existingIndex = findSpellIndex(current, newSpell);
        if (existingIndex != -1 && existingIndex != slotIndex) {
            current.set(existingIndex, oldValue);
        }

        current.set(slotIndex, newSpell);

        List<String> spellIds = current.stream()
                .map(spell -> spell != null ? spell.getIdAsString() : "")
                .toList();

        onePiecePlayer.onepiece$setSelectedSpellIds(spellIds);
        ClientPlayNetworking.send(new SetSpellsPayload(spellIds));
    }

    private int findSpellIndex(List<RegistryEntry<Spell>> spells, RegistryEntry<Spell> targetSpell) {
        for (int i = 0; i < spells.size(); i++) {
            if (Objects.equals(spells.get(i), targetSpell)) {
                return i;
            }
        }
        return -1;
    }


    private boolean clickedOnSpell(int i, int button) {
        if (button == 0) {
            return openSpellSelection(i);
        } else if (button == 1 ) {
            return deleteSpell(i);
        }
        return false;
    }

    private boolean openSpellSelection(int i) {
        selectedSpell = i;
        selectingSpell = true;
        return true;
    }

    private boolean deleteSpell(int i) {
        if (player instanceof IOnePiecePlayer onePiecePlayer) {
            if (removeSpellFromSlot(onePiecePlayer, i)) {
                playClickSound();
                return true;
            }
        }
        return false;
    }

    private boolean removeSpellFromSlot(IOnePiecePlayer onePiecePlayer, int slotIndex) {
        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());
        while (current.size() <= slotIndex) current.add(null);

        if (current.get(slotIndex) != null) {
            RegistryEntry<Spell> oldValue = current.get(slotIndex);
            current.set(slotIndex, null);

            List<String> spellIds = current.stream()
                    .map(spell -> spell != null ? spell.getIdAsString() : "")
                    .toList();

            if (oldValue != null) {
                onePiecePlayer.onepiece$setSelectedSpellIds(spellIds);
                ClientPlayNetworking.send(new SetSpellsPayload(spellIds));
                return true;
            }
        }
        return false;
    }

    private void playClickSound() {
        if (client != null && client.player != null) {
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
    }


    private ScreenPosition getScreenPosition() {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        return new ScreenPosition(x, y);
    }

    private static ClientSkillScreenData loadSkillScreenData() {
        try {
            SkillsClientMod skillsClientMod = SkillsClientMod.getInstance();
            var screenDataField = SkillsClientMod.class.getDeclaredField("screenData");
            screenDataField.setAccessible(true);
            return (ClientSkillScreenData) screenDataField.get(skillsClientMod);
        } catch (ReflectiveOperationException e) {
            OnePieceRPG.LOGGER.error("Failed to load SkillsScreen via reflection: {}", e.getMessage());
            return new ClientSkillScreenData(); // Fallback
        }
    }

    // Helper records for better organization
    private record ScreenPosition(int x, int y) {
    }

    private record ViewState(int x, int y, float scale) {
        void applyTo(ClientCategoryData categoryData) {
            categoryData.setX(x);
            categoryData.setY(y);
            categoryData.setScale(scale);
        }
    }
}