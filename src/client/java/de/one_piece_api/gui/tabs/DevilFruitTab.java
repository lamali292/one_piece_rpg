// DevilFruitTab.java
package de.one_piece_api.gui.tabs;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.gui.widgets.SkillPathWidget;
import de.one_piece_api.network.DevilFruitPayload;
import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.gui.util.Tab;
import de.one_piece_api.util.DataGenUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer;

import java.util.*;

public class DevilFruitTab implements Tab {
    private static final int PATH_COUNT = 5;

    private final OnePieceScreen parent;
    private final List<SkillPathWidget> pathWidgets = new ArrayList<>();
    private final DevilFruitConfig devilFruitConfig;

    // Viewport bounds - will be calculated relative to screen position
    private int viewportX;
    private int viewportY;
    private int viewportWidth;
    private int viewportHeight;

    // Scroll/pan state
    private int scrollOffset = 0; // Vertical scroll offset
    private int maxScrollUp = 0; // Maximum scroll upward (positive)
    private int maxScrollDown = 0; // Maximum scroll downward (negative)
    private static final int SCROLL_SPEED = 20; // Pixels per scroll tick
    private static final int VERTICAL_SPACING = 36; // Frame size + spacing (must match widget constant)

    // Dragging state
    private boolean isDragging = false;
    private double dragStartY;
    private int dragStartOffset;

    public DevilFruitTab(OnePieceScreen parent, DevilFruitConfig devilFruitConfig) {
        this.parent = parent;
        this.devilFruitConfig = devilFruitConfig;
        updatePaths();
    }

    private void updatePaths() {
        pathWidgets.clear();
        if (!parent.hasCategoryData() || devilFruitConfig == null) {
            return;
        }
        ClientCategoryConfig categoryConfig = parent.getCategoryData().getConfig();
        Map<String, ClientSkillConfig> skillConfig = categoryConfig.skills();

        for (int pathIndex = 0; pathIndex < devilFruitConfig.paths().size(); pathIndex++) {
            var path = devilFruitConfig.paths().get(pathIndex);
            List<String> pathSkillIds = path.skills().stream()
                    .map(DataGenUtil::generateDeterministicId)
                    .toList();

            pathWidgets.add(new SkillPathWidget(parent, pathIndex, pathSkillIds, categoryConfig, skillConfig));
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        // Calculate screen position (same as OnePieceScreen.getScreenPosition())
        int screenX = (width - OnePieceScreen.Layout.BACKGROUND_WIDTH) / 2;
        int screenY = (height - OnePieceScreen.Layout.BACKGROUND_HEIGHT) / 2;

        // Calculate viewport bounds relative to screen position
        this.viewportX = screenX + OnePieceScreen.Layout.SKILLTREE_OFFSET_X;
        this.viewportY = screenY + OnePieceScreen.Layout.CONTENT_OFFSET_Y;
        this.viewportWidth = OnePieceScreen.Layout.SKILLTREE_WIDTH;
        this.viewportHeight = OnePieceScreen.Layout.CONTENT_HEIGHT;

        // Calculate scroll limits based on path length
        calculateScrollLimits();

        // Calculate center within the viewport (apply scroll offset)
        int centerX = viewportX + viewportWidth / 2;
        int centerY = viewportY + viewportHeight / 2 + scrollOffset;

        int frameHalfSize = 13;
        int horizontalSpacing = (frameHalfSize * 2) + 15;

        for (int i = 0; i < pathWidgets.size(); i++) {
            int pathX = centerX + i * horizontalSpacing - (pathWidgets.size() - 1) * horizontalSpacing / 2;
            pathWidgets.get(i).setPosition(pathX, centerY);
        }
    }

    private boolean isMouseOutsideViewport(double mouseX, double mouseY) {
        return (mouseX < viewportX) || (mouseX >= viewportX + viewportWidth) ||
                (mouseY < viewportY) || (mouseY >= viewportY + viewportHeight);
    }

    /**
     * Calculate scroll limits so that:
     * - Max scroll up: last skill is centered in viewport
     * - Max scroll down: first skill is centered in viewport
     */
    private void calculateScrollLimits() {
        if (pathWidgets.isEmpty() || devilFruitConfig == null) {
            maxScrollUp = 0;
            maxScrollDown = 0;
            return;
        }

        // Find the longest path
        int maxPathLength = devilFruitConfig.paths().stream()
                .mapToInt(path -> path.skills().size())
                .max()
                .orElse(0);

        if (maxPathLength <= 1) {
            maxScrollUp = 0;
            maxScrollDown = 0;
            return;
        }

        // Calculate distances from center to first/last skills
        // For max scroll down: we want the first skill (index 0) at center
        maxScrollDown = -(maxPathLength - 1) * VERTICAL_SPACING;

        // For max scroll up: we want the last skill at center
        maxScrollUp = (maxPathLength - 1) * VERTICAL_SPACING;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (pathWidgets.isEmpty()) {
            updatePaths();
        }

        MinecraftClient client = MinecraftClient.getInstance();
        resize(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());

        // Enable scissor to clip rendering to viewport bounds
        context.enableScissor(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight);

        RenderSystem.enableBlend();

        // Render connections first (behind skills)
        if (parent.hasCategoryData() && devilFruitConfig != null) {
            ConnectionBatchedRenderer connectionRenderer = new ConnectionBatchedRenderer();
            ClientCategoryConfig categoryConfig = parent.getCategoryData().getConfig();

            for (SkillPathWidget widget : pathWidgets) {
                widget.renderConnections(context, connectionRenderer, categoryConfig);
            }

            connectionRenderer.draw();
        }

        // Render skills
        for (SkillPathWidget widget : pathWidgets) {
            widget.updateSkillData();
            widget.render(context, mouseX, mouseY, delta);
        }

        RenderSystem.disableBlend();

        // Disable scissor after rendering
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if mouse is in viewport first
        if (isMouseOutsideViewport(mouseX, mouseY)) {
            return false;
        }

        // Right click or middle click to start dragging
        if (button == 1 || button == 2) {
            isDragging = true;
            dragStartY = mouseY;
            dragStartOffset = scrollOffset;
            return true;
        }

        // Left click for skill interaction
        for (SkillPathWidget widget : pathWidgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                for (SkillPathWidget w : pathWidgets) {
                    w.updateSkillData();
                }
                parent.updateLearned();
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1 || button == 2) {
            isDragging = false;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            double dragDistance = mouseY - dragStartY;
            scrollOffset = (int) Math.clamp(dragStartOffset + dragDistance, maxScrollDown, maxScrollUp);

            // Update widget positions
            MinecraftClient client = MinecraftClient.getInstance();
            resize(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Only handle scroll if mouse is in viewport
        if (isMouseOutsideViewport(mouseX, mouseY)) {
            return false;
        }

        // Scroll up = positive vertical, scroll down = negative vertical
        // We want scroll up to move content down (increase offset), scroll down to move content up (decrease offset)
        scrollOffset = (int) Math.clamp(
                scrollOffset + (verticalAmount * SCROLL_SPEED),
                maxScrollDown,
                maxScrollUp
        );

        // Update widget positions
        MinecraftClient client = MinecraftClient.getInstance();
        resize(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());

        return true;
    }
}