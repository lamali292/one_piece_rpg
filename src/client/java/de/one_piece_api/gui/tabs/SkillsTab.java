package de.one_piece_api.gui.tabs;

import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.gui.widgets.SkillTreeViewportWidget;
import de.one_piece_api.gui.util.Tab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Optional;

/**
 * Tab displaying the skill tree with pan, zoom, and click interactions.
 *
 * Features:
 * - Interactive skill tree viewport with drag-to-pan
 * - Zoom with mouse wheel
 * - Click to unlock/purchase skills
 * - Hover tooltips for skill information
 * - Empty state display when no skills available
 *
 * Architecture:
 * - Delegates rendering to SkillTreeViewportWidget
 * - Manages input state with DragState
 * - Communicates with parent screen for description updates
 */
public class SkillsTab implements Tab {

    // ==================== Constants ====================

    // Note: These constants should ideally be accessed from OnePieceScreen.Layout
    // but are kept here for compatibility with the current OnePieceScreen structure
    private static final double DRAG_THRESHOLD = 2.0;
    private static final int EMPTY_STATE_TEXT_COLOR = 0xFFFFFFFF;

    // ==================== Fields ====================

    private final OnePieceScreen parent;
    private final TextRenderer textRenderer;
    private final DragState dragState;

    private SkillTreeViewportWidget viewportWidget;
    private int screenWidth;
    private int screenHeight;

    // ==================== Constructor ====================

    public SkillsTab(OnePieceScreen parent) {
        this.parent = Objects.requireNonNull(parent, "Parent screen cannot be null");

        MinecraftClient client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;
        this.screenWidth = client.getWindow().getScaledWidth();
        this.screenHeight = client.getWindow().getScaledHeight();
        this.dragState = new DragState();

        initializeViewport();
    }

    /**
     * Initializes or reinitializes the viewport widget with current category data.
     */
    private void initializeViewport() {
        ViewportPosition position = calculateViewportPosition();

        this.viewportWidget = new SkillTreeViewportWidget(
                position.x(),
                position.y(),
                position.width(),
                position.height()
        );

        updateViewportData();
        setupViewportCallbacks();
    }

    /**
     * Updates the viewport with latest category data from parent.
     */
    private void updateViewportData() {
        if (parent.hasCategoryData()) {
            viewportWidget.setCategoryData(parent.getCategoryData());
        }
    }

    /**
     * Sets up callback handlers for viewport events.
     */
    private void setupViewportCallbacks() {
        viewportWidget.setOnSkillHover(this::handleSkillHover);
        // Uncomment if you want to clear description when not hovering
        // viewportWidget.setOnNoSkillHover(parent::clearHoveredSkillInfo);
    }

    /**
     * Handles skill hover events by updating parent's description panel.
     */
    private void handleSkillHover(SkillTreeViewportWidget.SkillHoverInfo info) {
        parent.setHoveredSkillInfo(
                info.title(),
                info.description(),
                info.extraDescription(),
                Text.literal(info.skillId())
        );
    }

