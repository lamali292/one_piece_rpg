package de.one_piece_api.screen.widget.main.skill_tree;

import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.util.Bounds2i;
import org.joml.Vector2i;

/**
 * Manages viewport transformation state for the skill tree.
 * <p>
 * Encapsulates pan (x, y offset) and zoom (scale) state with automatic
 * clamping to keep the viewport within valid bounds. This separates
 * transformation logic from rendering and input handling.
 *
 * <h2>Coordinate Systems:</h2>
 * <ul>
 *     <li>World space - Raw skill positions from configuration</li>
 *     <li>View space - After applying x/y offset and scale transformation</li>
 *     <li>Screen space - Final pixel positions in the window</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * ViewportState state = new ViewportState(800, 600);
 * state.setBounds(skillTreeBounds);
 * state.setPosition(0, 0, 1.5f); // Center at origin, 1.5x zoom
 *
 * // Apply zoom centered on mouse
 * state.applyZoom(mouseX, mouseY, scrollAmount);
 *
 * // Pan the viewport
 * state.pan(deltaX, deltaY);
 * }</pre>
 */
public class ViewportState {

    // ==================== Constants ====================

    /** Extra padding added to content bounds in pixels */
    private static final int CONTENT_GROW = 32;

    /** Padding to allow dragging slightly beyond viewport edges */
    private static final int DRAG_PADDING = 40;

    /** Multiplier for scroll wheel sensitivity */
    private static final double SCROLL_SENSITIVITY = 0.25;

    // ==================== Fields ====================

    /** Width of the viewport in pixels */
    private final int viewportWidth;

    /** Height of the viewport in pixels */
    private final int viewportHeight;

    /** Current x-offset in pixels */
    private int x;

    /** Current y-offset in pixels */
    private int y;

    /** Current zoom scale (1.0 = 100%) */
    private float scale;

    /** Bounds of the skill tree content in world space */
    private Bounds2i bounds;

    /** Minimum allowed zoom scale */
    private float minScale;

    /** Maximum allowed zoom scale */
    private float maxScale;

    // ==================== Constructor ====================

