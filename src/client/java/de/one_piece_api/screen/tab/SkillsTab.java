package de.one_piece_api.screen.tab;

import de.one_piece_api.screen.OnePieceScreen;
import de.one_piece_api.screen.widget.main.skill_tree.SkillTreeViewportWidget;
import de.one_piece_api.screen.widget.main.skill_tree.ViewportState;
import de.one_piece_api.screen.component.Tab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * Controller for the skills tab, managing viewport state and user interactions.
 * <p>
 * This class acts as a controller between the view (SkillTreeViewportWidget),
 * the model (ClientCategoryData), and the parent screen. It handles:
 * <ul>
 *     <li>Viewport state management (pan, zoom)</li>
 *     <li>Mouse input processing (click, drag, scroll)</li>
 *     <li>Drag gesture detection</li>
 *     <li>Network communication for skill clicks</li>
 *     <li>Data synchronization with parent screen</li>
 * </ul>
 *
 * <h2>Architecture:</h2>
 * <pre>
 * SkillsTab (Controller)
 *   ├── ViewportState (Model)
 *   ├── SkillTreeViewportWidget (View)
 *   └── OnePieceScreen (Parent)
 * </pre>
 */
public class SkillsTab implements Tab {

    // ==================== Constants ====================

    /** Distance threshold for distinguishing clicks from drags */
    private static final double DRAG_THRESHOLD = 2.0;

    /** Text color for empty state messages */
    private static final int EMPTY_STATE_TEXT_COLOR = 0xFFFFFFFF;

    // ==================== Fields ====================

    /** Parent screen for data access and callbacks */
    private final OnePieceScreen parent;

    /** Text renderer for empty state messages */
    private final TextRenderer textRenderer;

    /** Viewport transformation state */
    private ViewportState viewportState;

    /** The skill tree rendering widget */
    private SkillTreeViewportWidget viewportWidget;

    /** Drag state manager */
    private final DragState dragState;

    /** Current screen dimensions */
    private int screenWidth;
    private int screenHeight;

    /** Flag indicating data needs to be refreshed */
    private boolean needsDataUpdate = false;

    // ==================== Constructor ====================

    /**
     * Creates a new skills tab.
     *
     * @param parent the parent screen
     * @throws NullPointerException if parent is null
     */
    public SkillsTab(OnePieceScreen parent) {
        this.parent = Objects.requireNonNull(parent, "Parent screen cannot be null");

        MinecraftClient client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;
        this.screenWidth = client.getWindow().getScaledWidth();
        this.screenHeight = client.getWindow().getScaledHeight();
        this.dragState = new DragState();

        initializeComponents();
    }

    /**
     * Initializes viewport widget and state.
     */
    private void initializeComponents() {
        ViewportPosition position = calculateViewportPosition();

        // Create viewport state
        this.viewportState = new ViewportState(position.width(), position.height());

        // Create viewport widget
        this.viewportWidget = new SkillTreeViewportWidget(
                position.x(),
                position.y(),
                position.width(),
                position.height()
        );

        // Initialize viewport state with data if available
        updateViewportState();

        // Setup callbacks
        setupViewportCallbacks();
    }

    /**
     * Updates viewport state with current category data.
     */
    private void updateViewportState() {
        if (!parent.hasCategoryData()) {
            return;
        }

        ClientCategoryData categoryData = parent.getCategoryData();

        // Set bounds and initial position from category data
        viewportState.setBounds(categoryData.getConfig().getBounds());
        viewportState.setPosition(
                categoryData.getX(),
                categoryData.getY(),
                categoryData.getScale()
        );

        needsDataUpdate = false;
    }

    /**
     * Synchronizes category data with current viewport state.
     * <p>
     * Updates the category data to match the current viewport position.
     */
    private void syncCategoryDataWithViewport() {
        if (!parent.hasCategoryData()) {
            return;
        }

        ClientCategoryData categoryData = parent.getCategoryData();
        categoryData.setX(viewportState.getX());
        categoryData.setY(viewportState.getY());
        categoryData.setScale(viewportState.getScale());
    }

    /**
     * Sets up event callbacks for the viewport widget.
     */
    private void setupViewportCallbacks() {
        viewportWidget.setOnSkillHover(this::handleSkillHover);
        viewportWidget.setOnNoSkillHover(parent::clearHoveredSkillInfo);
    }

    /**
     * Handles skill hover events from the widget.
     *
     * @param info the hover information
     */
    private void handleSkillHover(SkillTreeViewportWidget.SkillHoverInfo info) {
        parent.setHoveredSkillInfo(
                info.title(),
                info.description(),
                info.extraDescription(),
                Text.literal(info.skillId())
        );
    }

