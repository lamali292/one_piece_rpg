package de.one_piece_api.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.PostProcessingManager;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.gui.util.Tab;
import de.one_piece_api.interfaces.IDevilFruitPlayer;
import de.one_piece_api.network.DevilFruitPayload;
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
import de.one_piece_api.util.ClientData;
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
import net.spell_engine.api.spell.Spell;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Main screen for the One Piece skill system.
 * Features a tabbed interface with skill trees, description panels, and spell management.
 * <p>
 * Architecture:
 * - Widget-based UI components for better separation of concerns
 * - Immutable layout constants for consistency
 * - Lazy layout updates to minimize redraws
 * - Proper resource cleanup and null safety
 */
public class OnePieceScreen extends GenericTabbedScreen {

    // ==================== Singleton ====================
    private static OnePieceScreen instance = null;

    public static OnePieceScreen getInstance() {
        if (instance == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            instance = new OnePieceScreen(client.player);
        }
        return instance;
    }

    public void open(MinecraftClient client) {
        client.setScreen(this);
        reloadCategoryData();
    }

    // ==================== Constants ====================

    // Texture resources
    private static final class Textures {
        static final Identifier SKILL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/background.png");
        static final Identifier SKILL_WIDE_RECT = OnePieceRPG.id("textures/gui/skill/boutique.png");
        static final Identifier SKILLTREE_FRAME = OnePieceRPG.id("textures/gui/skill/skilltree.png");
        static final Identifier DESCRIPTION_FRAME = OnePieceRPG.id("textures/gui/skill/description.png");
        static final Identifier RESET_BUTTON = OnePieceRPG.id("textures/gui/skill/reskill.png");
        static final Identifier RESET_BUTTON_HOVERED = OnePieceRPG.id("textures/gui/skill/reskill_hovered.png");
    }

    // Layout configuration
    public static final class Layout {
        static final float SCALE = 0.23F;
        static final int BUTTON_MARGIN = 3;

        // Scaled dimensions - made public for backward compatibility
        public static final int BACKGROUND_WIDTH = scale(1785);
        public static final int BACKGROUND_HEIGHT = scale(985);
        public static final int SKILLTREE_WIDTH = scale(1202);
        public static final int CONTENT_HEIGHT = scale(688);
        public static final int DESCRIPTION_WIDTH = scale(456);
        public static final int BOUTIQUE_HEIGHT = scale(83);
        public static final int BOUTIQUE_WIDTH = scale(363);
        public static final int RECT_DIM = scale(100);
        public static final int RESET_DIM = scale(68);

        // Calculated offsets - made public for backward compatibility
        public static final int CONTENT_OFFSET_Y = scale((985 - 688) / 2);
        public static final int SKILLTREE_OFFSET_X = (BACKGROUND_WIDTH - SKILLTREE_WIDTH - DESCRIPTION_WIDTH) / 3;
        public static final int DESCRIPTION_OFFSET_X = 2 * SKILLTREE_OFFSET_X + SKILLTREE_WIDTH;
        public static final int TOP_MARGIN = (BACKGROUND_HEIGHT - CONTENT_HEIGHT - CONTENT_OFFSET_Y - RECT_DIM) / 2;
        public static final int CONTENT_OFFSET_Y2 = CONTENT_OFFSET_Y + CONTENT_HEIGHT + TOP_MARGIN;

        private static int scale(int value) {
            return (int) (value * SCALE);
        }

        private static int scale(float value) {
            return (int) (value * SCALE);
        }
    }

    // UI color scheme
    private static final class Colors {
        static final int PRIMARY_TEXT = 0xFFFFFFFF;
        static final int DIVIDER = 0xFFE2E0B4;
    }

    // ==================== State ====================

    private final ClientSkillScreenData skillScreenData;
    private ClientCategoryData categoryData;

    // UI Components
    private CustomButton resetButton;
    private DescriptionPanelWidget descriptionPanel;
    private ExperienceBarWidget experienceBar;
    private SkillPointsWidget skillPointsWidget;
    private SpellSlotManager spellSlotManager;
    private SpellSelectionOverlay spellSelectionOverlay;

    // Layout caching
    private ScreenPosition cachedScreenPosition;

    // ==================== Construction ====================

