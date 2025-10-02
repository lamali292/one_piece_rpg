package de.one_piece_api.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.PostProcessingManager;
import de.one_piece_api.gui.util.Tab;
import de.one_piece_api.interfaces.IOnePiecePlayer;
import de.one_piece_api.items.DevilFruitItem;
import de.one_piece_api.network.UiPayload;
import de.one_piece_api.registries.MyFonts;
import de.one_piece_api.registries.MyKeys;
import de.one_piece_api.gui.managers.SpellManager;
import de.one_piece_api.gui.managers.SpellSlotManager;
import de.one_piece_api.gui.tabs.DevilFruitTab;
import de.one_piece_api.gui.tabs.HakiTab;
import de.one_piece_api.gui.tabs.SkillsTab;
import de.one_piece_api.gui.util.CustomButton;
import de.one_piece_api.gui.util.GenericTabbedScreen;
import de.one_piece_api.gui.widgets.*;
import de.one_piece_api.util.OnePieceCategory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import net.puffish.skillsmod.client.gui.SkillsScreen;
import net.spell_engine.api.spell.Spell;

/**
 * Main tab for the One Piece skill system.
 * Refactored with widget-based architecture for better maintainability.
 */
public class OnePieceScreen extends GenericTabbedScreen {
    // Singleton instance
    public static OnePieceScreen INSTANCE = null;

    // Texture constants
    private static final Identifier SKILL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/background.png");
    private static final Identifier SKILL_WIDE_RECT = OnePieceRPG.id("textures/gui/skill/boutique.png");
    private static final Identifier SKILLTREE_FRAME = OnePieceRPG.id("textures/gui/skill/skilltree.png");
    private static final Identifier DESCRIPTION_FRAME = OnePieceRPG.id("textures/gui/skill/description.png");
    private static final Identifier RESET_BUTTON = OnePieceRPG.id("textures/gui/skill/reskill.png");
    private static final Identifier RESET_BUTTON_HOVERED = OnePieceRPG.id("textures/gui/skill/reskill_hovered.png");

    // Layout constants
    private static final float SCALE = 0.23F;
    private static final int BUTTON_MARGIN = 3;

    // Calculated dimensions
    public static final int backgroundWidth = (int) (1785 * SCALE);
    public static final int backgroundHeight = (int) (985 * SCALE);
    public static final int skilltreeWidth = (int) (1202 * SCALE);
    public static final int contentHeight = (int) (688 * SCALE);
    public static final int descriptionWidth = (int) (456 * SCALE);
    public static final int boutiqueHeight = (int) (83 * SCALE);
    public static final int boutiqueWidth = (int) (363 * SCALE);
    public static final int rectDim = (int) (100 * SCALE);
    public static final int resetDim = (int) (68 * SCALE);

    // Calculated offsets
    public static final int contentOffsetY = (int) ((985 - 688) / 2F * SCALE);
    public static final int skilltreeOffsetX = (backgroundWidth - skilltreeWidth - descriptionWidth) / 3;
    public static final int descriptionOffsetX = 2 * skilltreeOffsetX + skilltreeWidth;
    public static final int topMargin = (backgroundHeight - contentHeight - contentOffsetY - rectDim) / 2;
    public static final int contentOffsetY2 = contentOffsetY + contentHeight + topMargin;

    // UI colors
    private static final int PRIMARY_TEXT_COLOR = 0xFFFFFFFF;
    private static final int DIVIDER_COLOR = 0xFFE2E0B4;

    // Core data
    private ClientSkillScreenData data;
    private ClientCategoryData categoryData = null;

    // Widgets
    private CustomButton resetButton;
    private DescriptionPanelWidget descriptionPanel;
    private ExperienceBarWidget experienceBar;
    private SkillPointsWidget skillPointsWidget;
    private SpellSlotManager spellSlotManager;
    private SpellSelectionOverlay spellSelectionOverlay;

    // Layout tracking
    private ScreenPosition lastScreenPosition = null;

    public OnePieceScreen(ClientPlayerEntity player) {
        super(Text.literal("Skill Tree"), player);
        init(MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight());
        INSTANCE = this;
        initializeComponents();
        setupTabs(player);
    }

