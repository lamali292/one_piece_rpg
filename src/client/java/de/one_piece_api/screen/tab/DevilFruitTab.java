package de.one_piece_api.screen.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.screen.widget.main.devilfruit.SkillPathWidget;
import de.one_piece_api.screen.widget.main.devilfruit.ScrollState;
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
 * Controller for the devil fruit tab, managing vertical scrolling and skill path rendering.
 * <p>
 * This tab presents multiple parallel skill paths in a scrollable vertical layout,
 * allowing players to view and interact with their devil fruit abilities.
 *
 * <h2>Architecture:</h2>
 * <pre>
 * DevilFruitTab (Controller)
 *   ├── ScrollState (Model)
 *   ├── SkillPathWidget[] (Views)
 *   └── OnePieceScreen (Parent)
 * </pre>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Vertical scrolling with configurable limits</li>
 *     <li>Click-and-drag panning (right or middle mouse button)</li>
 *     <li>Viewport clipping to prevent overflow rendering</li>
 *     <li>Dynamic path widget positioning</li>
 *     <li>Connection rendering between related skills</li>
 * </ul>
 */
public class DevilFruitTab implements Tab {

    // ==================== Constants ====================

    /** Vertical spacing between skill nodes in pixels */
    private static final int VERTICAL_SPACING = 36; // Must match SkillPathWidget constant

    // ==================== Fields ====================

    /** Parent screen for data access and callbacks */
    private final OnePieceScreen parent;

    /** Configuration data for devil fruit paths */
    private final DevilFruitConfig devilFruitConfig;

    /** List of skill path rendering widgets */
    private final List<SkillPathWidget> pathWidgets = new ArrayList<>();

    /** Scroll state manager */
    private final ScrollState scrollState;

    /** Drag state manager */
    private final DragState dragState;

    // Viewport bounds
    /** Left edge x-coordinate of the visible viewport */
    private int viewportX;

    /** Top edge y-coordinate of the visible viewport */
    private int viewportY;

    /** Width of the visible viewport in pixels */
    private int viewportWidth;

    /** Height of the visible viewport in pixels */
    private int viewportHeight;

    /** Current screen dimensions */
    private int screenWidth;
    private int screenHeight;

    // ==================== Constructor ====================

    /**
     * Creates a new devil fruit tab.
     *
     * @param parent the parent OnePiece screen
     * @param devilFruitConfig the devil fruit configuration containing skill paths
     * @throws NullPointerException if parent or config is null
     */
    public DevilFruitTab(OnePieceScreen parent, DevilFruitConfig devilFruitConfig) {
        this.parent = Objects.requireNonNull(parent, "Parent screen cannot be null");
        this.devilFruitConfig = Objects.requireNonNull(devilFruitConfig, "Devil fruit config cannot be null");

        MinecraftClient client = MinecraftClient.getInstance();
        this.screenWidth = client.getWindow().getScaledWidth();
        this.screenHeight = client.getWindow().getScaledHeight();

        this.scrollState = new ScrollState();
        this.dragState = new DragState();

        initializePaths();
    }

    // ==================== Initialization ====================

    /**
     * Initializes skill path widgets based on configuration.
     * <p>
     * Creates a widget for each path in the devil fruit configuration,
     * generating skill IDs and associating them with the category configuration.
     */
    private void initializePaths() {
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

            pathWidgets.add(new SkillPathWidget(
                    parent,
                    pathIndex,
                    pathSkillIds,
                    categoryConfig,
                    skillConfig
            ));
        }

