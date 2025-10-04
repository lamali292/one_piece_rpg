package de.one_piece_api.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.gui.widgets.ClassWidget;
import de.one_piece_api.network.ClassConfigPayload;
import de.one_piece_api.network.SetClassPayload;
import de.one_piece_api.registries.MySounds;
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
 * Manages layout, rendering, and interaction of class widgets.
 */
public class ClassScreen extends Screen {

    // Reference dimensions for scaling calculations
    private static final int REFERENCE_WIDTH = 1920;
    private static final int REFERENCE_HEIGHT = 1080;
    private static final int CONTAINER_REF_WIDTH = 1785;
    private static final int CONTAINER_REF_HEIGHT = 985;
    private static final int TITLE_REF_WIDTH = 348;
    private static final int TITLE_REF_HEIGHT = 42;
    private static final int TITLE_TOP_MARGIN = 12;

    // Widget spacing parameters
    private static final float WIDGET_SPREAD_FACTOR = 0.6f;

    // Texture identifiers
    private static final Identifier BACK_SCENE = OnePieceRPG.id("textures/gui/class/back_scene.png");
    private static final Identifier CLASS_BACKGROUND = OnePieceRPG.id("textures/gui/skill/background.png");
    private static final Identifier CHOOSE_TEXT = OnePieceRPG.id("textures/gui/class/choose_text.png");

    private final List<ClassWidget> classWidgets = new ArrayList<>();
    private final ScreenDimensions dimensions = new ScreenDimensions();


    private static ClassScreen instance = null;

    public static ClassScreen getInstance() {
        if (instance == null) {
            instance = new ClassScreen();
        }
        return instance;
    }

    public ClassScreen() {
        super(ScreenTexts.EMPTY);
        ClientData.CLASS_CONFIG.addListener(this::onClassConfigChange);
    }

    /**
     * Updates the available class options and recreates widgets.
     * Called when class configuration is received from server.
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
     * Distributes widgets evenly across the available space.
     */
    private float calculateWidgetOffset(int index, int totalCount) {
        if (totalCount == 1) {
            return 0.0f;
        }
        float normalizedPosition = (float) index / (totalCount - 1);
        return (2.0f * normalizedPosition - 1.0f) * WIDGET_SPREAD_FACTOR;
    }

    private PositionedSoundInstance music;

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

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        dimensions.update(width, height);
        updateWidgetLayout();
    }

    private void updateWidgetLayout() {
        float scale = dimensions.getMinScale();
        int containerWidth = dimensions.containerWidth;

        for (ClassWidget widget : classWidgets) {
            widget.updateLayout(scale, containerWidth);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        renderWidgets(context, mouseX, mouseY, delta);
        renderTitle(context);
    }

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

    private ClassWidget hovered = null;

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

    private Optional<ClassWidget> findHoveredWidget(double mouseX, double mouseY) {
        return classWidgets.stream()
                .filter(widget -> widget.isMouseOver(mouseX, mouseY))
                .findFirst();
    }

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

    private void onClassSelected(Identifier classId) {
        ClientPlayNetworking.send(new SetClassPayload(classId));
        this.close();
    }



    @Override
    public boolean shouldPause() {
        return false;
    }

    public void open(MinecraftClient client) {
        client.setScreen(this);
    }

    /**
     * Encapsulates tab dimension calculations and scaling logic.
     * Reduces clutter in main class and makes scaling logic testable.
     */
    private static class ScreenDimensions {
        int sceneWidth, sceneHeight;
        int containerWidth, containerHeight;
        int titleWidth, titleHeight;

        private int screenWidth, screenHeight;

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

        float getMinScale() {
            float scaleX = (float) screenWidth / REFERENCE_WIDTH;
            float scaleY = (float) screenHeight / REFERENCE_HEIGHT;
            return Math.min(scaleX, scaleY);
        }

        int getSceneCenterX() {
            return (screenWidth - sceneWidth) / 2;
        }

        int getSceneCenterY() {
            return (screenHeight - sceneHeight) / 2;
        }

        int getContainerCenterX() {
            return (screenWidth - containerWidth) / 2;
        }

        int getContainerCenterY() {
            return (screenHeight - containerHeight) / 2;
        }

        int getTitleCenterX() {
            return (screenWidth - titleWidth) / 2;
        }

        int getTitleY() {
            return getContainerCenterY() + TITLE_TOP_MARGIN;
        }
    }
}