package de.one_piece_api.gui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Custom button widget with texture support and tooltip functionality.
 * <p>
 * This button implementation provides:
 * <ul>
 *     <li>Position relative to a parent container</li>
 *     <li>Optional texture-based rendering</li>
 *     <li>Hover and disabled state visual feedback</li>
 *     <li>Separate tooltips for enabled and disabled states</li>
 *     <li>Click sound effects</li>
 *     <li>Centered text rendering</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * CustomButton button = new CustomButton(
 *     10, 10, 100, 20,
 *     Text.literal("Click Me"),
 *     textures,
 *     () -> System.out.println("Clicked!")
 * );
 * button.render(context, textRenderer, mouseX, mouseY, containerX, containerY);
 * button.handleClick(mouseX, mouseY, button, containerX, containerY);
 * }</pre>
 *
 * @see ButtonTextures
 */
public class CustomButton {
    /** Horizontal position relative to parent container */
    private final int relativeX;

    /** Vertical position relative to parent container */
    private final int relativeY;

    /** Width of the button in pixels */
    private final int width;

    /** Height of the button in pixels */
    private final int height;

    /** Text to display on the button */
    private final Text text;

    /** Textures for different button states (enabled/disabled, hovered/not hovered) */
    private final ButtonTextures textures;

    /** Action to execute when button is clicked */
    private final Runnable onClick;

    /** Tooltip lines to display when button is enabled and hovered */
    private final List<Text> tooltip;

    /** Tooltip lines to display when button is disabled and hovered */
    private final List<Text> disabledTooltip;

    /** Whether the button is currently enabled (clickable) */
    private boolean enabled = true;

    /** Whether the mouse is currently hovering over the button */
    private boolean hovered = false;

