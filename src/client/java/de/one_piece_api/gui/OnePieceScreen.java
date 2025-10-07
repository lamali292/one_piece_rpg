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
 * <p>
 * This screen provides a comprehensive tabbed interface featuring:
 * <ul>
 *     <li>Devil Fruit skill trees with visual progression</li>
 *     <li>Class-based skill trees</li>
 *     <li>Haki skill trees</li>
 *     <li>Spell slot management with drag-and-drop</li>
 *     <li>Experience tracking and skill points display</li>
 *     <li>Interactive description panels</li>
 *     <li>Reset functionality with visual effects</li>
 * </ul>
 *
 * <h2>Architecture:</h2>
 * <ul>
 *     <li>Widget-based UI components for better separation of concerns</li>
 *     <li>Immutable layout constants for consistency</li>
 *     <li>Lazy layout updates to minimize redraws</li>
 *     <li>Proper resource cleanup and null safety</li>
 * </ul>
 *
 * @see GenericTabbedScreen
 * @see SpellSlotManager
 * @see DescriptionPanelWidget
 */
public class OnePieceScreen extends GenericTabbedScreen {

    // ==================== Singleton ====================

    /** Singleton instance of the screen */
    private static OnePieceScreen instance = null;

    /**
     * Gets or creates the singleton instance of the One Piece screen.
     *
     * @return the screen instance
     */
    public static OnePieceScreen getInstance() {
        if (instance == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            instance = new OnePieceScreen(client.player);
        }
        return instance;
    }

    /**
     * Opens this screen in the client and reloads category data.
     *
     * @param client the Minecraft client instance
     */
    public void open(MinecraftClient client) {
        client.setScreen(this);
        reloadCategoryData();
    }

    // ==================== Constants ====================

    /**
     * Texture resource identifiers used throughout the screen.
     */
    private static final class Textures {
        /** Main background texture */
        static final Identifier SKILL_BACKGROUND = OnePieceRPG.id("textures/gui/skill/background.png");

        /** Wide rectangular frame for boutique section */
        static final Identifier SKILL_WIDE_RECT = OnePieceRPG.id("textures/gui/skill/boutique.png");

        /** Skill tree frame texture */
        static final Identifier SKILLTREE_FRAME = OnePieceRPG.id("textures/gui/skill/skilltree.png");

        /** Description panel frame texture */
        static final Identifier DESCRIPTION_FRAME = OnePieceRPG.id("textures/gui/skill/description.png");

        /** Reset button normal state texture */
        static final Identifier RESET_BUTTON = OnePieceRPG.id("textures/gui/skill/reskill.png");

        /** Reset button hovered state texture */
        static final Identifier RESET_BUTTON_HOVERED = OnePieceRPG.id("textures/gui/skill/reskill_hovered.png");
    }

    /**
     * Layout configuration and dimension calculations.
     * <p>
     * All dimensions are scaled by a constant factor to maintain aspect ratio
     * across different screen sizes. Public fields are provided for backward
     * compatibility with existing code.
     */
    public static final class Layout {
        /** Global scaling factor for all UI elements */
        static final float SCALE = 0.23F;

        /** Margin between buttons and adjacent elements */
        static final int BUTTON_MARGIN = 3;

        /** Scaled background width in pixels */
        public static final int BACKGROUND_WIDTH = scale(1785);

        /** Scaled background height in pixels */
        public static final int BACKGROUND_HEIGHT = scale(985);

        /** Scaled skill tree content width in pixels */
        public static final int SKILLTREE_WIDTH = scale(1202);

        /** Scaled main content area height in pixels */
        public static final int CONTENT_HEIGHT = scale(688);

        /** Scaled description panel width in pixels */
        public static final int DESCRIPTION_WIDTH = scale(456);

        /** Scaled boutique section height in pixels */
        public static final int BOUTIQUE_HEIGHT = scale(83);

        /** Scaled boutique section width in pixels */
        public static final int BOUTIQUE_WIDTH = scale(363);

        /** Scaled rectangular element dimension in pixels */
        public static final int RECT_DIM = scale(100);

        /** Scaled reset button dimension in pixels */
        public static final int RESET_DIM = scale(68);

        /** Vertical offset for main content area */
        public static final int CONTENT_OFFSET_Y = scale((985 - 688) / 2);

        /** Horizontal offset for skill tree section */
        public static final int SKILLTREE_OFFSET_X = (BACKGROUND_WIDTH - SKILLTREE_WIDTH - DESCRIPTION_WIDTH) / 3;