    // ==================== Data Management ====================

    /**
     * Marks that viewport data needs updating.
     * <p>
     * Called by parent screen when category data changes.
     */
    public void markDataUpdateNeeded() {
        needsDataUpdate = true;
    }

    /**
     * Refreshes category data from parent screen.
     * <p>
     * Updates viewport state to match new category data.
     */
    public void refreshCategoryData() {
        if (parent.hasCategoryData()) {
            updateViewportState();
        }
    }

    // ==================== Lifecycle ====================

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        initializeComponents();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!parent.hasCategoryData()) {
            renderEmptyState(context);
            return;
        }

        // Process pending updates
        if (needsDataUpdate) {
            updateViewportState();
        }

        // Render the skill tree
        viewportWidget.render(
                context,
                mouseX,
                mouseY,
                delta,
                viewportState,
                parent.getCategoryData(),
                parent.getClassConfig()
        );
    }

    /**
     * Renders the empty state when no category data is available.
     *
     * @param context the drawing context
     */
    private void renderEmptyState(DrawContext context) {
        ViewportPosition viewport = calculateViewportPosition();
        int centerX = viewport.x() + viewport.width() / 2;
        int centerY = viewport.y() + viewport.height() / 2;
        int fontHeight = textRenderer.fontHeight;

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.translatable("advancements.sad_label"),
                centerX,
                centerY - fontHeight,
                EMPTY_STATE_TEXT_COLOR
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.translatable("advancements.empty"),
                centerX,
                centerY + fontHeight,
                EMPTY_STATE_TEXT_COLOR
        );
    }

    // ==================== Input Handling ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 || !parent.hasCategoryData()) {
            return false;
        }

        if (isMouseOutsideViewport(mouseX, mouseY)) {
            return false;
        }

        startDragOperation(mouseX, mouseY);
        return handleSkillClick(mouseX, mouseY);
    }

    /**
     * Starts a drag operation at the given mouse position.
     *
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     */
    private void startDragOperation(double mouseX, double mouseY) {
        ViewportPosition viewport = calculateViewportPosition();
        double relativeMouseX = mouseX - viewport.x();
        double relativeMouseY = mouseY - viewport.y();

        dragState.startDrag(relativeMouseX, relativeMouseY, viewportState);
    }

    /**
     * Handles skill click detection and network communication.
     *
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @return {@code true} if a skill was clicked, {@code false} otherwise
     */
    private boolean handleSkillClick(double mouseX, double mouseY) {
        var skillId = viewportWidget.getSkillAtPosition(
                mouseX,
                mouseY,
                viewportState,
                parent.getCategoryData().getConfig(),
                parent.getClassConfig()
        );

        if (skillId.isEmpty()) {
            return false;
        }

        sendSkillClickPacket(skillId.get());
        dragState.preventDrag();

        return true;
    }

    /**
     * Sends a skill click packet to the server.
     *
     * @param skillId the skill identifier
     */
    private void sendSkillClickPacket(String skillId) {
        ClientCategoryData categoryData = parent.getCategoryData();
        if (categoryData == null) {
            return;
        }

        SkillsClientMod.getInstance()
                .getPacketSender()
                .send(new SkillClickOutPacket(
                        categoryData.getConfig().id(),
                        skillId
                ));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 || !dragState.canDrag()) {
            return false;
        }

        dragState.updateDrag(deltaX, deltaY);

        if (dragState.shouldStartDragging() && parent.hasCategoryData()) {
            applyViewportPan(mouseX, mouseY);
        }

        return true;
    }

    /**
     * Applies panning to the viewport based on current mouse position.
     *
     * @param mouseX the current mouse x-coordinate in screen space
     * @param mouseY the current mouse y-coordinate in screen space
     */
    private void applyViewportPan(double mouseX, double mouseY) {
        ViewportPosition viewport = calculateViewportPosition();
        double relativeMouseX = mouseX - viewport.x();
        double relativeMouseY = mouseY - viewport.y();

        // Calculate delta from drag start
        int deltaX = (int) Math.round(relativeMouseX - dragState.getStartX());
        int deltaY = (int) Math.round(relativeMouseY - dragState.getStartY());

        // Apply to viewport state
        viewportState.setPosition(deltaX, deltaY, viewportState.getScale());

        // Sync back to category data
        syncCategoryDataWithViewport();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
            return false;
        }

        // Handle small drags as clicks
        if (dragState.isSmallDrag() && parent.hasCategoryData()) {
            handleSkillClick(mouseX, mouseY);
        }

        dragState.endDrag();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOutsideViewport(mouseX, mouseY) || !parent.hasCategoryData()) {
            return false;
        }

        // Convert to viewport-relative coordinates
        ViewportPosition viewport = calculateViewportPosition();
        double relativeMouseX = mouseX - viewport.x();
        double relativeMouseY = mouseY - viewport.y();

        // Apply zoom
        viewportState.applyZoom(relativeMouseX, relativeMouseY, verticalAmount);

        // Sync back to category data
        syncCategoryDataWithViewport();

        return true;
    }

    // ==================== Layout Calculation ====================

    /**
     * Calculates the viewport position within the screen.
     *
     * @return the viewport position and dimensions
     */
    private ViewportPosition calculateViewportPosition() {
        int x = (screenWidth - OnePieceScreen.Layout.BACKGROUND_WIDTH) / 2 +
                OnePieceScreen.Layout.SKILLTREE_OFFSET_X;
        int y = (screenHeight - OnePieceScreen.Layout.BACKGROUND_HEIGHT) / 2 +
                OnePieceScreen.Layout.CONTENT_OFFSET_Y;

        return new ViewportPosition(
                x,
                y,
                OnePieceScreen.Layout.SKILLTREE_WIDTH,
                OnePieceScreen.Layout.CONTENT_HEIGHT
        );
    }

    /**
     * Checks if the mouse is outside the viewport bounds.
     *
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @return {@code true} if outside, {@code false} otherwise
     */
    private boolean isMouseOutsideViewport(double mouseX, double mouseY) {
        return viewportWidget.isMouseOutsideViewport(mouseX, mouseY);
    }

    // ==================== Inner Classes ====================

    /**
     * Manages drag state for distinguishing clicks from drags.
     * <p>
     * Tracks the drag start position and accumulated movement to determine
     * whether the user intended to click or pan the viewport.
     */
    private static class DragState {
        private double startX = 0;
        private double startY = 0;
        private double totalDrag = 0;
        private boolean dragEnabled = false;

        /**
         * Starts a new drag operation.
         *
         * @param mouseX the mouse x-coordinate in viewport space
         * @param mouseY the mouse y-coordinate in viewport space
         * @param viewportState the current viewport state
         * @throws NullPointerException if viewportState is null
         */
        void startDrag(double mouseX, double mouseY, ViewportState viewportState) {
            Objects.requireNonNull(viewportState, "Viewport state cannot be null");

            // Store starting position relative to viewport origin
            this.startX = mouseX - viewportState.getX();
            this.startY = mouseY - viewportState.getY();
            this.totalDrag = 0;
            this.dragEnabled = true;
        }

        /**
         * Updates the drag with movement delta.
         *
         * @param deltaX the horizontal movement
         * @param deltaY the vertical movement
         */
        void updateDrag(double deltaX, double deltaY) {
            this.totalDrag += Math.abs(deltaX) + Math.abs(deltaY);
        }

        /**
         * Prevents this drag from being recognized as a drag.
         * <p>
         * Used when a skill is clicked to prevent accidental panning.
         */
        void preventDrag() {
            this.dragEnabled = false;
        }

        /**
         * Ends the drag operation.
         */
        void endDrag() {
            this.dragEnabled = false;
            this.totalDrag = 0;
        }

        /**
         * Checks if dragging is currently enabled.
         *
         * @return {@code true} if drag is enabled, {@code false} otherwise
         */
        boolean canDrag() {
            return dragEnabled;
        }

        /**
         * Checks if the drag has exceeded the threshold to start panning.
         *
         * @return {@code true} if should start dragging, {@code false} otherwise
         */
        boolean shouldStartDragging() {
            return totalDrag > DRAG_THRESHOLD;
        }

        /**
         * Checks if this was a small drag (should be treated as a click).
         *
         * @return {@code true} if small drag, {@code false} otherwise
         */
        boolean isSmallDrag() {
            return dragEnabled && totalDrag <= DRAG_THRESHOLD;
        }

        /**
         * Gets the drag start x-coordinate.
         *
         * @return the start x-coordinate
         */
        double getStartX() {
            return startX;
        }

        /**
         * Gets the drag start y-coordinate.
         *
         * @return the start y-coordinate
         */
        double getStartY() {
            return startY;
        }
    }

    /**
     * Immutable record representing viewport position and dimensions.
     *
     * @param x the x-coordinate in screen space
     * @param y the y-coordinate in screen space
     * @param width the viewport width in pixels
     * @param height the viewport height in pixels
     */
    private record ViewportPosition(int x, int y, int width, int height) {
        /**
         * Compact constructor validating dimensions.
         */
        ViewportPosition {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "Viewport dimensions must be positive: " + width + "x" + height
                );
            }
        }
    }
}