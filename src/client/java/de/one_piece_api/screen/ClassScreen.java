package de.one_piece_api.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.screen.widget.ClassWidget;
import de.one_piece_api.network.payload.ClassConfigPayload;
import de.one_piece_api.network.payload.SetClassPayload;
import de.one_piece_api.init.MySounds;
import de.one_piece_api.util.ClientData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Screen for displaying and selecting character classes.
 * <p>
 * This screen presents available character classes in a visually appealing layout
 * with scaling widgets, background music, and smooth hover animations. Players can
 * view class details and select their desired class by clicking on the widget.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Dynamic widget layout that adapts to screen size</li>
 *     <li>Background music playback during class selection</li>
 *     <li>Hover effects with sound feedback</li>
 *     <li>Responsive scaling for different resolutions</li>
 * </ul>
 *
 * @see ClassWidget
 * @see ClassConfig
 */
public class ClassScreen extends Screen {

    // Reference dimensions for scaling calculations
    /** Reference width for 1080p resolution */
    private static final int REFERENCE_WIDTH = 1920;

    /** Reference height for 1080p resolution */
    private static final int REFERENCE_HEIGHT = 1080;

    /** Reference width of the main container */
    private static final int CONTAINER_REF_WIDTH = 1785;

    /** Reference height of the main container */
    private static final int CONTAINER_REF_HEIGHT = 985;

    /** Reference width of the title texture */
    private static final int TITLE_REF_WIDTH = 348;

    /** Reference height of the title texture */
    private static final int TITLE_REF_HEIGHT = 42;

    /** Top margin for the title placement */
    private static final int TITLE_TOP_MARGIN = 12;

    // Widget spacing parameters
    /** Factor controlling horizontal spacing between class widgets (0.0-1.0) */
    private static final float WIDGET_SPREAD_FACTOR = 0.6f;

    // Texture identifiers
    /** Background scene texture showing the world */
    private static final Identifier BACK_SCENE = OnePieceRPG.id("textures/gui/class/back_scene.png");

    /** Container background texture */
    private static final Identifier CLASS_BACKGROUND = OnePieceRPG.id("textures/gui/skill/background.png");

    /** "Choose your class" title text texture */
    private static final Identifier CHOOSE_TEXT = OnePieceRPG.id("textures/gui/class/choose_text.png");

    /** List of class widgets displayed on screen */
    private final List<ClassWidget> classWidgets = new ArrayList<>();

    /** Dimension calculator for responsive scaling */
    private final ScreenDimensions dimensions = new ScreenDimensions();

    /** Singleton instance of the class screen */
    private static ClassScreen instance = null;

    /**
     * Gets or creates the singleton instance of the class screen.
     *
     * @return the screen instance
     */
    public static ClassScreen getInstance() {
        if (instance == null) {
            instance = new ClassScreen();
        }
        return instance;
    }

    /**
     * Creates a new class selection screen.
     * <p>
     * Sets up a listener for class configuration updates from the server.
     */
    public ClassScreen() {
        super(ScreenTexts.EMPTY);
        ClientData.CLASS_CONFIG.addListener(this::onClassConfigChange);
    }

    /**
     * Updates the available class options and recreates widgets.
     * <p>
     * Called when class configuration is received from server. Clears existing
     * widgets and creates new ones based on the provided configuration, distributing
     * them evenly across the screen.
     *
     * @param classConfigs map of class identifiers to their configurations
     */
    private void onClassConfigChange(Map<Identifier, ClassConfig> classConfigs) {
        classWidgets.clear();

        if (classConfigs.isEmpty()) {
            return;
        }

        int classCount = classConfigs.size();
        int index = 0;

        for (Map.Entry<Identifier, ClassConfig> entry : classConfigs.entrySet()) {
            float offset = calculateWidgetOffset(index, classCount);
            ClassWidget widget = new ClassWidget(
                    entry.getKey(),
                    entry.getValue(),
                    offset,
                    this::onClassSelected
            );
            classWidgets.add(widget);
            index++;
        }

        updateWidgetLayout();
    }

    /**
     * Calculates the horizontal offset for a widget based on its position.
     * <p>
     * Distributes widgets evenly across the available space using a normalized
     * offset value. For a single widget, returns 0 (centered). For multiple
     * widgets, spreads them from -WIDGET_SPREAD_FACTOR to +WIDGET_SPREAD_FACTOR.
     *
     * @param index the index of the widget (0-based)
     * @param totalCount the total number of widgets
     * @return the normalized horizontal offset (-WIDGET_SPREAD_FACTOR to +WIDGET_SPREAD_FACTOR)
     */
    private float calculateWidgetOffset(int index, int totalCount) {
        if (totalCount == 1) {
            return 0.0f;
        }
        float normalizedPosition = (float) index / (totalCount - 1);
        return (2.0f * normalizedPosition - 1.0f) * WIDGET_SPREAD_FACTOR;
    }