    public OnePieceScreen(ClientPlayerEntity player) {
        super(Text.literal("Skill Tree"), player);
        ClientData.DEVIL_FRUIT_CONFIG.addListener(this::onDevilFruitUpdate);
        this.skillScreenData = loadSkillScreenData();
        this.categoryData = loadCategoryData();

        // Set singleton
        instance = this;
    }



    private void initializeWidgets() {
        // This is called from init() when textRenderer is available
        if (this.descriptionPanel == null) {
            this.descriptionPanel = createDescriptionPanel();
        }
        if (this.experienceBar == null) {
            this.experienceBar = createExperienceBar();
        }
        if (this.skillPointsWidget == null) {
            this.skillPointsWidget = createSkillPointsWidget();
        }
        if (this.spellSlotManager == null) {
            this.spellSlotManager = createSpellSlotManager();
        }
        if (this.resetButton == null) {
            this.resetButton = createResetButton();
        }
    }

    private DescriptionPanelWidget createDescriptionPanel() {
        if (textRenderer == null) {
            throw new IllegalStateException("Cannot create DescriptionPanelWidget: textRenderer not initialized");
        }
        return new DescriptionPanelWidget(
                Layout.DESCRIPTION_OFFSET_X,
                Layout.CONTENT_OFFSET_Y + 15,
                Layout.DESCRIPTION_WIDTH,
                textRenderer
        );
    }

    private ExperienceBarWidget createExperienceBar() {
        if (textRenderer == null) {
            throw new IllegalStateException("Cannot create ExperienceBarWidget: textRenderer not initialized");
        }
        ExperienceBarWidget widget = new ExperienceBarWidget(textRenderer, Layout.SCALE);
        widget.setTooltipSetter(this::setTooltip);
        return widget;
    }

    private SkillPointsWidget createSkillPointsWidget() {
        if (textRenderer == null) {
            throw new IllegalStateException("Cannot create SkillPointsWidget: textRenderer not initialized");
        }
        return new SkillPointsWidget(textRenderer);
    }

    private SpellSlotManager createSpellSlotManager() {
        return new SpellSlotManager(player, Layout.RECT_DIM, Layout.SKILLTREE_WIDTH);
    }

    private CustomButton createResetButton() {
        int buttonX = Layout.SKILLTREE_OFFSET_X + Layout.SKILLTREE_WIDTH - Layout.RESET_DIM - Layout.BUTTON_MARGIN;
        int buttonY = Layout.CONTENT_OFFSET_Y + Layout.BUTTON_MARGIN;

        ButtonTextures textures = new ButtonTextures(
                Textures.RESET_BUTTON,
                Textures.RESET_BUTTON_HOVERED
        );

        return new CustomButton(
                buttonX, buttonY,
                Layout.RESET_DIM, Layout.RESET_DIM,
                Text.literal(""),
                textures,
                this::handleResetClick
        );
    }

    // ==================== Tab Setup ====================

    private Identifier fruitId = null;
    private void onDevilFruitUpdate(DevilFruitConfig newEntry) {
        setTabs(newEntry);
    }


    public void setTabs(DevilFruitConfig devilFruitConfig) {
        int tabWidth = Layout.RECT_DIM * 4;
        tabs.clear();
        if (devilFruitConfig != null && !devilFruitConfig.paths().isEmpty()) {
            addTab(getTranslatable("devilfruit"), new DevilFruitTab(this, devilFruitConfig), tabWidth);
        }
        addTab(getTranslatable("class"), new SkillsTab(this), tabWidth);
        addTab(getTranslatable("haki"), new HakiTab(this), tabWidth);
    }

    private static MutableText getTranslatable(String key) {
        return Text.translatable("gui." + OnePieceRPG.MOD_ID + ".tab." + key);
    }

    // ==================== Data Management ====================

    public ClientCategoryData getCategoryData() {
        return categoryData;
    }

    public boolean hasCategoryData() {
        return categoryData != null;
    }


    public void updateData() {
        if (categoryData == null) {
            categoryData = loadCategoryData();
        }

        if (categoryData != null) {
            categoryData.updateLastOpen();
            categoryData.updateUnseenPoints();
        }

        if (player instanceof IDevilFruitPlayer onePiecePlayer) {
            String fruitString = onePiecePlayer.onepiece$getDevilFruit();
            Identifier playerFruitId = Identifier.of(fruitString);
            if (!playerFruitId.equals(fruitId)) {
                fruitId = playerFruitId;
                ClientPlayNetworking.send(new DevilFruitPayload.Request(playerFruitId));
            }

        }

    }

