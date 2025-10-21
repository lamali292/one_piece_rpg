package de.one_piece_api.screen.widget.main.devilfruit;

import net.minecraft.util.math.MathHelper;

/**
 * Manages vertical scroll state for the devil fruit skill paths.
 * <p>
 * Encapsulates scroll offset and limits with automatic clamping to keep
 * the viewport within valid bounds. This separates scroll logic from
 * rendering and input handling.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * ScrollState state = new ScrollState();
 * state.setLimits(maxUp, maxDown);
 * state.scroll(deltaY);
 * int offset = state.getOffset();
 * }</pre>
 */
public class ScrollState {

    // ==================== Constants ====================

    /** Number of pixels to scroll per mouse wheel tick */
    private static final int SCROLL_SPEED = 20;

    // ==================== Fields ====================

    /** Current vertical scroll offset in pixels */
    private int offset;

    /** Maximum scroll distance upward (positive value) */
    private int maxScrollUp;

    /** Maximum scroll distance downward (negative value) */
    private int maxScrollDown;

    // ==================== Constructor ====================

    /**
     * Creates a new scroll state with no scrolling.
     */
    public ScrollState() {
        this.offset = 0;
        this.maxScrollUp = 0;
        this.maxScrollDown = 0;
    }

    // ==================== Limits Management ====================

    /**
     * Sets the scroll limits.
     * <p>
     * The limits define how far the content can scroll in each direction.
     * After setting limits, the current offset is clamped to remain valid.
     *
     * @param maxUp maximum scroll distance upward (positive value)
     * @param maxDown maximum scroll distance downward (negative value)
     */
    public void setLimits(int maxUp, int maxDown) {
        this.maxScrollUp = Math.max(0, maxUp);
        this.maxScrollDown = Math.min(0, maxDown);
        clampOffset();
    }

    /**
     * Calculates scroll limits based on content length and spacing.
     * <p>
     * For a vertical list of items:
     * <ul>
     *     <li>Max scroll up: last item centered</li>
     *     <li>Max scroll down: first item centered</li>
     * </ul>
     *
     * @param itemCount the number of items in the list
     * @param itemSpacing the spacing between items in pixels
     */
    public void calculateLimits(int itemCount, int itemSpacing) {
        if (itemCount <= 1) {
            this.maxScrollUp = 0;
            this.maxScrollDown = 0;
            this.offset = 0;
            return;
        }

        // For max scroll down: first item (index 0) at center
        this.maxScrollDown = -(itemCount - 1) * itemSpacing;

        // For max scroll up: last item at center
        this.maxScrollUp = (itemCount - 1) * itemSpacing;

        clampOffset();
    }

    // ==================== Scrolling ====================

    /**
     * Scrolls by a specific amount with automatic clamping.
     *
     * @param delta the scroll delta in pixels (positive = up, negative = down)
     */
    public void scroll(int delta) {
        this.offset = MathHelper.clamp(
                this.offset + delta,
                maxScrollDown,
                maxScrollUp
        );
    }

    /**
     * Scrolls by mouse wheel amount.
     * <p>
     * Applies the scroll speed multiplier to the wheel delta.
     *
     * @param wheelDelta the mouse wheel delta (positive = up, negative = down)
     */
    public void scrollByWheel(double wheelDelta) {
        scroll((int) (wheelDelta * SCROLL_SPEED));
    }

    /**
     * Sets the scroll offset directly with automatic clamping.
     *
     * @param newOffset the new offset in pixels
     */
    public void setOffset(int newOffset) {
        this.offset = MathHelper.clamp(newOffset, maxScrollDown, maxScrollUp);
    }

    /**
     * Resets scroll to the default position (offset 0).
     */
    public void reset() {
        this.offset = 0;
    }

    // ==================== Helper Methods ====================

    /**
     * Clamps the current offset to within the scroll limits.
     */
    private void clampOffset() {
        this.offset = MathHelper.clamp(this.offset, maxScrollDown, maxScrollUp);
    }

    // ==================== Getters ====================

    /**
     * Gets the current scroll offset.
     *
     * @return the offset in pixels
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the maximum upward scroll distance.
     *
     * @return the maximum upward scroll (positive value)
     */
    public int getMaxScrollUp() {
        return maxScrollUp;
    }

    /**
     * Gets the maximum downward scroll distance.
     *
     * @return the maximum downward scroll (negative value)
     */
    public int getMaxScrollDown() {
        return maxScrollDown;
    }

    /**
     * Checks if scrolling is currently possible.
     *
     * @return {@code true} if limits allow scrolling, {@code false} otherwise
     */
    public boolean canScroll() {
        return maxScrollUp > 0 || maxScrollDown < 0;
    }

    /**
     * Checks if currently scrolled to the top.
     *
     * @return {@code true} if at maximum upward scroll, {@code false} otherwise
     */
    public boolean isAtTop() {
        return offset >= maxScrollUp;
    }

    /**
     * Checks if currently scrolled to the bottom.
     *
     * @return {@code true} if at maximum downward scroll, {@code false} otherwise
     */
    public boolean isAtBottom() {
        return offset <= maxScrollDown;
    }

    @Override
    public String toString() {
        return String.format(
                "ScrollState[offset=%d, limits=[%d, %d]]",
                offset, maxScrollDown, maxScrollUp
        );
    }
}