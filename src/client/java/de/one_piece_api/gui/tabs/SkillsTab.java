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
 * <p>
 * This tab provides a fully interactive skill tree viewport where players can:
 * <ul>
 *     <li>Pan the view by clicking and dragging</li>
 *     <li>Zoom in/out with the mouse wheel</li>
 *     <li>Click skills to unlock/purchase them</li>
 *     <li>Hover over skills to view tooltips</li>
 * </ul>
 *
 * <h2>Architecture:</h2>
 * <ul>
 *     <li>Delegates rendering to {@link SkillTreeViewportWidget}</li>
 *     <li>Manages input state with {@link DragState}</li>
 *     <li>Communicates with parent screen for description updates</li>
 *     <li>Distinguishes between clicks and drags using movement threshold</li>
 * </ul>
 *
 * @see Tab
 * @see SkillTreeViewportWidget
 * @see OnePieceScreen
 */
public class SkillsTab implements Tab {

    // ==================== Constants ====================

    /**
     * Minimum mouse movement (in pixels) required to trigger a drag operation.
     * <p>
     * Movements below this threshold are treated as clicks rather than drags,
     * allowing for precise skill selection even with minor hand tremors.
     */
    private static final double DRAG_THRESHOLD = 2.0;

    /**
     * Text color for the empty state message (white with full opacity).
     */
    private static final int EMPTY_STATE_TEXT_COLOR = 0xFFFFFFFF;

    // ==================== Fields ====================

    /** Parent screen containing this tab */
    private final OnePieceScreen parent;

    /** Text renderer for drawing empty state messages */
    private final TextRenderer textRenderer;

    /** State tracker for drag operations */
    private final DragState dragState;

    /** Widget managing the skill tree viewport rendering and interaction */
    private SkillTreeViewportWidget viewportWidget;

    /** Current screen width in scaled pixels */
    private int screenWidth;

    /** Current screen height in scaled pixels */
    private int screenHeight;

    // ==================== Constructor ====================

    /**
     * Creates a new skills tab.
     * <p>
     * Initializes the viewport widget, sets up callbacks for skill hover events,
     * and prepares the drag state tracker.
     *
     * @param parent the parent OnePiece screen
     * @throws NullPointerException if parent is null
     */
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
     * <p>
     * Creates a new viewport widget at the calculated position and sets up
     * all necessary callbacks and data bindings.
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
     * <p>
     * Should be called whenever the category data might have changed
     * (e.g., after skills are learned or reset).
     */
    private void updateViewportData() {
        if (parent.hasCategoryData()) {
            viewportWidget.setCategoryData(parent.getCategoryData());
        }
    }

    /**
     * Sets up callback handlers for viewport events.
     * <p>
     * Configures the skill hover callback to update the parent screen's
     * description panel with skill information.
     */
    private void setupViewportCallbacks() {
        viewportWidget.setOnSkillHover(this::handleSkillHover);
        // Uncomment if you want to clear description when not hovering
        // viewportWidget.setOnNoSkillHover(parent::clearHoveredSkillInfo);
    }