    public void reloadCategoryData() {
        if (categoryData == null) {
            return;
        }

        // Save current view state
        ViewState viewState = ViewState.capture(categoryData);

        // Reload data
        categoryData = loadCategoryData();

        // Restore view state
        if (categoryData != null) {
            viewState.applyTo(categoryData);
            refreshCurrentTab();
        }
    }

    private void refreshCurrentTab() {
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof SkillsTab skillsTab) {
            skillsTab.refreshCategoryData();
        }
    }

    public void updateLearned() {
        if (spellSlotManager != null) {
            spellSlotManager.updateSpellData();
        }
    }

    // ==================== Description Panel Control ====================

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

    // ==================== Lifecycle ====================

    @Override
    protected void init() {
        super.init();

        // Initialize widgets now that textRenderer is available
        if (descriptionPanel == null) {
            initializeWidgets();
        }

        // Setup tabs if not already done
        if (tabs.isEmpty()) {
            var config = ClientData.DEVIL_FRUIT_CONFIG.get().orElse(null);
            setTabs(config);
        }

        invalidateLayout();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        invalidateLayout();
    }

    @Override
    public void close() {
        super.close();
        //instance = null;
    }

    private void invalidateLayout() {
        cachedScreenPosition = null;
        if (spellSlotManager != null) {
            spellSlotManager.invalidateLayout();
        }
    }

    // ==================== Input Handling ====================

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (client != null && MyKeys.getKeyBinding(MyKeys.OPEN_SKILL_TREE_KEY).matchesKey(keyCode, scanCode)) {
            client.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ScreenPosition screenPos = getScreenPosition();

        // Handle spell selection overlay (highest priority)
        if (handleOverlayClick(mouseX, mouseY, button)) {
            return true;
        }

        // Handle reset button
        if (resetButton.handleClick(mouseX, mouseY, button, screenPos.x(), screenPos.y())) {
            return true;
        }

        // Handle spell slot clicks
        if (handleSpellSlotClick(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleOverlayClick(double mouseX, double mouseY, int button) {
        if (spellSelectionOverlay == null) {
            return false;
        }

        boolean handled = spellSelectionOverlay.mouseClicked(mouseX, mouseY, button);
        if (handled) {
            closeSpellSelection();
        }
        return handled;
    }

    private boolean handleSpellSlotClick(double mouseX, double mouseY, int button) {
        for (SpellSlotWidget slot : spellSlotManager.getSlots()) {
            if (slot.mouseClicked(mouseX, mouseY, button)) {
                if (button == 0) { // Left click
                    openSpellSelection(slot.getSlotIndex());
                }
                return true;
            }
        }
        return false;
    }

    private void handleResetClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.mouse != null && client.getWindow() != null) {
            float mouseX = (float) client.mouse.getX() / client.getWindow().getWidth();
            float mouseY = (float) client.mouse.getY() / client.getWindow().getHeight();
            PostProcessingManager.click(client, mouseX, mouseY);
        }
        ClientPlayNetworking.send(new UiPayload("reset"));
    }

    // ==================== Spell Selection ====================

    private void openSpellSelection(int slotIndex) {
        SpellManager spellManager = spellSlotManager.getSpellManager();

        spellSelectionOverlay = new SpellSelectionOverlay(
                slotIndex,
                spellManager.getLearnedSpells(),
                this::handleSpellSelected,
                this::closeSpellSelection
        );
    }

    private void handleSpellSelected(int slotIndex, RegistryEntry<Spell> spell) {
        spellSlotManager.getSpellManager().updateSpellSlot(slotIndex, spell);
        spellSlotManager.updateSpellData();
        closeSpellSelection();
    }

    private void closeSpellSelection() {
        spellSelectionOverlay = null;
    }

    // ==================== Rendering ====================

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        ScreenPosition screenPos = getScreenPosition();

        RenderSystem.enableBlend();
        try {
            renderMainBackground(context, screenPos);
            renderFrames(context, screenPos);
            renderDividerLine(context, screenPos);
            renderSpellSlots(context, screenPos, mouseX, mouseY);
        } finally {
            RenderSystem.disableBlend();
        }
    }

    private void renderMainBackground(DrawContext context, ScreenPosition screenPos) {
        context.drawTexture(
                Textures.SKILL_BACKGROUND,
                screenPos.x(), screenPos.y(),
                0, 0,
                Layout.BACKGROUND_WIDTH, Layout.BACKGROUND_HEIGHT,
                Layout.BACKGROUND_WIDTH, Layout.BACKGROUND_HEIGHT
        );
    }

    private void renderFrames(DrawContext context, ScreenPosition screenPos) {
        // Skill tree frame
        context.drawTexture(
                Textures.SKILLTREE_FRAME,
                screenPos.x() + Layout.SKILLTREE_OFFSET_X,
                screenPos.y() + Layout.CONTENT_OFFSET_Y,
                0, 0,
                Layout.SKILLTREE_WIDTH, Layout.CONTENT_HEIGHT,
                Layout.SKILLTREE_WIDTH, Layout.CONTENT_HEIGHT
        );

        // Description frame
        context.drawTexture(
                Textures.DESCRIPTION_FRAME,
                screenPos.x() + Layout.DESCRIPTION_OFFSET_X,
                screenPos.y() + Layout.CONTENT_OFFSET_Y,
                0, 0,
                Layout.DESCRIPTION_WIDTH, Layout.CONTENT_HEIGHT,
                Layout.DESCRIPTION_WIDTH, Layout.CONTENT_HEIGHT
        );

        // Boutique frame
        int boutiqueX = screenPos.x() + Layout.DESCRIPTION_OFFSET_X +
                (Layout.DESCRIPTION_WIDTH - Layout.BOUTIQUE_WIDTH) / 2;
        int boutiqueY = screenPos.y() + Layout.CONTENT_OFFSET_Y2 +
                (Layout.RECT_DIM - Layout.BOUTIQUE_HEIGHT) / 2;

        context.drawTexture(
                Textures.SKILL_WIDE_RECT,
                boutiqueX, boutiqueY,
                0, 0,
                Layout.BOUTIQUE_WIDTH, Layout.BOUTIQUE_HEIGHT,
                Layout.BOUTIQUE_WIDTH, Layout.BOUTIQUE_HEIGHT
        );
    }

    private void renderDividerLine(DrawContext context, ScreenPosition screenPos) {
        int lineY = screenPos.y() + Layout.CONTENT_OFFSET_Y2 + Layout.RECT_DIM / 2;
        int lineStartX = screenPos.x() + Layout.SKILLTREE_OFFSET_X + Layout.RECT_DIM / 2;
        int lineEndX = screenPos.x() + Layout.SKILLTREE_OFFSET_X + Layout.SKILLTREE_WIDTH - Layout.RECT_DIM / 2;

        context.drawHorizontalLine(lineStartX, lineEndX, lineY, Colors.DIVIDER);
    }

    private void renderSpellSlots(DrawContext context, ScreenPosition screenPos, int mouseX, int mouseY) {
        updateSpellSlotLayoutIfNeeded(screenPos);

        boolean foundHover = false;

        for (SpellSlotWidget slot : spellSlotManager.getSlots()) {
            slot.render(context, mouseX, mouseY, 0);

            if (!foundHover && isMouseOverSlot(slot, mouseX, mouseY)) {
                setSlotHoverHandler(slot);
                foundHover = true;
            }
        }
    }

    private boolean isMouseOverSlot(SpellSlotWidget slot, int mouseX, int mouseY) {
        var pos = slot.getPosition();
        return mouseX >= pos.x && mouseX < pos.x + Layout.RECT_DIM &&
                mouseY >= pos.y && mouseY < pos.y + Layout.RECT_DIM;
    }

    private void setSlotHoverHandler(SpellSlotWidget slot) {
        slot.setOnHover(info -> {
            descriptionPanel.setContent(
                    info.title(),
                    info.description(),
                    info.extraDescription(),
                    info.advanced()
            );
        });
    }

    private void updateSpellSlotLayoutIfNeeded(ScreenPosition screenPos) {
        if (cachedScreenPosition != null && cachedScreenPosition.equals(screenPos)) {
            return; // Layout hasn't changed
        }

        int baseX = screenPos.x() + Layout.SKILLTREE_OFFSET_X;
        int baseY = screenPos.y() + Layout.CONTENT_OFFSET_Y2;

        spellSlotManager.updateLayoutIfNeeded(baseX, baseY, Layout.SKILLTREE_WIDTH);
        spellSlotManager.updateSpellData();

        cachedScreenPosition = screenPos;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateData();
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Update spell data every frame to catch server updates
        if (spellSlotManager != null) {
            spellSlotManager.updateSpellData();
        }

        ScreenPosition screenPos = getScreenPosition();

        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 200f);

        try {
            renderDescriptionSection(context, screenPos, mouseX, mouseY);
            renderExperienceSection(context, screenPos, mouseX, mouseY);
            renderResetButton(context, mouseX, mouseY, screenPos);
        } finally {
            context.getMatrices().pop();
        }

        // Render spell selection overlay on top
        if (spellSelectionOverlay != null) {
            context.getMatrices().push();
            context.getMatrices().translate(0f, 0f, 250f);
            try {
                spellSelectionOverlay.render(context, mouseX, mouseY, delta);
            } finally {
                context.getMatrices().pop();
            }
        }
    }

    private void renderDescriptionSection(DrawContext context, ScreenPosition screenPos, int mouseX, int mouseY) {
        // Render title
        Text title = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".skill.description")
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT));

        context.drawText(
                textRenderer,
                title,
                screenPos.x() + Layout.DESCRIPTION_OFFSET_X + 4,
                screenPos.y() + Layout.CONTENT_OFFSET_Y + 4,
                Colors.PRIMARY_TEXT,
                false
        );

        // Render description panel
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

    private void renderExperienceSection(DrawContext context, ScreenPosition screenPos, int mouseX, int mouseY) {
        // Update widget data
        experienceBar.setCategoryData(categoryData);
        skillPointsWidget.setCategoryData(categoryData);

        // Update positions
        int barX = screenPos.x() + Layout.SKILLTREE_OFFSET_X + 10;
        int barY = screenPos.y() + Layout.CONTENT_OFFSET_Y + Layout.CONTENT_HEIGHT - 6;
        experienceBar.setPosition(barX, barY);

        int pointsX = screenPos.x() + Layout.SKILLTREE_OFFSET_X + 5;
        int pointsY = screenPos.y() + Layout.CONTENT_OFFSET_Y + 4;
        skillPointsWidget.setPosition(pointsX, pointsY);

        // Render
        experienceBar.render(context, mouseX, mouseY, 0);
        skillPointsWidget.render(context, mouseX, mouseY, 0);
    }

    private void renderResetButton(DrawContext context, int mouseX, int mouseY, ScreenPosition screenPos) {
        resetButton.render(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
        resetButton.renderTooltip(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
    }

    // ==================== Helper Methods ====================

    private ScreenPosition getScreenPosition() {
        int x = (width - Layout.BACKGROUND_WIDTH) / 2;
        int y = (height - Layout.BACKGROUND_HEIGHT) / 2;
        return new ScreenPosition(x, y);
    }

    private ClientCategoryData loadCategoryData() {
        return skillScreenData.getCategory(OnePieceCategory.ID).orElse(null);
    }

    private static ClientSkillScreenData loadSkillScreenData() {
        try {
            SkillsClientMod skillsClientMod = SkillsClientMod.getInstance();
            Field screenDataField = SkillsClientMod.class.getDeclaredField("screenData");
            screenDataField.setAccessible(true);

            Object data = screenDataField.get(skillsClientMod);
            if (data instanceof ClientSkillScreenData skillData) {
                return skillData;
            }

            OnePieceRPG.LOGGER.warn("Failed to cast screen data, using empty instance");
            return new ClientSkillScreenData();

        } catch (NoSuchFieldException e) {
            OnePieceRPG.LOGGER.error("Field 'screenData' not found in SkillsClientMod: {}", e.getMessage());
            return new ClientSkillScreenData();
        } catch (IllegalAccessException e) {
            OnePieceRPG.LOGGER.error("Cannot access 'screenData' field: {}", e.getMessage());
            return new ClientSkillScreenData();
        } catch (Exception e) {
            OnePieceRPG.LOGGER.error("Unexpected error loading skill screen data: {}", e.getMessage(), e);
            return new ClientSkillScreenData();
        }
    }

    // ==================== Helper Records ====================

    private record ScreenPosition(int x, int y) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ScreenPosition other)) return false;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private record ViewState(int x, int y, float scale) {
        static ViewState capture(ClientCategoryData categoryData) {
            return new ViewState(
                    categoryData.getX(),
                    categoryData.getY(),
                    categoryData.getScale()
            );
        }

        void applyTo(ClientCategoryData categoryData) {
            categoryData.setX(x);
            categoryData.setY(y);
            categoryData.setScale(scale);
        }
    }
}