    // ==================== Tab Interface ====================

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        initializeViewport();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!parent.hasCategoryData()) {
            renderEmptyState(context);
            return;
        }

        // Ensure viewport has latest data
        updateViewportData();

        if (viewportWidget != null) {
            viewportWidget.render(context, mouseX, mouseY, delta);
        }
    }

    /**
     * Renders a friendly message when no skill tree data is available.
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
        // Only handle left mouse button
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
            return false;
        }

        if (!parent.hasCategoryData()) {
            return false;
        }

        // Check if click is within viewport bounds
        if (isMouseOutsideViewport(mouseX, mouseY)) {
            return false;
        }

        // Initialize drag state for potential drag operation
        startDragOperation(mouseX, mouseY);

        // Attempt to handle as skill click
        return handleSkillClick(mouseX, mouseY);
    }

    /**
     * Initiates a drag operation from the given mouse position.
     */
    private void startDragOperation(double mouseX, double mouseY) {
        ViewportPosition viewport = calculateViewportPosition();
        double adjustedMouseX = mouseX - viewport.x();
        double adjustedMouseY = mouseY - viewport.y();

        ClientCategoryData categoryData = parent.getCategoryData();
        if (categoryData != null) {
            dragState.startDrag(adjustedMouseX, adjustedMouseY, categoryData);
        }
    }

    /**
     * Attempts to click on a skill at the given position.
     *
     * @return true if a skill was clicked, false otherwise
     */
    private boolean handleSkillClick(double mouseX, double mouseY) {
        Optional<String> skillId = viewportWidget.getSkillAtPosition(mouseX, mouseY);

        if (skillId.isEmpty()) {
            return false;
        }

        sendSkillClickPacket(skillId.get());
        dragState.preventDrag(); // Prevent drag after successful click

        // The spell slots will update automatically on next render
        // due to the render loop calling updateSpellData()

        return true;
    }

    /**
     * Sends a network packet to the server to handle skill click.
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
        // Only handle left mouse button drags
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
            return false;
        }

        if (!dragState.canDrag()) {
            return false;
        }

        dragState.updateDrag(deltaX, deltaY);

        // Only apply pan once drag threshold is exceeded
        if (dragState.shouldStartDragging() && parent.hasCategoryData()) {
            applyViewportPan(mouseX, mouseY);
        }

        return true;
    }

    /**
     * Applies panning to the viewport based on current mouse position.
     */
    private void applyViewportPan(double mouseX, double mouseY) {
        ViewportPosition viewport = calculateViewportPosition();
        double adjustedMouseX = mouseX - viewport.x();
        double adjustedMouseY = mouseY - viewport.y();

        viewportWidget.applyPan(
                adjustedMouseX,
                adjustedMouseY,
                dragState.getStartX(),
                dragState.getStartY()
        );
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Only handle left mouse button
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) {
            return false;
        }

        // If drag was very small, treat as click
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

        viewportWidget.applyZoom(mouseX, mouseY, verticalAmount);
        return true;
    }

    // ==================== Public API ====================

    /**
     * Refreshes the viewport with updated category data.
     * Called when skill tree data changes (e.g., after unlock).
     */
    public void refreshCategoryData() {
        if (viewportWidget != null && parent.hasCategoryData()) {
            viewportWidget.setCategoryData(parent.getCategoryData());
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Calculates the viewport position and dimensions based on current screen size.
     * Uses OnePieceScreen.Layout for proper encapsulation of layout constants.
     *
     * @return ViewportPosition containing x, y coordinates and width, height
     */
    private ViewportPosition calculateViewportPosition() {
        // Calculate viewport position centered in the screen
        int x = (screenWidth - OnePieceScreen.Layout.BACKGROUND_WIDTH) / 2 + OnePieceScreen.Layout.SKILLTREE_OFFSET_X;
        int y = (screenHeight - OnePieceScreen.Layout.BACKGROUND_HEIGHT) / 2 + OnePieceScreen.Layout.CONTENT_OFFSET_Y;

        return new ViewportPosition(
                x,
                y,
                OnePieceScreen.Layout.SKILLTREE_WIDTH,
                OnePieceScreen.Layout.CONTENT_HEIGHT
        );
    }

    /**
     * Checks if mouse coordinates are outside the viewport bounds.
     */
    private boolean isMouseOutsideViewport(double mouseX, double mouseY) {
        return viewportWidget != null && viewportWidget.isMouseNotInViewport(mouseX, mouseY);
    }

    // ==================== Inner Classes ====================

    /**
     * Tracks drag operation state to differentiate between clicks and drags.
     *
     * Drag detection:
     * - Tracks total mouse movement since drag start
     * - If movement exceeds DRAG_THRESHOLD, it's a drag operation
     * - If movement is below threshold on release, it's treated as a click
     */
    private static class DragState {
        private double startX = 0;
        private double startY = 0;
        private double totalDrag = 0;
        private boolean dragEnabled = false;

        /**
         * Begins tracking a potential drag operation.
         */
        void startDrag(double mouseX, double mouseY, ClientCategoryData categoryData) {
            Objects.requireNonNull(categoryData, "Category data cannot be null");

            // Calculate start position relative to category offset
            this.startX = mouseX - categoryData.getX();
            this.startY = mouseY - categoryData.getY();
            this.totalDrag = 0;
            this.dragEnabled = true;
        }

        /**
         * Updates drag state with mouse movement delta.
         */
        void updateDrag(double deltaX, double deltaY) {
            this.totalDrag += Math.abs(deltaX) + Math.abs(deltaY);
        }

        /**
         * Cancels drag operation (e.g., when a skill is clicked).
         */
        void preventDrag() {
            this.dragEnabled = false;
        }

        /**
         * Ends the drag operation and resets state.
         */
        void endDrag() {
            this.dragEnabled = false;
            this.totalDrag = 0;
        }

        /**
         * @return true if dragging is currently allowed
         */
        boolean canDrag() {
            return dragEnabled;
        }

        /**
         * @return true if mouse has moved beyond the drag threshold
         */
        boolean shouldStartDragging() {
            return totalDrag > DRAG_THRESHOLD;
        }

        /**
         * @return true if drag is active but movement is below threshold
         */
        boolean isSmallDrag() {
            return dragEnabled && totalDrag <= DRAG_THRESHOLD;
        }

        double getStartX() {
            return startX;
        }

        double getStartY() {
            return startY;
        }
    }

    /**
     * Immutable viewport position and dimensions in screen coordinates.
     */
    private record ViewportPosition(int x, int y, int width, int height) {
        ViewportPosition {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "Viewport dimensions must be positive: " + width + "x" + height
                );
            }
        }
    }
}