        /** Horizontal offset for description panel */
        public static final int DESCRIPTION_OFFSET_X = 2 * SKILLTREE_OFFSET_X + SKILLTREE_WIDTH;

        /** Top margin for spacing */
        public static final int TOP_MARGIN = (BACKGROUND_HEIGHT - CONTENT_HEIGHT - CONTENT_OFFSET_Y - RECT_DIM) / 2;

        /** Secondary vertical offset for lower content */
        public static final int CONTENT_OFFSET_Y2 = CONTENT_OFFSET_Y + CONTENT_HEIGHT + TOP_MARGIN;

        /**
         * Scales an integer value by the global scale factor.
         *
         * @param value the value to scale
         * @return the scaled value
         */
        private static int scale(int value) {
            return (int) (value * SCALE);
        }

        /**
         * Scales a float value by the global scale factor.
         *
         * @param value the value to scale
         * @return the scaled value
         */
        private static int scale(float value) {
            return (int) (value * SCALE);
        }
    }

    /**
     * UI color scheme constants in ARGB format.
     */
    private static final class Colors {
        /** Primary text color (white) */
        static final int PRIMARY_TEXT = 0xFFFFFFFF;

        /** Divider line color (beige) */
        static final int DIVIDER = 0xFFE2E0B4;
    }

    // ==================== State ====================

    /** Skill screen data from the skills mod */
    private final ClientSkillScreenData skillScreenData;

    /** Current category data for the One Piece category */
    private ClientCategoryData categoryData;

    // UI Components
    /** Button for resetting skill points */
    private CustomButton resetButton;

    /** Widget displaying skill descriptions */
    private DescriptionPanelWidget descriptionPanel;

    /** Widget showing experience bar */
    private ExperienceBarWidget experienceBar;

    /** Widget displaying available skill points */
    private SkillPointsWidget skillPointsWidget;

    /** Manager for spell slot widgets and spell selection */
    private SpellSlotManager spellSlotManager;

    /** Overlay for selecting spells from learned spells */
    private SpellSelectionOverlay spellSelectionOverlay;

    // Layout caching
    /** Cached screen position to avoid unnecessary layout updates */
    private ScreenPosition cachedScreenPosition;

    // ==================== Construction ====================

    /**
     * Creates a new One Piece screen instance.
     * <p>
     * This constructor initializes the screen with player data, loads skill screen
     * and category data, and sets up listeners for devil fruit configuration updates.
     *
     * @param player the client player entity
     */
    public OnePieceScreen(ClientPlayerEntity player) {
        super(Text.literal("Skill Tree"), player);
        ClientData.DEVIL_FRUIT_CONFIG.addListener(this::onDevilFruitUpdate);
        this.skillScreenData = loadSkillScreenData();
        this.categoryData = loadCategoryData();

        // Set singleton
        instance = this;
    }

    /**
     * Initializes all UI widgets that require a text renderer.
     * <p>
     * This method is called from {@link #init()} when the text renderer becomes
     * available. It ensures widgets are only created once.
     */
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

    /**
     * Creates the description panel widget.
     *
     * @return a new description panel widget
     * @throws IllegalStateException if text renderer is not initialized
     */
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

    /**
     * Creates the experience bar widget.
     *
     * @return a new experience bar widget
     * @throws IllegalStateException if text renderer is not initialized
     */
    private ExperienceBarWidget createExperienceBar() {
        if (textRenderer == null) {
            throw new IllegalStateException("Cannot create ExperienceBarWidget: textRenderer not initialized");
        }
        ExperienceBarWidget widget = new ExperienceBarWidget(textRenderer, Layout.SCALE);
        widget.setTooltipSetter(this::setTooltip);
        return widget;
    }

    /**
     * Creates the skill points widget.
     *
     * @return a new skill points widget
     * @throws IllegalStateException if text renderer is not initialized
     */
    private SkillPointsWidget createSkillPointsWidget() {
        if (textRenderer == null) {
            throw new IllegalStateException("Cannot create SkillPointsWidget: textRenderer not initialized");
        }
        return new SkillPointsWidget(textRenderer);
    }

    /**
     * Creates the spell slot manager.
     *
     * @return a new spell slot manager
     */
    private SpellSlotManager createSpellSlotManager() {
        return new SpellSlotManager(player, Layout.RECT_DIM, Layout.SKILLTREE_WIDTH);
    }

    /**
     * Creates the reset button.
     *
     * @return a new reset button
     */
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