    /**
     * Handles skill hover events by updating parent's description panel.
     * <p>
     * Called when the mouse hovers over a skill node in the viewport.
     * Extracts skill information and passes it to the parent screen
     * for display in the description panel.
     *
     * @param info the skill hover information containing title, descriptions, and ID
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

    /**
     * Handles screen resize events.
     * <p>
     * Updates stored dimensions and reinitializes the viewport widget
     * to match the new screen size.
     *
     * @param client the Minecraft client instance
     * @param width the new screen width
     * @param height the new screen height
     */
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        initializeViewport();
    }

    /**
     * Renders the skills tab content.
     * <p>
     * Displays either the skill tree viewport (if data is available) or
     * an empty state message (if no skill data is loaded).
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
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
     * <p>
     * Displays centered text indicating that the skill tree is empty,
     * using vanilla advancement screen translations for consistency.
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

    /**
     * Handles mouse click events.
     * <p>
     * Processes left-click events for skill interaction. Initializes drag
     * tracking to distinguish between clicks and drag operations.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (only left button is handled)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
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
     * <p>
     * Sets up the drag state with the starting mouse position relative
     * to the current viewport offset.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
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
     * <p>
     * If a skill is found at the mouse position, sends a network packet
     * to the server to process the skill click (unlock/purchase).
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if a skill was clicked, {@code false} otherwise
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
     * <p>
     * Constructs and sends a {@link SkillClickOutPacket} with the category ID
     * and skill ID to notify the server of the player's action.
     *
     * @param skillId the ID of the clicked skill
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

    /**
     * Handles mouse drag events for viewport panning.
     * <p>
     * Tracks mouse movement and applies viewport panning once the drag
     * threshold is exceeded. This allows for smooth panning while still
     * supporting click interactions.
     *
     * @param mouseX the current mouse x-coordinate
     * @param mouseY the current mouse y-coordinate
     * @param button the mouse button being dragged
     * @param deltaX the horizontal mouse movement since last frame
     * @param deltaY the vertical mouse movement since last frame
     * @return {@code true} if the drag was handled, {@code false} otherwise
     */
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
     * <p>
     * Calculates the viewport-relative mouse position and applies the pan
     * transformation to the skill tree view.
     *
     * @param mouseX the current mouse x-coordinate
     * @param mouseY the current mouse y-coordinate
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

    /**
     * Handles mouse button release events.
     * <p>
     * If the drag distance was below the threshold, treats the action as
     * a click rather than a drag. Resets drag state after processing.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (only left button is handled)
     * @return {@code true} if the release was handled, {@code false} otherwise
     */
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

    /**
     * Handles mouse scroll wheel events for viewport zooming.
     * <p>
     * Zooms the skill tree view in or out centered on the mouse position,
     * allowing for precise navigation to specific areas of the tree.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param horizontalAmount the horizontal scroll amount (unused)
     * @param verticalAmount the vertical scroll amount (positive = zoom in, negative = zoom out)
     * @return {@code true} if the scroll was handled, {@code false} otherwise
     */
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
     * <p>
     * Called when skill tree data changes (e.g., after skill unlock or reset)
     * to ensure the display reflects the current state.
     */
    public void refreshCategoryData() {
        if (viewportWidget != null && parent.hasCategoryData()) {
            viewportWidget.setCategoryData(parent.getCategoryData());
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Calculates the viewport position and dimensions based on current screen size.
     * <p>
     * Uses {@link OnePieceScreen.Layout} constants for proper encapsulation
     * of layout parameters, ensuring consistency with the parent screen.
     *
     * @return viewport position containing x, y coordinates and width, height
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
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if mouse is outside viewport, {@code false} otherwise
     */
    private boolean isMouseOutsideViewport(double mouseX, double mouseY) {
        return viewportWidget != null && viewportWidget.isMouseNotInViewport(mouseX, mouseY);
    }

    // ==================== Inner Classes ====================

    /**
     * Tracks drag operation state to differentiate between clicks and drags.
     * <p>
     * This class implements a simple drag detection algorithm:
     * <ol>
     *     <li>Records the starting mouse position when drag begins</li>
     *     <li>Accumulates total mouse movement distance</li>
     *     <li>If movement exceeds {@link #DRAG_THRESHOLD}, treats as drag</li>
     *     <li>If movement is below threshold on release, treats as click</li>
     * </ol>
     * This allows for precise skill selection while still supporting smooth panning.
     */
    private static class DragState {
        /** Starting x-coordinate of the drag (viewport-relative) */
        private double startX = 0;

        /** Starting y-coordinate of the drag (viewport-relative) */
        private double startY = 0;

        /** Total accumulated drag distance in pixels */
        private double totalDrag = 0;

        /** Whether dragging is currently enabled */
        private boolean dragEnabled = false;

        /**
         * Begins tracking a potential drag operation.
         * <p>
         * Calculates the starting position relative to the category offset
         * to enable proper pan calculations.
         *
         * @param mouseX the mouse x-coordinate (viewport-relative)
         * @param mouseY the mouse y-coordinate (viewport-relative)
         * @param categoryData the category data containing current offset
         * @throws NullPointerException if categoryData is null
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
         * <p>
         * Accumulates the total Manhattan distance moved since drag start.
         *
         * @param deltaX the horizontal mouse movement
         * @param deltaY the vertical mouse movement
         */
        void updateDrag(double deltaX, double deltaY) {
            this.totalDrag += Math.abs(deltaX) + Math.abs(deltaY);
        }

        /**
         * Cancels drag operation (e.g., when a skill is clicked).
         * <p>
         * This prevents accidental panning when the user clicks on a skill.
         */
        void preventDrag() {
            this.dragEnabled = false;
        }

        /**
         * Ends the drag operation and resets state.
         * <p>
         * Should be called on mouse button release.
         */
        void endDrag() {
            this.dragEnabled = false;
            this.totalDrag = 0;
        }

        /**
         * Checks if dragging is currently allowed.
         *
         * @return {@code true} if dragging is enabled, {@code false} otherwise
         */
        boolean canDrag() {
            return dragEnabled;
        }

        /**
         * Checks if mouse has moved beyond the drag threshold.
         *
         * @return {@code true} if total movement exceeds threshold, {@code false} otherwise
         */
        boolean shouldStartDragging() {
            return totalDrag > DRAG_THRESHOLD;
        }

        /**
         * Checks if drag is active but movement is below threshold.
         * <p>
         * Used to determine if a mouse release should be treated as a click.
         *
         * @return {@code true} if movement is below threshold, {@code false} otherwise
         */
        boolean isSmallDrag() {
            return dragEnabled && totalDrag <= DRAG_THRESHOLD;
        }

        /**
         * Gets the starting x-coordinate of the drag.
         *
         * @return the start x-coordinate
         */
        double getStartX() {
            return startX;
        }

        /**
         * Gets the starting y-coordinate of the drag.
         *
         * @return the start y-coordinate
         */
        double getStartY() {
            return startY;
        }
    }

    /**
     * Immutable record representing viewport position and dimensions in screen coordinates.
     * <p>
     * Ensures viewport dimensions are always positive and provides a clean
     * interface for passing viewport bounds throughout the tab.
     *
     * @param x the left edge x-coordinate
     * @param y the top edge y-coordinate
     * @param width the viewport width in pixels (must be positive)
     * @param height the viewport height in pixels (must be positive)
     * @throws IllegalArgumentException if width or height is not positive
     */
    private record ViewportPosition(int x, int y, int width, int height) {
        /**
         * Compact constructor validating viewport dimensions.
         *
         * @throws IllegalArgumentException if width or height is not positive
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