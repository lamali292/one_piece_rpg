// DevilFruitTab.java
package de.one_piece_api.screen.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.screen.widget.SkillPathWidget;
import de.one_piece_api.screen.OnePieceScreen;
import de.one_piece_api.screen.component.Tab;
import de.one_piece_api.util.DataGenUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer;

import java.util.*;

/**
 * Tab displaying devil fruit skill paths in a scrollable vertical layout.
 * <p>
 * This tab presents multiple parallel skill paths that can be scrolled vertically,
 * allowing players to view and interact with their devil fruit abilities. It supports
 * both mouse wheel scrolling and click-and-drag navigation.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Vertical scrolling with configurable limits</li>
 *     <li>Click-and-drag panning (right or middle mouse button)</li>
 *     <li>Viewport clipping to prevent overflow rendering</li>
 *     <li>Dynamic path widget positioning</li>
 *     <li>Connection rendering between related skills</li>
 * </ul>
 *
 * @see SkillPathWidget
 * @see DevilFruitConfig
 * @see Tab
 */
public class DevilFruitTab implements Tab {

    /** Number of parallel skill paths displayed */
    private static final int PATH_COUNT = 5;

    /** Parent screen containing this tab */
    private final OnePieceScreen parent;

    /** List of skill path widgets to render */
    private final List<SkillPathWidget> pathWidgets = new ArrayList<>();

    /** Configuration data for devil fruit paths */
    private final DevilFruitConfig devilFruitConfig;

    // Viewport bounds - will be calculated relative to screen position
    /** Left edge x-coordinate of the visible viewport */
    private int viewportX;

    /** Top edge y-coordinate of the visible viewport */
    private int viewportY;

    /** Width of the visible viewport in pixels */
    private int viewportWidth;

    /** Height of the visible viewport in pixels */
    private int viewportHeight;

    // Scroll/pan state
    /** Current vertical scroll offset in pixels */
    private int scrollOffset = 0;

    /** Maximum scroll distance upward (positive value) */
    private int maxScrollUp = 0;

    /** Maximum scroll distance downward (negative value) */
    private int maxScrollDown = 0;

    /** Number of pixels to scroll per mouse wheel tick */
    private static final int SCROLL_SPEED = 20;

    /** Vertical spacing between skill nodes in pixels */
    private static final int VERTICAL_SPACING = 36; // Frame size + spacing (must match widget constant)

    // Dragging state
    /** Whether the user is currently dragging to scroll */
    private boolean isDragging = false;

    /** Y-coordinate where the drag started */
    private double dragStartY;

    /** Scroll offset when the drag started */
    private int dragStartOffset;

    /**
     * Creates a new devil fruit tab.
     *
     * @param parent the parent OnePiece screen
     * @param devilFruitConfig the devil fruit configuration containing skill paths
     */
    public DevilFruitTab(OnePieceScreen parent, DevilFruitConfig devilFruitConfig) {
        this.parent = parent;
        this.devilFruitConfig = devilFruitConfig;
        updatePaths();
    }

    /**
     * Updates the skill path widgets based on current configuration.
     * <p>
     * Creates a new widget for each path in the devil fruit configuration,
     * generating skill IDs and associating them with the category configuration.
     */
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

    /**
     * Handles screen resize and recalculates widget positions.
     * <p>
     * Updates viewport bounds, scroll limits, and positions all path widgets
     * in a horizontal line centered within the viewport, accounting for the
     * current scroll offset.
     *
     * @param client the Minecraft client instance
     * @param width the new screen width
     * @param height the new screen height
     */
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

    /**
     * Checks if the mouse is outside the visible viewport bounds.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if the mouse is outside the viewport, {@code false} otherwise
     */
    private boolean isMouseOutsideViewport(double mouseX, double mouseY) {
        return (mouseX < viewportX) || (mouseX >= viewportX + viewportWidth) ||
                (mouseY < viewportY) || (mouseY >= viewportY + viewportHeight);
    }

    /**
     * Calculates scroll limits based on the longest skill path.
     * <p>
     * Determines the maximum scroll distances that allow:
     * <ul>
     *     <li>Max scroll up: Last skill node centered in viewport</li>
     *     <li>Max scroll down: First skill node centered in viewport</li>
     * </ul>
     * This ensures the entire skill tree remains accessible while preventing
     * excessive scrolling into empty space.
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

    /**
     * Renders the devil fruit tab including connections and skill nodes.
     * <p>
     * Rendering is clipped to the viewport bounds using scissor testing to
     * prevent overflow. Connections are drawn first (behind nodes), followed
     * by the skill nodes themselves.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
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

    /**
     * Handles mouse click events.
     * <p>
     * Processes clicks in the following order:
     * <ol>
     *     <li>Checks if click is within viewport bounds</li>
     *     <li>Right or middle click: Starts drag scrolling</li>
     *     <li>Left click: Passes to skill widgets for interaction</li>
     * </ol>
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
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

    /**
     * Handles mouse button release events.
     * <p>
     * Stops drag scrolling when the right or middle mouse button is released.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the release was handled, {@code false} otherwise
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1 || button == 2) {
            isDragging = false;
            return true;
        }
        return false;
    }

    /**
     * Handles mouse drag events for scroll panning.
     * <p>
     * When dragging is active, calculates the new scroll offset based on the
     * drag distance and updates widget positions accordingly. The offset is
     * clamped to the calculated scroll limits.
     *
     * @param mouseX the current mouse x-coordinate
     * @param mouseY the current mouse y-coordinate
     * @param button the mouse button being dragged
     * @param deltaX the horizontal mouse movement since last frame
     * @param deltaY the vertical mouse movement since last frame
     * @return {@code true} if the drag was handled, {@code false} otherwise
     */
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

    /**
     * Handles mouse scroll wheel events.
     * <p>
     * Scrolls the skill tree vertically when the mouse is within the viewport.
     * Positive vertical scroll (wheel up) moves content down, negative scroll
     * (wheel down) moves content up. Scroll offset is clamped to the calculated limits.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param horizontalAmount the horizontal scroll amount (unused)
     * @param verticalAmount the vertical scroll amount (positive = up, negative = down)
     * @return {@code true} if the scroll was handled, {@code false} otherwise
     */
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