    /** Cached devil fruit identifier to detect changes */
    private Identifier fruitId = null;

    /**
     * Handles devil fruit configuration updates.
     * <p>
     * This listener is called when the devil fruit configuration changes,
     * triggering a refresh of the tab layout.
     *
     * @param newEntry the new devil fruit configuration
     */
    private void onDevilFruitUpdate(DevilFruitConfig newEntry) {
        setTabs(newEntry);
    }

    /**
     * Sets up the tab layout based on the devil fruit configuration.
     * <p>
     * Creates tabs for devil fruit (if available), class skills, and haki.
     * The devil fruit tab is only shown if the player has a devil fruit with
     * configured paths.
     *
     * @param devilFruitConfig the devil fruit configuration, or {@code null}
     */
    public void setTabs(DevilFruitConfig devilFruitConfig) {
        int tabWidth = Layout.RECT_DIM * 4;
        tabs.clear();
        if (devilFruitConfig != null && !devilFruitConfig.paths().isEmpty()) {
            addTab(getTranslatable("devilfruit"), new DevilFruitTab(this, devilFruitConfig), tabWidth);
        }
        addTab(getTranslatable("class"), new SkillsTab(this), tabWidth);
        addTab(getTranslatable("haki"), new HakiTab(this), tabWidth);
    }

    /**
     * Gets a translatable text for a tab key.
     *
     * @param key the tab key suffix
     * @return the translatable text
     */
    private static MutableText getTranslatable(String key) {
        return Text.translatable("gui." + OnePieceRPG.MOD_ID + ".tab." + key);
    }

    // ==================== Data Management ====================

    /**
     * Gets the current category data.
     *
     * @return the category data, or {@code null} if not loaded
     */
    public ClientCategoryData getCategoryData() {
        return categoryData;
    }

    /**
     * Checks if category data is available.
     *
     * @return {@code true} if category data is loaded, {@code false} otherwise
     */
    public boolean hasCategoryData() {
        return categoryData != null;
    }

    /**
     * Updates category data and checks for devil fruit changes.
     * <p>
     * This method is called each frame to keep data synchronized with the server.
     * If the player's devil fruit changes, it requests the new configuration.
     */
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

    /**
     * Reloads category data while preserving view state.
     * <p>
     * This method captures the current view position and scale, reloads the
     * category data from the server, and then restores the view state to
     * maintain continuity for the user.
     */
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