    /** Currently playing background music instance */
    private PositionedSoundInstance music;

    /**
     * Initializes the screen and starts background music.
     * <p>
     * Updates dimensions, positions widgets, stops any currently playing music,
     * and starts the class selection background music. Also requests class
     * configuration data from the server.
     */
    @Override
    protected void init() {
        super.init();
        dimensions.update(width, height);
        updateWidgetLayout();

        if (client != null) {
            // Stop all currently playing music
            client.getSoundManager().stopSounds(null, SoundCategory.MUSIC);
            client.getMusicTracker().stop();
            // Create and play GUI music
            music = new PositionedSoundInstance(
                    MySounds.AMBIENT.getId(),
                    SoundCategory.MUSIC,
                    0.5f,
                    1.0f,
                    SoundInstance.createRandom(),
                    true,
                    0,
                    SoundInstance.AttenuationType.NONE,
                    0, 0, 0,
                    true
            );

            client.getSoundManager().play(music);
        }

        ClientPlayNetworking.send(new ClassConfigPayload.Request());
    }

    /**
     * Handles screen removal and cleanup.
     * <p>
     * Stops the background music and resumes normal game music when the
     * screen is closed.
     */
    @Override
    public void removed() {
        if (client != null && music != null) {
            client.getSoundManager().stop(music);
            if (client.getMusicTracker() != null) {
                client.getMusicTracker().tick();
            }
        }
        super.removed();
    }