    /**
     * Creates a new viewport state.
     *
     * @param viewportWidth the viewport width in pixels
     * @param viewportHeight the viewport height in pixels
     * @throws IllegalArgumentException if dimensions are not positive
     */
    public ViewportState(int viewportWidth, int viewportHeight) {
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            throw new IllegalArgumentException(
                    "Viewport dimensions must be positive: " + viewportWidth + "x" + viewportHeight
            );
        }

        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.x = 0;
        this.y = 0;
        this.scale = 1.0f;
        this.bounds = Bounds2i.zero();
        this.minScale = 0.75f;
        this.maxScale = 2.0f;
    }

    // ==================== Bounds Management ====================

    /**
     * Sets the content bounds and recalculates zoom constraints.
     * <p>
     * This should be called whenever the skill tree content changes.
     * Automatically extends bounds to ensure content fits in viewport
     * and calculates appropriate min/max zoom levels.
     *
     * @param contentBounds the bounds of all skills in world space
     */
    public void setBounds(Bounds2i contentBounds) {
        if (contentBounds == null) {
            this.bounds = Bounds2i.zero();
            return;
        }

        // Create a copy and add padding
        this.bounds = contentBounds;
        this.bounds.grow(CONTENT_GROW);

        // Extend bounds to accommodate viewport aspect ratio
        int halfContentWidth = MathHelper.ceilDiv(
                this.bounds.height() * viewportWidth,
                viewportHeight * 2
        );
        int halfContentHeight = MathHelper.ceilDiv(
                this.bounds.width() * viewportHeight,
                viewportWidth * 2
        );

        this.bounds.extend(new Vector2i(-halfContentWidth, -halfContentHeight));
        this.bounds.extend(new Vector2i(halfContentWidth, halfContentHeight));

        // Calculate zoom limits based on bounds
        this.minScale = Math.max(
                (float) viewportWidth / this.bounds.width(),
                (float) viewportHeight / this.bounds.height()
        ) * 0.75f;

        this.maxScale = 2.0f;

        // Clamp current state to new bounds
        applyChanges(this.x, this.y, this.scale);
    }

    // ==================== Position Control ====================

    /**
     * Sets the viewport position and scale with automatic clamping.
     * <p>
     * Ensures the viewport stays within valid bounds and zoom levels.
     *
     * @param x the new x-offset in pixels
     * @param y the new y-offset in pixels
     * @param scale the new zoom scale
     */
    public void setPosition(int x, int y, float scale) {
        applyChanges(x, y, scale);
    }

    /**
     * Applies a pan offset to the current position.
     *
     * @param deltaX the horizontal movement in pixels
     * @param deltaY the vertical movement in pixels
     */
    public void pan(int deltaX, int deltaY) {
        applyChanges(this.x + deltaX, this.y + deltaY, this.scale);
    }

    /**
     * Applies zoom centered on a specific point in viewport space.
     * <p>
     * Zooms while keeping the point under the mouse stationary,
     * creating an intuitive zoom-to-cursor effect.
     *
     * @param viewportMouseX mouse x-coordinate relative to viewport origin
     * @param viewportMouseY mouse y-coordinate relative to viewport origin
     * @param scrollAmount scroll wheel delta (positive = zoom in)
     */
    public void applyZoom(double viewportMouseX, double viewportMouseY, double scrollAmount) {
        float factor = (float) Math.pow(2, scrollAmount * SCROLL_SENSITIVITY);
        float newScale = MathHelper.clamp(this.scale * factor, minScale, maxScale);
        float actualFactor = newScale / this.scale;

        int newX = x - (int) Math.round(
                (actualFactor - 1f) * (viewportMouseX - x - viewportWidth / 2f)
        );
        int newY = y - (int) Math.round(
                (actualFactor - 1f) * (viewportMouseY - y - viewportHeight / 2f)
        );

        applyChanges(newX, newY, newScale);
    }

    /**
     * Applies and clamps view changes for pan and zoom operations.
     * <p>
     * Ensures the viewport stays within valid bounds and zoom levels.
     * Allows some dragging beyond viewport edges for better UX.
     *
     * @param newX the desired x-offset
     * @param newY the desired y-offset
     * @param newScale the desired zoom scale
     */
    private void applyChanges(int newX, int newY, float newScale) {
        if (bounds.width() <= 0 || bounds.height() <= 0) {
            this.x = 0;
            this.y = 0;
            this.scale = MathHelper.clamp(newScale, minScale, maxScale);
            return;
        }

        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;

        // Clamp scale first
        newScale = MathHelper.clamp(newScale, minScale, maxScale);

        // Calculate the actual content size at this scale
        int scaledWidth = (int) (bounds.width() * newScale);
        int scaledHeight = (int) (bounds.height() * newScale);

        // Calculate position constraints
        int minX, maxX, minY, maxY;

        if (scaledWidth < viewportWidth) {
            // Content smaller than viewport - allow drag padding
            int centerOffset = (viewportWidth - scaledWidth) / 2;
            minX = -centerOffset - DRAG_PADDING;
            maxX = centerOffset + DRAG_PADDING;
        } else {
            // Content larger than viewport - constrain to edges
            minX = (int) Math.ceil(halfWidth - bounds.max().x() * newScale);
            maxX = (int) Math.floor(-halfWidth - bounds.min().x() * newScale);
        }

        if (scaledHeight < viewportHeight) {
            // Content smaller than viewport - allow drag padding
            int centerOffset = (viewportHeight - scaledHeight) / 2;
            minY = -centerOffset - DRAG_PADDING;
            maxY = centerOffset + DRAG_PADDING;
        } else {
            // Content larger than viewport - constrain to edges
            minY = (int) Math.ceil(halfHeight - bounds.max().y() * newScale);
            maxY = (int) Math.floor(-halfHeight - bounds.min().y() * newScale);
        }

        // Apply clamped values
        this.x = MathHelper.clamp(newX, minX, maxX);
        this.y = MathHelper.clamp(newY, minY, maxY);
        this.scale = newScale;
    }

    // ==================== Coordinate Transformation ====================

    /**
     * Transforms a point from viewport space to world space.
     * <p>
     * Useful for hit testing - converting mouse coordinates to
     * skill positions in the tree.
     *
     * @param viewportX x-coordinate relative to viewport origin
     * @param viewportY y-coordinate relative to viewport origin
     * @return the position in world space
     */
    public Vector2i viewportToWorld(double viewportX, double viewportY) {
        return new Vector2i(
                (int) Math.round((viewportX - x - viewportWidth / 2.0) / scale),
                (int) Math.round((viewportY - y - viewportHeight / 2.0) / scale)
        );
    }

    /**
     * Transforms a point from world space to viewport space.
     * <p>
     * Useful for rendering - converting skill positions to
     * screen coordinates.
     *
     * @param worldX x-coordinate in world space
     * @param worldY y-coordinate in world space
     * @return the position in viewport space
     */
    public Vector2i worldToViewport(int worldX, int worldY) {
        return new Vector2i(
                (int) Math.round(worldX * scale + x + viewportWidth / 2.0),
                (int) Math.round(worldY * scale + y + viewportHeight / 2.0)
        );
    }

    // ==================== Getters ====================

    /**
     * Gets the current x-offset in pixels.
     *
     * @return the x-offset
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the current y-offset in pixels.
     *
     * @return the y-offset
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the current zoom scale.
     *
     * @return the scale (1.0 = 100%)
     */
    public float getScale() {
        return scale;
    }

    /**
     * Gets the viewport width in pixels.
     *
     * @return the width
     */
    public int getViewportWidth() {
        return viewportWidth;
    }

    /**
     * Gets the viewport height in pixels.
     *
     * @return the height
     */
    public int getViewportHeight() {
        return viewportHeight;
    }

    /**
     * Gets the content bounds in world space.
     *
     * @return the bounds
     */
    public Bounds2i getBounds() {
        return bounds;
    }

    /**
     * Gets the minimum allowed zoom scale.
     *
     * @return the minimum scale
     */
    public float getMinScale() {
        return minScale;
    }

    /**
     * Gets the maximum allowed zoom scale.
     *
     * @return the maximum scale
     */
    public float getMaxScale() {
        return maxScale;
    }

    // ==================== State Inspection ====================

    /**
     * Checks if the viewport has valid bounds configured.
     *
     * @return {@code true} if bounds are valid, {@code false} otherwise
     */
    public boolean hasBounds() {
        return bounds != null && bounds.width() > 0 && bounds.height() > 0;
    }

    /**
     * Creates a copy of this viewport state.
     *
     * @return a new ViewportState with the same values
     */
    public ViewportState copy() {
        ViewportState copy = new ViewportState(viewportWidth, viewportHeight);
        copy.x = this.x;
        copy.y = this.y;
        copy.scale = this.scale;
        copy.bounds = this.bounds;
        copy.minScale = this.minScale;
        copy.maxScale = this.maxScale;
        return copy;
    }

    @Override
    public String toString() {
        return String.format(
                "ViewportState[x=%d, y=%d, scale=%.2f, bounds=%s, viewport=%dx%d]",
                x, y, scale, bounds, viewportWidth, viewportHeight
        );
    }
}