        updateScrollLimits();
    }

    /**
     * Updates scroll limits based on the longest skill path.
     * <p>
     * Calculates maximum scroll distances to ensure all skills remain accessible.
     */
    private void updateScrollLimits() {
        if (pathWidgets.isEmpty() || devilFruitConfig == null) {
            scrollState.setLimits(0, 0);
            return;
        }

        int maxPathLength = devilFruitConfig.paths().stream()
                .mapToInt(path -> path.skills().size())
                .max()
                .orElse(0);

        scrollState.calculateLimits(maxPathLength, VERTICAL_SPACING);
    }

    /**
     * Updates widget positions based on current scroll offset.
     */
    private void updateWidgetPositions() {
        calculateViewportBounds();

        int centerX = viewportX + viewportWidth / 2;
        int centerY = viewportY + viewportHeight / 2 + scrollState.getOffset();

        int frameHalfSize = 13;
        int horizontalSpacing = (frameHalfSize * 2) + 15;

        for (int i = 0; i < pathWidgets.size(); i++) {
            int pathX = centerX + i * horizontalSpacing -
                    (pathWidgets.size() - 1) * horizontalSpacing / 2;
            pathWidgets.get(i).setPosition(pathX, centerY);
        }
    }

    /**
     * Calculates viewport bounds relative to screen position.
     */
    private void calculateViewportBounds() {
        int screenX = (screenWidth - OnePieceScreen.Layout.BACKGROUND_WIDTH) / 2;
        int screenY = (screenHeight - OnePieceScreen.Layout.BACKGROUND_HEIGHT) / 2;

        this.viewportX = screenX + OnePieceScreen.Layout.SKILLTREE_OFFSET_X;
        this.viewportY = screenY + OnePieceScreen.Layout.CONTENT_OFFSET_Y;
        this.viewportWidth = OnePieceScreen.Layout.SKILLTREE_WIDTH;
        this.viewportHeight = OnePieceScreen.Layout.CONTENT_HEIGHT;
    }

    // ==================== Lifecycle ====================

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        updateWidgetPositions();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (pathWidgets.isEmpty()) {
            initializePaths();
        }

        updateWidgetPositions();

        // Enable scissor to clip rendering to viewport bounds
        context.enableScissor(
                viewportX,
                viewportY,
                viewportX + viewportWidth,
                viewportY + viewportHeight
        );

        RenderSystem.enableBlend();

        // Render connections first (behind skills)
        renderConnections(context);

        // Render skills
        for (SkillPathWidget widget : pathWidgets) {
            widget.updateSkillData();
            widget.render(context, mouseX, mouseY, delta);
        }

        RenderSystem.disableBlend();

        context.disableScissor();
    }

    /**
     * Renders all connection lines between skills.
     *
     * @param context the drawing context
     */
    private void renderConnections(DrawContext context) {
        if (!parent.hasCategoryData() || devilFruitConfig == null) {
            return;
        }

        ConnectionBatchedRenderer connectionRenderer = new ConnectionBatchedRenderer();
        ClientCategoryConfig categoryConfig = parent.getCategoryData().getConfig();

        for (SkillPathWidget widget : pathWidgets) {
            widget.renderConnections(context, connectionRenderer, categoryConfig);
        }

        connectionRenderer.draw();
    }

    // ==================== Input Handling ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOutsideViewport(mouseX, mouseY)) {
            return false;
        }

        // Right or middle click to start dragging
        if (button == 1 || button == 2) {
            dragState.startDrag(mouseY, scrollState.getOffset());
            return true;
        }

        // Left click for skill interaction
        for (SkillPathWidget widget : pathWidgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                // Refresh all widgets' skill data
                for (SkillPathWidget w : pathWidgets) {
                    w.updateSkillData();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1 || button == 2) {
            dragState.endDrag();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double deltaX, double deltaY) {
        if (!dragState.isDragging()) {
            return false;
        }

        int newOffset = dragState.calculateOffset(mouseY);
        scrollState.setOffset(newOffset);
        updateWidgetPositions();

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        if (isMouseOutsideViewport(mouseX, mouseY)) {
            return false;
        }

        scrollState.scrollByWheel(verticalAmount);
        updateWidgetPositions();

        return true;
    }

    // ==================== Helper Methods ====================

    /**
     * Checks if the mouse is outside the viewport bounds.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if outside, {@code false} otherwise
     */
    private boolean isMouseOutsideViewport(double mouseX, double mouseY) {
        return mouseX < viewportX || mouseX >= viewportX + viewportWidth ||
                mouseY < viewportY || mouseY >= viewportY + viewportHeight;
    }

    // ==================== Inner Classes ====================

    /**
     * Manages drag state for scroll panning.
     */
    private static class DragState {
        private boolean dragging = false;
        private double startY;
        private int startOffset;

        /**
         * Starts a new drag operation.
         *
         * @param mouseY the starting mouse y-coordinate
         * @param currentOffset the current scroll offset
         */
        void startDrag(double mouseY, int currentOffset) {
            this.dragging = true;
            this.startY = mouseY;
            this.startOffset = currentOffset;
        }

        /**
         * Calculates the new offset based on current mouse position.
         *
         * @param currentY the current mouse y-coordinate
         * @return the calculated offset
         */
        int calculateOffset(double currentY) {
            double dragDistance = currentY - startY;
            return (int) (startOffset + dragDistance);
        }

        /**
         * Ends the drag operation.
         */
        void endDrag() {
            this.dragging = false;
        }

        /**
         * Checks if currently dragging.
         *
         * @return {@code true} if dragging, {@code false} otherwise
         */
        boolean isDragging() {
            return dragging;
        }
    }
}