    /**
     * Handles screen resize events.
     * <p>
     * Recalculates dimensions and updates widget layout to fit the new screen size.
     *
     * @param client the Minecraft client instance
     * @param width the new screen width
     * @param height the new screen height
     */
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        dimensions.update(width, height);
        updateWidgetLayout();
    }

    /**
     * Updates the layout of all class widgets based on current dimensions.
     * <p>
     * Recalculates scale and positions for all widgets to ensure they remain
     * properly sized and positioned.
     */
    private void updateWidgetLayout() {
        float scale = dimensions.getMinScale();
        int containerWidth = dimensions.containerWidth;

        for (ClassWidget widget : classWidgets) {
            widget.updateLayout(scale, containerWidth);
        }
    }

    /**
     * Renders the entire screen including background, widgets, and title.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        renderWidgets(context, mouseX, mouseY, delta);
        renderTitle(context);
    }

    /**
     * Renders the background layers including scene and container.
     * <p>
     * Draws the full-screen background scene and the centered container
     * texture with proper scaling.
     *
     * @param context the drawing context
     */
    private void renderBackground(DrawContext context) {
        RenderSystem.enableBlend();

        // Full background scene
        context.drawTexture(
                BACK_SCENE,
                dimensions.getSceneCenterX(),
                dimensions.getSceneCenterY(),
                0, 0,
                dimensions.sceneWidth,
                dimensions.sceneHeight,
                dimensions.sceneWidth,
                dimensions.sceneHeight
        );

        // Container background
        context.drawTexture(
                CLASS_BACKGROUND,
                dimensions.getContainerCenterX(),
                dimensions.getContainerCenterY(),
                0, 0,
                dimensions.containerWidth,
                dimensions.containerHeight,
                dimensions.containerWidth,
                dimensions.containerHeight
        );

        RenderSystem.disableBlend();
    }

    /** Currently hovered class widget, used for sound effect tracking */
    private ClassWidget hovered = null;

    /**
     * Renders all class widgets with hover effects.
     * <p>
     * Detects which widget is being hovered, plays sound effects when hover
     * changes, and renders all widgets with appropriate hover states.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
    private void renderWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        Optional<ClassWidget> hoveredWidget = findHoveredWidget(mouseX, mouseY);
        if (hovered != hoveredWidget.orElse(null)) {
            hovered = hoveredWidget.orElse(null);
            if (client != null && client.player != null && hovered != null) {
                client.player.playSound(SoundEvent.of(OnePieceRPG.id("panel_select")), 1, 1);
            }
        }

        boolean anyHovered = false;
        for (ClassWidget widget : classWidgets) {
            boolean isHovered = hoveredWidget.isPresent() && hoveredWidget.get() == widget;
            widget.setHovered(isHovered);
            if (isHovered) {
                anyHovered = true;
            }
        }
        for (ClassWidget widget : classWidgets) {
            widget.setAnyHovered(anyHovered);
            widget.render(context, mouseX, mouseY, delta);
        }

    }

    /**
     * Renders the "Choose your class" title text.
     *
     * @param context the drawing context
     */
    private void renderTitle(DrawContext context) {
        RenderSystem.enableBlend();
        context.drawTexture(
                CHOOSE_TEXT,
                dimensions.getTitleCenterX(),
                dimensions.getTitleY(),
                0, 0,
                dimensions.titleWidth,
                dimensions.titleHeight,
                dimensions.titleWidth,
                dimensions.titleHeight
        );
        RenderSystem.disableBlend();
    }

    /**
     * Finds the widget currently under the mouse cursor.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return an {@link Optional} containing the hovered widget, or empty if none
     */
    private Optional<ClassWidget> findHoveredWidget(double mouseX, double mouseY) {
        return classWidgets.stream()
                .filter(widget -> widget.isMouseOver(mouseX, mouseY))
                .findFirst();
    }

    /**
     * Handles mouse click events.
     * <p>
     * Checks if any widget was clicked and plays a click sound if so.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ClassWidget widget : classWidgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                if (client == null || client.player == null) return false;
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 3.0f, 1.0f);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Handles class selection callback.
     * <p>
     * Sends the selected class to the server and closes the screen.
     *
     * @param classId the identifier of the selected class
     */
    private void onClassSelected(Identifier classId) {
        ClientPlayNetworking.send(new SetClassPayload(classId));
        this.close();
    }

    /**
     * Determines whether the game should pause while this screen is open.
     *
     * @return {@code false} to allow the game to continue running
     */
    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     * Opens this screen in the client.
     *
     * @param client the Minecraft client instance
     */
    public void open(MinecraftClient client) {
        client.setScreen(this);
    }

    /**
     * Encapsulates screen dimension calculations and scaling logic.
     * <p>
     * This inner class handles all responsive scaling calculations for the screen,
     * ensuring UI elements maintain proper proportions across different resolutions.
     * It uses reference dimensions from 1080p as a baseline and scales elements
     * based on the current screen size.
     */
    private static class ScreenDimensions {
        /** Scaled scene width in pixels */
        int sceneWidth, sceneHeight;

        /** Scaled container width in pixels */
        int containerWidth, containerHeight;

        /** Scaled title width in pixels */
        int titleWidth, titleHeight;

        /** Current screen dimensions */
        private int screenWidth, screenHeight;

        /**
         * Updates all dimension calculations based on new screen size.
         * <p>
         * The scene uses maximum scale to ensure full coverage without gaps,
         * while container and UI elements use minimum scale to fit within the viewport.
         *
         * @param screenWidth the current screen width
         * @param screenHeight the current screen height
         */
        void update(int screenWidth, int screenHeight) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;

            float scaleX = (float) screenWidth / REFERENCE_WIDTH;
            float scaleY = (float) screenHeight / REFERENCE_HEIGHT;
            float maxScale = Math.max(scaleX, scaleY);
            float minScale = Math.min(scaleX, scaleY);

            // Scene uses max scale to ensure full coverage
            sceneWidth = (int) (maxScale * REFERENCE_WIDTH);
            sceneHeight = (int) (maxScale * REFERENCE_HEIGHT);

            // Container and UI elements use min scale to fit within viewport
            containerWidth = (int) (minScale * CONTAINER_REF_WIDTH);
            containerHeight = (int) (minScale * CONTAINER_REF_HEIGHT);
            titleWidth = (int) (minScale * TITLE_REF_WIDTH);
            titleHeight = (int) (minScale * TITLE_REF_HEIGHT);
        }

        /**
         * Gets the minimum scaling factor for UI elements.
         *
         * @return the smaller of horizontal or vertical scale factors
         */
        float getMinScale() {
            float scaleX = (float) screenWidth / REFERENCE_WIDTH;
            float scaleY = (float) screenHeight / REFERENCE_HEIGHT;
            return Math.min(scaleX, scaleY);
        }

        /**
         * Gets the x-coordinate for centering the background scene.
         *
         * @return the scene left edge x-coordinate
         */
        int getSceneCenterX() {
            return (screenWidth - sceneWidth) / 2;
        }

        /**
         * Gets the y-coordinate for centering the background scene.
         *
         * @return the scene top edge y-coordinate
         */
        int getSceneCenterY() {
            return (screenHeight - sceneHeight) / 2;
        }

        /**
         * Gets the x-coordinate for centering the container.
         *
         * @return the container left edge x-coordinate
         */
        int getContainerCenterX() {
            return (screenWidth - containerWidth) / 2;
        }

        /**
         * Gets the y-coordinate for centering the container.
         *
         * @return the container top edge y-coordinate
         */
        int getContainerCenterY() {
            return (screenHeight - containerHeight) / 2;
        }

        /**
         * Gets the x-coordinate for centering the title.
         *
         * @return the title left edge x-coordinate
         */
        int getTitleCenterX() {
            return (screenWidth - titleWidth) / 2;
        }

        /**
         * Gets the y-coordinate for the title with top margin applied.
         *
         * @return the title top edge y-coordinate
         */
        int getTitleY() {
            return getContainerCenterY() + TITLE_TOP_MARGIN;
        }
    }
}