    /**
     * Refreshes the current tab if it's a skills tab.
     */
    private void refreshCurrentTab() {
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof SkillsTab skillsTab) {
            skillsTab.refreshCategoryData();
        }
    }

    /**
     * Updates learned spell data in the spell slot manager.
     */
    public void updateLearned() {
        if (spellSlotManager != null) {
            spellSlotManager.updateSpellData();
        }
    }

    // ==================== Description Panel Control ====================

    /**
     * Sets the hovered skill information in the description panel.
     *
     * @param title the skill title
     * @param description the skill description
     * @param extraDescription additional description text
     * @param skillId the skill identifier for advanced tooltips
     */
    public void setHoveredSkillInfo(Text title, Text description, Text extraDescription, Text skillId) {
        if (descriptionPanel != null) {
            descriptionPanel.setContent(title, description, extraDescription, skillId);
        }
    }

    /**
     * Clears the hovered skill information from the description panel.
     */
    public void clearHoveredSkillInfo() {
        if (descriptionPanel != null) {
            descriptionPanel.clearContent();
        }
    }

    // ==================== Lifecycle ====================

    /**
     * Initializes the screen components.
     * <p>
     * This method is called by Minecraft when the screen is shown. It initializes
     * widgets now that the text renderer is available, sets up tabs if needed,
     * and invalidates the layout to trigger initial positioning.
     */
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

    /**
     * Handles screen resize events.
     * <p>
     * Invalidates the layout cache to force recalculation of widget positions
     * at the new screen dimensions.
     *
     * @param client the Minecraft client instance
     * @param width the new screen width
     * @param height the new screen height
     */
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        invalidateLayout();
    }

    /**
     * Handles screen closure.
     * <p>
     * Note: The singleton instance is intentionally not cleared to maintain
     * state across screen reopenings.
     */
    @Override
    public void close() {
        super.close();
        //instance = null;
    }

    /**
     * Invalidates the layout cache, forcing a recalculation on next render.
     */
    private void invalidateLayout() {
        cachedScreenPosition = null;
        if (spellSlotManager != null) {
            spellSlotManager.invalidateLayout();
        }
    }

    // ==================== Input Handling ====================

    /**
     * Handles keyboard input.
     * <p>
     * Closes the screen when the skill tree keybinding is pressed.
     *
     * @param keyCode the key code
     * @param scanCode the scan code
     * @param modifiers the modifier keys
     * @return {@code true} if the event was handled, {@code false} otherwise
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (client != null && MyKeys.getKeyBinding(MyKeys.OPEN_SKILL_TREE_KEY).matchesKey(keyCode, scanCode)) {
            client.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Handles mouse click events.
     * <p>
     * Processes clicks in the following priority order:
     * <ol>
     *     <li>Spell selection overlay (if open)</li>
     *     <li>Reset button</li>
     *     <li>Spell slots</li>
     *     <li>Parent class handling</li>
     * </ol>
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
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

    /**
     * Handles clicks on the spell selection overlay.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button
     * @return {@code true} if the overlay handled the click, {@code false} otherwise
     */
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

    /**
     * Handles clicks on spell slots.
     * <p>
     * Left-clicking a slot opens the spell selection overlay for that slot.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button
     * @return {@code true} if a slot was clicked, {@code false} otherwise
     */
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

    /**
     * Handles reset button clicks.
     * <p>
     * Triggers a visual post-processing effect at the mouse position and
     * sends a reset request to the server.
     */
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

    /**
     * Opens the spell selection overlay for a specific slot.
     *
     * @param slotIndex the index of the slot to configure
     */
    private void openSpellSelection(int slotIndex) {
        SpellManager spellManager = spellSlotManager.getSpellManager();

        spellSelectionOverlay = new SpellSelectionOverlay(
                slotIndex,
                spellManager.getLearnedSpells(),
                this::handleSpellSelected,
                this::closeSpellSelection
        );
    }

    /**
     * Handles spell selection from the overlay.
     * <p>
     * Updates the spell slot with the selected spell and refreshes the display.
     *
     * @param slotIndex the slot index that was configured
     * @param spell the selected spell registry entry
     */
    private void handleSpellSelected(int slotIndex, RegistryEntry<Spell> spell) {
        spellSlotManager.getSpellManager().updateSpellSlot(slotIndex, spell);
        spellSlotManager.updateSpellData();
        closeSpellSelection();
    }

    /**
     * Closes the spell selection overlay.
     */
    private void closeSpellSelection() {
        spellSelectionOverlay = null;
    }

    // ==================== Rendering ====================

    /**
     * Renders the screen background and UI frames.
     * <p>
     * This includes the main background texture, skill tree frame, description
     * frame, boutique section, divider lines, and spell slots.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
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

    /**
     * Renders the main background texture.
     *
     * @param context the drawing context
     * @param screenPos the screen position offset
     */
    private void renderMainBackground(DrawContext context, ScreenPosition screenPos) {
        context.drawTexture(
                Textures.SKILL_BACKGROUND,
                screenPos.x(), screenPos.y(),
                0, 0,
                Layout.BACKGROUND_WIDTH, Layout.BACKGROUND_HEIGHT,
                Layout.BACKGROUND_WIDTH, Layout.BACKGROUND_HEIGHT
        );
    }

    /**
     * Renders all UI frames (skill tree, description panel, boutique).
     *
     * @param context the drawing context
     * @param screenPos the screen position offset
     */
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

    /**
     * Renders the horizontal divider line.
     *
     * @param context the drawing context
     * @param screenPos the screen position offset
     */
    private void renderDividerLine(DrawContext context, ScreenPosition screenPos) {
        int lineY = screenPos.y() + Layout.CONTENT_OFFSET_Y2 + Layout.RECT_DIM / 2;
        int lineStartX = screenPos.x() + Layout.SKILLTREE_OFFSET_X + Layout.RECT_DIM / 2;
        int lineEndX = screenPos.x() + Layout.SKILLTREE_OFFSET_X + Layout.SKILLTREE_WIDTH - Layout.RECT_DIM / 2;

        context.drawHorizontalLine(lineStartX, lineEndX, lineY, Colors.DIVIDER);
    }

    /**
     * Renders all spell slot widgets and handles hover interactions.
     *
     * @param context the drawing context
     * @param screenPos the screen position offset
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     */
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

    /**
     * Checks if the mouse is over a spell slot.
     *
     * @param slot the slot widget to check
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if the mouse is over the slot, {@code false} otherwise
     */
    private boolean isMouseOverSlot(SpellSlotWidget slot, int mouseX, int mouseY) {
        var pos = slot.getPosition();
        return mouseX >= pos.x && mouseX < pos.x + Layout.RECT_DIM &&
                mouseY >= pos.y && mouseY < pos.y + Layout.RECT_DIM;
    }

    /**
     * Sets up the hover handler for a spell slot.
     * <p>
     * When a slot is hovered, its information is displayed in the description panel.
     *
     * @param slot the slot widget to set up
     */
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

    /**
     * Updates spell slot layout if the screen position has changed.
     * <p>
     * This method uses caching to avoid unnecessary layout recalculations.
     * Layout is only updated when the screen position changes.
     *
     * @param screenPos the current screen position
     */
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

    /**
     * Renders the entire screen including background, widgets, and overlays.
     * <p>
     * Rendering order:
     * <ol>
     *     <li>Background and frames (via {@link #renderBackground})</li>
     *     <li>Parent class content (tabs, etc.)</li>
     *     <li>Description section with title and panel</li>
     *     <li>Experience bar and skill points</li>
     *     <li>Reset button</li>
     *     <li>Spell selection overlay (if open)</li>
     * </ol>
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
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

    /**
     * Renders the description section including title and panel.
     * <p>
     * The description panel shows information about the currently hovered
     * skill or spell. Advanced tooltips are shown when F3+H is enabled.
     *
     * @param context the drawing context
     * @param screenPos the screen position offset
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     */
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

    /**
     * Renders the experience section including experience bar and skill points.
     * <p>
     * Updates widget positions and data before rendering to ensure they
     * reflect the current state.
     *
     * @param context the drawing context
     * @param screenPos the screen position offset
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     */
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

    /**
     * Renders the reset button and its tooltip.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param screenPos the screen position offset
     */
    private void renderResetButton(DrawContext context, int mouseX, int mouseY, ScreenPosition screenPos) {
        resetButton.render(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
        resetButton.renderTooltip(context, textRenderer, mouseX, mouseY, screenPos.x(), screenPos.y());
    }

    // ==================== Helper Methods ====================

    /**
     * Calculates the centered screen position for the UI.
     *
     * @return the screen position with x and y offsets
     */
    private ScreenPosition getScreenPosition() {
        int x = (width - Layout.BACKGROUND_WIDTH) / 2;
        int y = (height - Layout.BACKGROUND_HEIGHT) / 2;
        return new ScreenPosition(x, y);
    }

    /**
     * Loads category data for the One Piece category.
     *
     * @return the category data, or {@code null} if not found
     */
    private ClientCategoryData loadCategoryData() {
        return skillScreenData.getCategory(OnePieceCategory.ID).orElse(null);
    }

    /**
     * Loads skill screen data from the Skills mod using reflection.
     * <p>
     * This method accesses the private {@code screenData} field in
     * {@link SkillsClientMod} to retrieve skill tree information. If access
     * fails, an empty instance is returned and an error is logged.
     *
     * @return the skill screen data, or an empty instance on failure
     */
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

    /**
     * Immutable record representing a screen position offset.
     *
     * @param x the horizontal offset in pixels
     * @param y the vertical offset in pixels
     */
    private record ScreenPosition(int x, int y) {
        /**
         * Compares this position with another object for equality.
         *
         * @param obj the object to compare
         * @return {@code true} if positions are equal, {@code false} otherwise
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ScreenPosition other)) return false;
            return x == other.x && y == other.y;
        }

        /**
         * Computes the hash code for this position.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Immutable record capturing the view state of a category.
     * <p>
     * Used to preserve pan and zoom state when reloading category data.
     *
     * @param x the horizontal pan position
     * @param y the vertical pan position
     * @param scale the zoom scale factor
     */
    private record ViewState(int x, int y, float scale) {
        /**
         * Captures the current view state from category data.
         *
         * @param categoryData the category data to capture from
         * @return a new view state with the current position and scale
         */
        static ViewState capture(ClientCategoryData categoryData) {
            return new ViewState(
                    categoryData.getX(),
                    categoryData.getY(),
                    categoryData.getScale()
            );
        }

        /**
         * Applies this view state to category data.
         * <p>
         * Restores the pan position and zoom scale to the category.
         *
         * @param categoryData the category data to apply to
         */
        void applyTo(ClientCategoryData categoryData) {
            categoryData.setX(x);
            categoryData.setY(y);
            categoryData.setScale(scale);
        }
    }
}