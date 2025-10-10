package de.one_piece_api.screen.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

/**
 * Interface for tab content in a tabbed screen interface.
 * <p>
 * This interface combines {@link Drawable} and {@link Element} to provide
 * a complete tab implementation that can render content and handle user input.
 * Tabs are managed by {@link GenericTabbedScreen} and receive events when active.
 *
 * <h2>Implementation Requirements:</h2>
 * <ul>
 *     <li>{@link #render} - Render the tab's visual content</li>
 *     <li>{@link #resize} - Handle screen size changes</li>
 *     <li>Input methods - Handle mouse and keyboard events (optional)</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * public class MyTab implements Tab {
 *     @Override
 *     public void render(DrawContext context, int mouseX, int mouseY, float delta) {
 *         // Render tab content
 *     }
 *
 *     @Override
 *     public void resize(MinecraftClient client, int width, int height) {
 *         // Recalculate layout
 *     }
 *
 *     @Override
 *     public boolean mouseClicked(double mouseX, double mouseY, int button) {
 *         // Handle clicks
 *         return false;
 *     }
 * }
 * }</pre>
 *
 * @see GenericTabbedScreen
 * @see Drawable
 * @see Element
 */
public interface Tab extends Drawable, Element {

    /**
     * Sets whether this tab has input focus.
     * <p>
     * Default implementation does nothing as tabs typically don't require
     * explicit focus management - they receive events when they are the
     * active tab in the parent screen.
     *
     * @param focused {@code true} to set focus, {@code false} to remove it
     */
    @Override
    default void setFocused(boolean focused) {

    }

    /**
     * Checks if this tab currently has input focus.
     * <p>
     * Default implementation always returns {@code false} as tabs typically
     * handle focus through their active state in the parent screen rather
     * than explicit focus tracking.
     *
     * @return {@code false} by default
     */
    @Override
    default boolean isFocused() {
        return false;
    }

    /**
     * Handles screen resize events.
     * <p>
     * Called when the game window is resized or when the tab is first initialized.
     * Implementations should recalculate layout positions, update widget bounds,
     * and adjust any size-dependent rendering parameters.
     *
     * <h3>Typical Implementation:</h3>
     * <pre>{@code
     * @Override
     * public void resize(MinecraftClient client, int width, int height) {
     *     this.screenWidth = width;
     *     this.screenHeight = height;
     *     recalculateLayout();
     *     updateWidgetPositions();
     * }
     * }</pre>
     *
     * @param client the Minecraft client instance
     * @param width the new screen width in scaled pixels
     * @param height the new screen height in scaled pixels
     */
    void resize(MinecraftClient client, int width, int height);
}