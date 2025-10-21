package de.one_piece_api.screen;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.render.PostProcessingManager;
import de.one_piece_api.screen.component.CustomButton;
import de.one_piece_api.screen.component.GenericTabbedScreen;
import de.one_piece_api.screen.manager.*;
import de.one_piece_api.screen.tab.DevilFruitTab;
import de.one_piece_api.screen.tab.HakiTab;
import de.one_piece_api.screen.tab.SkillsTab;
import de.one_piece_api.init.MyKeys;
import de.one_piece_api.network.payload.UiPayload;
import de.one_piece_api.screen.widget.main.*;
import de.one_piece_api.util.ClientData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.spell_engine.api.spell.Spell;

/**
 * Main screen for the One Piece skill system.
 * Acts as a coordinator between data, rendering, and user interaction.
 */
public class OnePieceScreen extends GenericTabbedScreen {

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
        dataManager.reloadCategoryData();
    }

    // ==================== Layout Constants ====================

    public static final class Layout {
        // Reference dimensions (same as ClassScreen)
        private static final int REFERENCE_WIDTH = 1920;
        private static final int REFERENCE_HEIGHT = 1080;
        private static final int REFERENCE_BACKGROUND_WIDTH = 1785;
        private static final int REFERENCE_BACKGROUND_HEIGHT = 985;

        // Original scale for backward compatibility
        static final float BASE_SCALE = 0.23F;

        // Button and UI constants
        static final int BUTTON_MARGIN = 3;

        // These will be calculated dynamically based on screen size
        public static int BACKGROUND_WIDTH = scaleStatic(REFERENCE_BACKGROUND_WIDTH);
        public static int BACKGROUND_HEIGHT = scaleStatic(REFERENCE_BACKGROUND_HEIGHT);
        public static int SKILLTREE_WIDTH = scaleStatic(1202);
        public static int CONTENT_HEIGHT = scaleStatic(688);
        public static int DESCRIPTION_WIDTH = scaleStatic(456);
        public static int BOUTIQUE_HEIGHT = scaleStatic(83);
        public static int BOUTIQUE_WIDTH = scaleStatic(363);
        public static int RECT_DIM = scaleStatic(100);
        public static int RESET_DIM = scaleStatic(68);
        public static int CONTENT_OFFSET_Y = scaleStatic((985 - 688) / 2);
        public static int SKILLTREE_OFFSET_X = (BACKGROUND_WIDTH - SKILLTREE_WIDTH - DESCRIPTION_WIDTH) / 3;
        public static int DESCRIPTION_OFFSET_X = 2 * SKILLTREE_OFFSET_X + SKILLTREE_WIDTH;
        public static int TOP_MARGIN = (BACKGROUND_HEIGHT - CONTENT_HEIGHT - CONTENT_OFFSET_Y - RECT_DIM) / 2;
        public static int CONTENT_OFFSET_Y2 = CONTENT_OFFSET_Y + CONTENT_HEIGHT + TOP_MARGIN;

        private static int scaleStatic(int value) {
            return (int) (value * BASE_SCALE);
        }

        /**
         * Updates all layout dimensions based on screen size.
         * Call this when screen is resized.
         */
        public static void updateDimensions(int screenWidth, int screenHeight) {
            float scaleX = (float) screenWidth / REFERENCE_WIDTH;
            float scaleY = (float) screenHeight / REFERENCE_HEIGHT;
            float minScale = Math.min(scaleX, scaleY);

            // Apply responsive scaling
            BACKGROUND_WIDTH = (int) (minScale * REFERENCE_BACKGROUND_WIDTH);
            BACKGROUND_HEIGHT = (int) (minScale * REFERENCE_BACKGROUND_HEIGHT);
            SKILLTREE_WIDTH = (int) (minScale * 1202);
            CONTENT_HEIGHT = (int) (minScale * 688);
            DESCRIPTION_WIDTH = (int) (minScale * 456);
            BOUTIQUE_HEIGHT = (int) (minScale * 83);
            BOUTIQUE_WIDTH = (int) (minScale * 363);
            RECT_DIM = (int) (minScale * 100);
            RESET_DIM = (int) (minScale * 68);
            CONTENT_OFFSET_Y = (int) (minScale * ((985 - 688) / 2f));
            SKILLTREE_OFFSET_X = (BACKGROUND_WIDTH - SKILLTREE_WIDTH - DESCRIPTION_WIDTH) / 3;
            DESCRIPTION_OFFSET_X = 2 * SKILLTREE_OFFSET_X + SKILLTREE_WIDTH;
            TOP_MARGIN = (BACKGROUND_HEIGHT - CONTENT_HEIGHT - CONTENT_OFFSET_Y - RECT_DIM) / 2;
            CONTENT_OFFSET_Y2 = CONTENT_OFFSET_Y + CONTENT_HEIGHT + TOP_MARGIN;
        }

        /**
         * Gets the current responsive scale factor
         */
        public static float getCurrentScale(int screenWidth, int screenHeight) {
            float scaleX = (float) screenWidth / REFERENCE_WIDTH;
            float scaleY = (float) screenHeight / REFERENCE_HEIGHT;
            return Math.min(scaleX, scaleY);
        }
    }

    // ==================== Managers ====================

    private final ScreenDataManager dataManager;
    private ScreenRenderManager renderManager;
    private final ScreenUpdateCoordinator updateCoordinator;

    // UI Components
    private CustomButton resetButton;
    private DescriptionPanelWidget descriptionPanel;
    private ExperienceBarWidget experienceBar;
    private SkillPointsWidget skillPointsWidget;
    private SpellSlotManager spellSlotManager;
    private SpellSelectionOverlay spellSelectionOverlay;

    // ==================== Construction ====================

    public OnePieceScreen(ClientPlayerEntity player) {
        super(Text.literal("Skill Tree"), player);

        // Initialize managers
        this.dataManager = new ScreenDataManager(player);
        this.updateCoordinator = new ScreenUpdateCoordinator();

        // Widgets will be initialized in init()
        this.descriptionPanel = null;
        this.experienceBar = null;
        this.skillPointsWidget = null;
        this.renderManager = null;

        instance = this;
    }

    // ==================== Lifecycle ====================

    @Override
    protected void init() {
        super.init();

        // Update layout dimensions for current screen size
        Layout.updateDimensions(width, height);

        initializeWidgets();

        if (tabs.isEmpty()) {
            var config = ClientData.DEVIL_FRUIT_CONFIG.get().orElse(null);
            setTabs(config);
        }
        ClientData.invalidate(ClientData.DataInvalidationType.ALL);
    }

    private void initializeWidgets() {
        float currentScale = Layout.getCurrentScale(width, height);

        // Create widgets with responsive dimensions
        this.descriptionPanel = new DescriptionPanelWidget(
                Layout.DESCRIPTION_OFFSET_X,
                Layout.CONTENT_OFFSET_Y + 15,
                Layout.DESCRIPTION_WIDTH,
                textRenderer
        );

        // Create or update experience bar with current scale
        if (this.experienceBar == null) {
            this.experienceBar = new ExperienceBarWidget(textRenderer, currentScale);
        } else {
            this.experienceBar.updateScale(currentScale);
        }
        experienceBar.setTooltipSetter(this::setTooltip);

        this.skillPointsWidget = new SkillPointsWidget(textRenderer);
        this.spellSlotManager = new SpellSlotManager(player, Layout.RECT_DIM, Layout.SKILLTREE_WIDTH);

        // Create render manager
        this.renderManager = new ScreenRenderManager(
                client,
                textRenderer,
                descriptionPanel,
                experienceBar,
                skillPointsWidget
        );

        this.resetButton = createResetButton();
    }

    private CustomButton createResetButton() {
        int buttonX = Layout.SKILLTREE_OFFSET_X + Layout.SKILLTREE_WIDTH -
                Layout.RESET_DIM - Layout.BUTTON_MARGIN;
        int buttonY = Layout.CONTENT_OFFSET_Y + Layout.BUTTON_MARGIN;

        ButtonTextures textures = new ButtonTextures(
                OnePieceRPG.id("textures/gui/skill/reskill.png"),
                OnePieceRPG.id("textures/gui/skill/reskill_hovered.png")
        );

        return new CustomButton(
                buttonX, buttonY,
                Layout.RESET_DIM, Layout.RESET_DIM,
                Text.literal(""),
                textures,
                this::handleResetClick
        );
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        // Update layout dimensions first
        Layout.updateDimensions(width, height);

        super.resize(client, width, height);

        if (renderManager != null) {
            renderManager.invalidateCache();
        }
        if (spellSlotManager != null) {
            spellSlotManager.invalidateLayout();
        }

        // Reinitialize widgets with new dimensions
        initializeWidgets();
    }

    // ==================== Tab Management ====================

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

    private void notifyTabsDataChanged() {
        for (TabData tabData : tabs) {
            if (tabData.tab() instanceof SkillsTab skillsTab) {
                skillsTab.markDataUpdateNeeded();
            }
        }
    }


    // ==================== Update Processing ====================

    /**
     * Main update loop - processes all pending updates
     */
    private void processUpdates() {
        var flags = updateCoordinator.getFlags();

        if (flags.devilFruitConfig) {
            var config = ClientData.DEVIL_FRUIT_CONFIG.get().orElse(null);
            setTabs(config);
            updateCoordinator.clearFlag(ScreenUpdateCoordinator.UpdateFlag.DEVIL_FRUIT_CONFIG);
        }

        if (flags.classConfig) {
            notifyTabsDataChanged();
            updateCoordinator.clearFlag(ScreenUpdateCoordinator.UpdateFlag.CLASS_CONFIG);
        }

        if (flags.categoryData) {
            dataManager.updateCategoryDataIfNeeded();
            updateCoordinator.clearFlag(ScreenUpdateCoordinator.UpdateFlag.CATEGORY_DATA);
        }

        if (flags.viewport) {
            notifyTabsDataChanged();
            updateCoordinator.clearFlag(ScreenUpdateCoordinator.UpdateFlag.VIEWPORT);
        }

        spellSlotManager.updateSpellData();


        if (flags.tabs) {
            updateCoordinator.clearFlag(ScreenUpdateCoordinator.UpdateFlag.TABS);
        }
    }

    // ==================== Data Access ====================

    public ClientCategoryData getCategoryData() {
        return dataManager.getCategoryData();
    }

    public boolean hasCategoryData() {
        return dataManager.hasCategoryData();
    }

    public ClassConfig getClassConfig() {
        return dataManager.getClassConfig();
    }

    public boolean hasClassConfig() {
        return dataManager.hasClassConfig();
    }

    public void reloadCategoryData() {
        dataManager.reloadCategoryData();
    }

    // ==================== Description Panel ====================

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
        var screenPos = renderManager.getCachedScreenPosition(width, height);

        // Priority order: overlay > reset button > spell slots > tabs
        if (handleOverlayClick(mouseX, mouseY, button)) {
            return true;
        }

        if (resetButton.handleClick(mouseX, mouseY, button, screenPos.x(), screenPos.y())) {
            return true;
        }

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
                if (button == 0) {
                    openSpellSelection(slot.getSlotIndex());
                }
                return true;
            }
        }
        return false;
    }

    private void handleResetClick() {
        if (client != null && client.mouse != null && client.getWindow() != null) {
            float mouseX = (float) client.mouse.getX() / client.getWindow().getWidth();
            float mouseY = (float) client.mouse.getY() / client.getWindow().getHeight();
            PostProcessingManager.click(client, mouseX, mouseY);
        }
        ClientPlayNetworking.send(new UiPayload("reset"));
    }

    // ==================== Spell Selection ====================

    private void openSpellSelection(int slotIndex) {
        spellSelectionOverlay = new SpellSelectionOverlay(
                slotIndex,
                spellSlotManager.getSpellManager().getLearnedSpells(),
                this::handleSpellSelected,
                this::closeSpellSelection
        );
    }

    private void handleSpellSelected(int slotIndex, RegistryEntry<Spell> spell) {
        spellSlotManager.getSpellManager().updateSpellSlot(slotIndex, spell);
        closeSpellSelection();
    }

    private void closeSpellSelection() {
        spellSelectionOverlay = null;
    }

    // ==================== Rendering ====================

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.applyBlur(delta);
        this.renderDarkening(context);

        if (renderManager != null) {
            renderManager.renderBackground(context, width, height);
            renderSpellSlots(context, mouseX, mouseY);
        }
    }

    private void renderSpellSlots(DrawContext context, int mouseX, int mouseY) {
        var screenPos = renderManager.getCachedScreenPosition(width, height);
        updateSpellSlotLayoutIfNeeded(screenPos);

        renderManager.renderSpellSlots(context, spellSlotManager.getSlots(), mouseX, mouseY, width, height);

        // Handle spell slot hover
        for (SpellSlotWidget slot : spellSlotManager.getSlots()) {
            if (isMouseOverSlot(slot, mouseX, mouseY)) {
                slot.setOnHover(info -> {
                    descriptionPanel.setContent(
                            info.title(),
                            info.description(),
                            info.extraDescription(),
                            info.advanced()
                    );
                });
                break;
            }
        }
    }

    private boolean isMouseOverSlot(SpellSlotWidget slot, int mouseX, int mouseY) {
        var pos = slot.getPosition();
        return mouseX >= pos.x && mouseX < pos.x + Layout.RECT_DIM &&
                mouseY >= pos.y && mouseY < pos.y + Layout.RECT_DIM;
    }

    private void updateSpellSlotLayoutIfNeeded(ScreenRenderManager.ScreenPosition screenPos) {
        int baseX = screenPos.x() + Layout.SKILLTREE_OFFSET_X;
        int baseY = screenPos.y() + Layout.CONTENT_OFFSET_Y2;

        spellSlotManager.updateLayoutIfNeeded(baseX, baseY, Layout.SKILLTREE_WIDTH);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        dataManager.updatePlayerData();
        processUpdates();
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 200f);
        try {
            renderManager.renderDescriptionSection(context, mouseX, mouseY, width, height);
            renderManager.renderExperienceSection(context, dataManager.getCategoryData(),
                    mouseX, mouseY, width, height);
            renderResetButton(context, mouseX, mouseY);
        } finally {
            context.getMatrices().pop();
        }

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

    private void renderResetButton(DrawContext context, int mouseX, int mouseY) {
        var screenPos = renderManager.getCachedScreenPosition(width, height);
        resetButton.render(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
        resetButton.renderTooltip(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
    }
}