    /**
     * Creates a button with textures but no tooltips.
     *
     * @param relativeX x-coordinate relative to parent container
     * @param relativeY y-coordinate relative to parent container
     * @param width button width in pixels
     * @param height button height in pixels
     * @param text text to display on the button
     * @param textures textures for button states
     * @param onClick action to execute on click
     */
    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, ButtonTextures textures, Runnable onClick) {
        this(relativeX, relativeY, width, height, text, textures, onClick, null, null);
    }

    /**
     * Creates a button with textures and enabled tooltip.
     *
     * @param relativeX x-coordinate relative to parent container
     * @param relativeY y-coordinate relative to parent container
     * @param width button width in pixels
     * @param height button height in pixels
     * @param text text to display on the button
     * @param textures textures for button states
     * @param onClick action to execute on click
     * @param tooltip tooltip lines to show when enabled
     */
    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, ButtonTextures textures, Runnable onClick, List<Text> tooltip) {
        this(relativeX, relativeY, width, height, text, textures, onClick, tooltip, null);
    }

    /**
     * Creates a button with textures and both enabled and disabled tooltips.
     *
     * @param relativeX x-coordinate relative to parent container
     * @param relativeY y-coordinate relative to parent container
     * @param width button width in pixels
     * @param height button height in pixels
     * @param text text to display on the button
     * @param textures textures for button states, or {@code null} for solid color rendering
     * @param onClick action to execute on click
     * @param tooltip tooltip lines to show when enabled, or {@code null} for no tooltip
     * @param disabledTooltip tooltip lines to show when disabled, or {@code null} for no tooltip
     */
    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, ButtonTextures textures, Runnable onClick, List<Text> tooltip, List<Text> disabledTooltip) {
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.width = width;
        this.height = height;
        this.text = text;
        this.textures = textures;
        this.onClick = onClick;
        this.tooltip = tooltip;
        this.disabledTooltip = disabledTooltip;
    }

    /**
     * Creates a simple button without textures or tooltips.
     * <p>
     * The button will be rendered as a solid color rectangle that changes
     * shade based on hover and enabled state.
     *
     * @param relativeX x-coordinate relative to parent container
     * @param relativeY y-coordinate relative to parent container
     * @param width button width in pixels
     * @param height button height in pixels
     * @param text text to display on the button
     * @param onClick action to execute on click
     */
    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, Runnable onClick) {
        this(relativeX, relativeY, width, height, text, null, onClick, null, null);
    }

    /**
     * Creates a button without textures but with tooltips.
     * <p>
     * The button will be rendered as a solid color rectangle with
     * tooltip support for both enabled and disabled states.
     *
     * @param relativeX x-coordinate relative to parent container
     * @param relativeY y-coordinate relative to parent container
     * @param width button width in pixels
     * @param height button height in pixels
     * @param text text to display on the button
     * @param onClick action to execute on click
     * @param tooltip tooltip lines to show when enabled, or {@code null} for no tooltip
     * @param disabledTooltip tooltip lines to show when disabled, or {@code null} for no tooltip
     */
    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, Runnable onClick, List<Text> tooltip, List<Text> disabledTooltip) {
        this(relativeX, relativeY, width, height, text, null, onClick, tooltip, disabledTooltip);
    }

    /**
     * Renders the button including background and text.
     * <p>
     * The button is rendered using either textures (if provided) or solid colors.
     * Text is automatically centered within the button bounds. Updates hover state
     * based on mouse position.
     *
     * @param context the drawing context
     * @param textRenderer the text renderer for drawing button text
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param backgroundX the x-coordinate of the parent container
     * @param backgroundY the y-coordinate of the parent container
     */
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int backgroundX, int backgroundY) {
        int x = backgroundX + relativeX;
        int y = backgroundY + relativeY;

        this.hovered = isMouseOver(mouseX, mouseY, x, y);

        if (textures != null) {
            Identifier texture = textures.get(enabled, hovered);
            context.drawTexture(texture, x, y, 0, 0, width, height, width, height);
        } else {
            int color = getButtonColor();
            context.fill(x, y, x + width, y + height, color);
        }

        // Render centered text
        int textX = x + (width - textRenderer.getWidth(text)) / 2;
        int textY = y + (height - textRenderer.fontHeight) / 2;
        int textColor = enabled ? 0xFFFFFF : 0x999999;

        context.drawText(textRenderer, text, textX, textY, textColor, false);
    }

    /**
     * Renders the button's tooltip if the mouse is hovering over it.
     * <p>
     * Displays the enabled tooltip when the button is enabled and hovered,
     * or the disabled tooltip when the button is disabled and hovered.
     * Should be called after {@link #render} to ensure tooltips appear on top.
     *
     * @param context the drawing context
     * @param textRenderer the text renderer for drawing tooltip text
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param backgroundX the x-coordinate of the parent container
     * @param backgroundY the y-coordinate of the parent container
     */
    public void renderTooltip(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int backgroundX, int backgroundY) {
        if (hovered) {
            if (enabled && tooltip != null) {
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            } else if (!enabled && disabledTooltip != null) {
                context.drawTooltip(textRenderer, disabledTooltip, mouseX, mouseY);
            }
        }
    }

    /**
     * Handles mouse click events on the button.
     * <p>
     * If the button is enabled and the left mouse button is clicked within
     * the button bounds, plays a click sound and executes the onClick action.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @param backgroundX the x-coordinate of the parent container
     * @param backgroundY the y-coordinate of the parent container
     * @return {@code true} if the button was clicked, {@code false} otherwise
     */
    public boolean handleClick(double mouseX, double mouseY, int button, int backgroundX, int backgroundY) {
        int x = backgroundX + relativeX;
        int y = backgroundY + relativeY;

        if (enabled && button == 0 && isMouseOver(mouseX, mouseY, x, y)) {
            // Play button click sound
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            if (onClick != null) {
                onClick.run();
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if the mouse is over the button.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param x the button's absolute x-coordinate
     * @param y the button's absolute y-coordinate
     * @return {@code true} if mouse is within button bounds, {@code false} otherwise
     */
    private boolean isMouseOver(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * Gets the background color for the button based on its state.
     * <p>
     * Used when no textures are provided. Returns different colors for:
     * <ul>
     *     <li>Disabled: Dark gray (#333333)</li>
     *     <li>Hovered: Medium gray (#666666)</li>
     *     <li>Normal: Default gray (#444444)</li>
     * </ul>
     *
     * @return the ARGB color value
     */
    private int getButtonColor() {
        if (!enabled) {
            return 0xFF333333;
        } else if (hovered) {
            return 0xFF666666;
        } else {
            return 0xFF444444;
        }
    }

    /**
     * Sets whether the button is enabled.
     * <p>
     * Disabled buttons cannot be clicked and display visual feedback
     * (grayed out appearance and different tooltip).
     *
     * @param enabled {@code true} to enable the button, {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if the button is currently enabled.
     *
     * @return {@code true} if enabled, {@code false} if disabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if the mouse is currently hovering over the button.
     *
     * @return {@code true} if hovered, {@code false} otherwise
     */
    public boolean isHovered() {
        return hovered;
    }

    /**
     * Gets the button width.
     *
     * @return the width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the button height.
     *
     * @return the height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the relative x-coordinate.
     *
     * @return the x-coordinate relative to parent container
     */
    public int getRelativeX() {
        return relativeX;
    }

    /**
     * Gets the relative y-coordinate.
     *
     * @return the y-coordinate relative to parent container
     */
    public int getRelativeY() {
        return relativeY;
    }
}