    private void initializeComponents() {
        this.data = loadSkillScreenData();
        updateCategory();

        // Create widgets
        this.descriptionPanel = new DescriptionPanelWidget(
                descriptionOffsetX,
                contentOffsetY + 15,
                descriptionWidth,
                textRenderer
        );

        this.experienceBar = new ExperienceBarWidget(textRenderer, SCALE);
        this.experienceBar.setTooltipSetter(this::setTooltip);

        this.skillPointsWidget = new SkillPointsWidget(textRenderer);

        this.spellSlotManager = new SpellSlotManager(player, rectDim, skilltreeWidth);

        createResetButton();
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

    public ClientCategoryData getCategoryData() {
        return categoryData;
    }

    public boolean hasCategoryData() {
        return categoryData != null;
    }

    private static MutableText getTranslatable(String s) {
        return Text.translatable("gui."+ OnePieceRPG.MOD_ID+".tab."+s);
    }

    private void setupTabs(ClientPlayerEntity player) {
        int tabWidth = rectDim * 4;

        // Add Devil Fruit tab if player has one
        if (player instanceof IOnePiecePlayer iOnePiecePlayer) {
            String fruit = iOnePiecePlayer.onepiece$getDevilFruit();
            Identifier fruitId = Identifier.of(fruit);

            if (!DevilFruitItem.DEFAULT_DEVIL_FRUIT.equals(fruitId)) {
                addTab(getTranslatable("devilfruit"), new DevilFruitTab(this, fruitId), tabWidth);
                currentTab = 1;
            }
        }

        // Add standard tabs
        addTab(getTranslatable("class"), new SkillsTab(this), tabWidth);
        addTab(getTranslatable("haki"), new HakiTab(this), tabWidth);
    }

    public void reloadCategoryData() {
        if (categoryData == null) return;

        ViewState viewState = new ViewState(
                categoryData.getX(),
                categoryData.getY(),
                categoryData.getScale()
        );
        categoryData = data.getCategory(OnePieceCategory.ID).orElse(null);

        if (categoryData != null) {
            viewState.applyTo(categoryData);
            Tab currentTab = getCurrentTab();
            if (currentTab instanceof SkillsTab skillsTab) {
                skillsTab.refreshCategoryData();
            }
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

    public void updateLearned() {
        if (spellSlotManager != null) {
            spellSlotManager.updateSpellData();
        }
    }

    public void setHoveredSkillInfo(Text title, Text description, Text extraDescription, Text skillId) {
        if (descriptionPanel != null) {
            descriptionPanel.setContent(title, description, extraDescription, skillId);
        }
    }

    public void clearHoveredSkillInfo() {
        if (descriptionPanel != null) {
            descriptionPanel.clearContent();
        }
    }

    @Override
    protected void init() {
        super.init();
        // Force layout update on init
        if (spellSlotManager != null) {
            spellSlotManager.invalidateLayout();
        }
        lastScreenPosition = null;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        // Invalidate layout cache on resize
        if (spellSlotManager != null) {
            spellSlotManager.invalidateLayout();
        }
        lastScreenPosition = null;
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

        renderMainBackground(context, screenPos);
        renderFrames(context, screenPos);
        renderDividerLine(context, screenPos);
        renderSpellSlots(context, screenPos, mouseX, mouseY);

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

    private void renderDividerLine(DrawContext context, ScreenPosition screenPos) {
        int lineY = screenPos.y() + contentOffsetY2 + rectDim / 2;
        int lineStartX = screenPos.x() + skilltreeOffsetX + rectDim / 2;
        int lineEndX = screenPos.x() + skilltreeOffsetX + skilltreeWidth - rectDim / 2;

        context.drawHorizontalLine(lineStartX, lineEndX, lineY, DIVIDER_COLOR);
    }

    private void renderSpellSlots(DrawContext context, ScreenPosition screenPos, int mouseX, int mouseY) {
        // Update layout only if screen position changed
        updateSpellSlotLayoutIfNeeded(screenPos);

        // Clear hover state first
        boolean foundHover = false;

        // Render all slots and check for hover
        for (SpellSlotWidget slot : spellSlotManager.getSlots()) {
            slot.render(context, mouseX, mouseY, 0);

            // Check hover with tab coordinates
            if (!foundHover) {
                var pos = slot.getPosition();
                if (mouseX >= pos.x && mouseX < pos.x + rectDim &&
                        mouseY >= pos.y && mouseY < pos.y + rectDim) {

                    // Set hover handler and trigger
                    slot.setOnHover(info -> {
                        descriptionPanel.setContent(
                                info.title(),
                                info.description(),
                                info.extraDescription(),
                                info.advanced()
                        );
                    });
                    foundHover = true;
                }
            }
        }
    }

    /**
     * Only updates spell slot layout if screen position has changed.
     */
    private void updateSpellSlotLayoutIfNeeded(ScreenPosition screenPos) {
        if (lastScreenPosition == null || !lastScreenPosition.equals(screenPos)) {
            int baseX = screenPos.x() + skilltreeOffsetX;
            int baseY = screenPos.y() + contentOffsetY2;

            spellSlotManager.updateLayoutIfNeeded(baseX, baseY, skilltreeWidth);
            spellSlotManager.updateSpellData();

            lastScreenPosition = screenPos;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateCategory();
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        ScreenPosition screenPos = getScreenPosition();

        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 200f);

        renderDescriptionSection(context, screenPos, mouseX, mouseY);
        renderExperienceSection(context, screenPos, mouseX, mouseY);
        renderResetButton(context, mouseX, mouseY, screenPos);

        context.getMatrices().pop();

        // Render spell selection overlay if active
        if (spellSelectionOverlay != null) {
            context.getMatrices().push();
            context.getMatrices().translate(0f, 0f, 250f);
            spellSelectionOverlay.render(context, mouseX, mouseY, delta);
            context.getMatrices().pop();
        }
    }

    private void renderDescriptionSection(DrawContext context, ScreenPosition screenPos, int mouseX, int mouseY) {
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

        // Render description panel
        descriptionPanel.setShowAdvanced(client != null && client.options.advancedItemTooltips);

        context.getMatrices().push();
        context.getMatrices().translate(screenPos.x(), screenPos.y(), 0);
        descriptionPanel.render(context, mouseX - screenPos.x(), mouseY - screenPos.y(), 0);
        context.getMatrices().pop();
    }

    private void renderExperienceSection(DrawContext context, ScreenPosition screenPos, int mouseX, int mouseY) {
        // Update widget data
        experienceBar.setCategoryData(categoryData);
        skillPointsWidget.setCategoryData(categoryData);

        // Update widget positions
        int barX = screenPos.x() + skilltreeOffsetX + 10;
        int barY = screenPos.y() + contentOffsetY + contentHeight - 6;
        experienceBar.setPosition(barX, barY);

        int pointsX = screenPos.x() + skilltreeOffsetX + 5;
        int pointsY = screenPos.y() + contentOffsetY + 4;
        skillPointsWidget.setPosition(pointsX, pointsY);

        // Render
        experienceBar.render(context, mouseX, mouseY, 0);
        skillPointsWidget.render(context, mouseX, mouseY, 0);
    }

    private void renderResetButton(DrawContext context, int mouseX, int mouseY, ScreenPosition screenPos) {
        resetButton.render(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
        resetButton.renderTooltip(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ScreenPosition screenPos = getScreenPosition();

        // Handle spell selection overlay clicks
        if (spellSelectionOverlay != null) {
            boolean handled = spellSelectionOverlay.mouseClicked(mouseX, mouseY, button);
            if (handled) {
                spellSelectionOverlay = null;
            }
            return handled;
        }

        // Handle reset button clicks
        if (resetButton.handleClick(mouseX, mouseY, button, screenPos.x(), screenPos.y())) {
            return true;
        }

        // Handle spell slot clicks
        for (SpellSlotWidget slot : spellSlotManager.getSlots()) {
            if (slot.mouseClicked(mouseX, mouseY, button)) {
                if (button == 0) {
                    // Left click - open selection
                    openSpellSelection(slot.getSlotIndex());
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openSpellSelection(int slotIndex) {
        SpellManager spellManager = spellSlotManager.getSpellManager();

        spellSelectionOverlay = new SpellSelectionOverlay(
                slotIndex,
                spellManager.getLearnedSpells(),
                this::handleSpellSelected,
                this::handleSelectionCancelled
        );
    }

    private void handleSpellSelected(int slotIndex, RegistryEntry<Spell> spell) {
        spellSlotManager.getSpellManager().updateSpellSlot(slotIndex, spell);
        spellSlotManager.updateSpellData();
        spellSelectionOverlay = null;
    }

    private void handleSelectionCancelled() {
        spellSelectionOverlay = null;
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
            return new ClientSkillScreenData();
        }
    }

    // Helper records
    private record ScreenPosition(int x, int y) {}

    private record ViewState(int x, int y, float scale) {
        void applyTo(ClientCategoryData categoryData) {
            categoryData.setX(x);
            categoryData.setY(y);
            categoryData.setScale(scale);
        }
    }
}