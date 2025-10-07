package de.one_piece_api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.util.TextureFramebufferCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

/**
 * Widget representing a selectable character class in the class selection UI.
 * <p>
 * This widget provides an interactive visual representation of a character class
 * with smooth animations and visual feedback. It features dynamic scaling on hover,
 * grayscale filtering for non-hovered states, and proper layout positioning.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Smooth scale animation on hover (1.0x to 1.1x)</li>
 *     <li>Grayscale effect for non-hovered classes when any class is hovered</li>
 *     <li>Delta-time-based animation for consistent speed across framerates</li>
 *     <li>Cached grayscale textures for performance</li>
 *     <li>Relative positioning within container for flexible layouts</li>
 *     <li>Click callback for class selection</li>
 * </ul>
 *
 * <h2>Visual Components:</h2>
 * <ul>
 *     <li>Background texture - Main class image</li>
 *     <li>Name badge - Class name overlay at bottom</li>
 *     <li>Selection frame - "SELECT" indicator (always colored)</li>
 * </ul>
 *
 * @see Drawable
 * @see Element
 * @see ClassConfig
 */
public class ClassWidget implements Drawable, Element {

    // ==================== Constants ====================

    /** Texture for the selection rectangle frame */
    private static final Identifier SELECT_RECT = OnePieceRPG.id("textures/gui/class/select_rect.png");

    /** Texture for the "SELECT" text overlay */
    private static final Identifier SELECT_TEXT = OnePieceRPG.id("textures/gui/class/select_text.png");

    // Base dimensions at reference scale (before scaling)
    /** Base width of the class widget at 1.0 scale */
    private static final float BASE_WIDTH = 500;

    /** Base height of the class widget at 1.0 scale */
    private static final float BASE_HEIGHT = 720;

    /** Base dimension (width/height) of the name badge at 1.0 scale */
    private static final float BASE_NAME_DIM = 180;

    /** Margin between name badge and bottom edge in pixels */
    private static final float NAME_BOTTOM_MARGIN = 10;

    /** Width of the selection rectangle at 1.0 scale */
    private static final float SELECT_RECT_WIDTH = 313;

    /** Height of the selection rectangle at 1.0 scale */
    private static final float SELECT_RECT_HEIGHT = 90;

    /** Width of the "SELECT" text at 1.0 scale */
    private static final float SELECT_TEXT_WIDTH = 214;

    /** Height of the "SELECT" text at 1.0 scale */
    private static final float SELECT_TEXT_HEIGHT = 35;

    // Animation constants
    /** Interpolation speed multiplier for scale animation */
    private static final float SCALE_SPEED = 0.2f;

    /** Target scale when widget is hovered */
    private static final float TARGET_SCALE_HOVERED = 1.1f;

    /** Target scale when widget is not hovered */
    private static final float TARGET_SCALE_NORMAL = 1.0f;

    // ==================== Immutable Data ====================

    /** The unique identifier for this character class */
    private final Identifier classId;

    /** Configuration containing textures and metadata for this class */
    private final ClassConfig config;

    /** Horizontal offset as fraction of container width (-1.0 to 1.0) */
    private final float relativeOffset;

    /** Callback invoked when the widget is clicked */
    private final Consumer<Identifier> onClickCallback;

    // Layout state
    /** Bounds calculator for positioning and hit detection */
    private final WidgetBounds bounds;

    // Rendering state
    /** Whether the mouse is currently hovering over this widget */
    private boolean isHovered = false;

    /** Whether any widget in the container is being hovered */
    private boolean anyHovered = false;

    // Animation state
    /** Current scale value being rendered (interpolated) */
    private float currentScale = 1.0f;

    /** Target scale value to animate towards */
    private float targetScale = 1.0f;

    // ==================== Constructor ====================

    /**
     * Creates a new class widget.
     *
     * @param classId the unique identifier for this character class
     * @param config configuration containing textures and metadata
     * @param relativeOffset horizontal offset as fraction of container width (-1.0 to 1.0)
     * @param onClickCallback callback invoked when widget is clicked
     */
    public ClassWidget(Identifier classId, ClassConfig config, float relativeOffset,
                       Consumer<Identifier> onClickCallback) {
        this.classId = classId;
        this.config = config;
        this.relativeOffset = relativeOffset;
        this.onClickCallback = onClickCallback;
        this.bounds = new WidgetBounds();
    }

    // ==================== Layout Management ====================

    /**
     * Updates the widget's scale factor and recalculates dimensions.
     * <p>
     * This method should be called when the screen is resized or when
     * the layout scale changes. It updates all dimension calculations
     * but does not recalculate screen position (that happens per-frame).
     *
     * @param scale the new scale factor to apply to base dimensions
     * @param containerWidth the width of the container for relative positioning
     */
    public void updateLayout(float scale, int containerWidth) {
        this.bounds.update(scale, containerWidth, relativeOffset);
    }

    // ==================== State Management ====================

    /**
     * Sets whether this widget is currently hovered.
     * <p>
     * When hovered, the widget renders in full color and scales up.
     * When not hovered (but {@link #anyHovered} is true), it renders
     * in grayscale at normal scale.
     *
     * @param hovered {@code true} for color rendering and scale-up, {@code false} otherwise
     */
    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
        this.targetScale = hovered ? TARGET_SCALE_HOVERED : TARGET_SCALE_NORMAL;
    }

    /**
     * Sets whether any widget in the container is being hovered.
     * <p>
     * This is used to determine when to apply grayscale filtering.
     * When {@code true}, non-hovered widgets render in grayscale.
     * When {@code false}, all widgets render in color.
     *
     * @param anyHovered {@code true} if any widget is hovered, {@code false} otherwise
     */
    public void setAnyHovered(boolean anyHovered) {
        this.anyHovered = anyHovered;
    }

    // ==================== Rendering ====================

    /**
     * Renders the class widget with smooth animations and visual effects.
     * <p>
     * Rendering pipeline:
     * <ol>
     *     <li>Update screen position based on current window size</li>
     *     <li>Animate scale towards target using delta-time interpolation</li>
     *     <li>Calculate scaled dimensions and offsets</li>
     *     <li>Render background (grayscale or color based on hover state)</li>
     *     <li>Render name badge at bottom</li>
     *     <li>Render selection frame and text (always colored)</li>
     * </ol>
     *
     * Animation uses exponential interpolation with delta time for smooth,
     * frame-rate-independent scaling.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time in seconds
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        bounds.updateScreenPosition();

        // Smoothly interpolate current scale towards target scale using delta time
        float lerpFactor = 1.0f - (float) Math.pow(0.001, delta * SCALE_SPEED);
        currentScale = MathHelper.lerp(lerpFactor, currentScale, targetScale);

        // Snap to target if very close to avoid floating point drift
        if (Math.abs(currentScale - targetScale) < 0.001f) {
            currentScale = targetScale;
        }

        int scaledWidth = (int) (bounds.width * currentScale);
        int scaledHeight = (int) (bounds.height * currentScale);
        float scaledOffsetX = bounds.width * (currentScale - 1) / 2;
        float scaledOffsetY = bounds.height * (currentScale - 1) / 2;

        int scaledNameDim = (int) (bounds.nameDim * currentScale);
        int scaledSelectRectWidth = (int) (bounds.selectRectWidth * currentScale);
        int scaledSelectRectHeight = (int) (bounds.selectRectHeight * currentScale);
        int scaledSelectTextWidth = (int) (bounds.selectTextWidth * currentScale);
        int scaledSelectTextHeight = (int) (bounds.selectTextHeight * currentScale);

        // Determine which texture to use (cached grayscale or original)
        Identifier backTexture = isHovered || !anyHovered ?
                config.backTexture() :
                TextureFramebufferCache.getGrayscaleTexture(config.backTexture());

        // Draw background
        RenderSystem.enableBlend();
        context.drawTexture(
                backTexture,
                (int) (bounds.x - scaledOffsetX),
                (int) (bounds.y - scaledOffsetY),
                0f, 0f,
                scaledWidth, scaledHeight,
                scaledWidth, scaledHeight
        );
        RenderSystem.disableBlend();

        // Determine which name texture to use
        Identifier nameTexture = config.nameTexture();

        // Draw name badge
        int nameY = (int) (bounds.y + bounds.height - scaledNameDim - NAME_BOTTOM_MARGIN + scaledOffsetY);
        RenderSystem.enableBlend();
        context.drawTexture(
                nameTexture,
                (int) (bounds.x - scaledOffsetX),
                nameY,
                0, 0,
                scaledNameDim, scaledNameDim,
                scaledNameDim, scaledNameDim
        );
        RenderSystem.disableBlend();

        // Draw selection rectangle and text (always colored)
        // Position rect first, then position text relative to rect to maintain alignment
        float rectXBase = 15;
        float rectYBase = bounds.height - 12;

        float rectXOffset = rectXBase * currentScale;
        float rectYOffset = rectYBase * currentScale;

        float rectX = (bounds.x + rectXOffset - scaledOffsetX);
        float rectY = (bounds.y + rectYOffset - scaledOffsetY);

        // Text is positioned 10 pixels to the right and 5 pixels up from rect (in base coordinates)
        // These offsets should also scale
        float textOffsetFromRectX = 10 * currentScale;
        float textOffsetFromRectY = 5 * currentScale;

        int textX = (int) (rectX + textOffsetFromRectX);
        int textY = (int) (rectY + textOffsetFromRectY);

        RenderSystem.enableBlend();
        context.drawTexture(
                SELECT_RECT,
                (int) rectX, (int) rectY,
                0, 0,
                scaledSelectRectWidth, scaledSelectRectHeight,
                scaledSelectRectWidth, scaledSelectRectHeight
        );
        context.drawTexture(
                SELECT_TEXT,
                textX, textY,
                0, 0,
                scaledSelectTextWidth, scaledSelectTextHeight,
                scaledSelectTextWidth, scaledSelectTextHeight
        );
        RenderSystem.disableBlend();
    }

    // ==================== Input Handling ====================

    /**
     * Handles mouse click events.
     * <p>
     * When the widget is left-clicked, invokes the callback with this
     * widget's class ID.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            onClickCallback.accept(classId);
            return true;
        }
        return false;
    }

    /**
     * Checks if the mouse is over this widget.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if mouse is within widget bounds, {@code false} otherwise
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY);
    }

    // ==================== Element Interface ====================

    /**
     * Sets focus state for this widget.
     * <p>
     * This widget is not focusable via keyboard, so this method does nothing.
     *
     * @param focused the focus state (ignored)
     */
    @Override
    public void setFocused(boolean focused) {
        // Not focusable via keyboard
    }

    /**
     * Checks if this widget has focus.
     * <p>
     * This widget is not focusable via keyboard.
     *
     * @return {@code false} always
     */
    @Override
    public boolean isFocused() {
        return false;
    }

    // ==================== Getters ====================

    /**
     * Gets the class identifier for this widget.
     *
     * @return the class identifier
     */
    public Identifier getClassId() {
        return classId;
    }

    // ==================== Inner Classes ====================

    /**
     * Internal class to encapsulate widget positioning and dimensions.
     * <p>
     * This class handles all layout calculations including scaling, relative
     * positioning within the container, and screen-space coordinate conversion.
     * Separating this logic reduces duplication and makes layout calculations
     * more explicit and testable.
     */
    private static class WidgetBounds {
        /** Current x-coordinate in screen space */
        float x, y;

        /** Current width and height after scaling */
        float width, height;

        /** Current dimension of the name badge after scaling */
        float nameDim;

        /** Current dimensions of the selection text after scaling */
        float selectTextWidth, selectTextHeight;

        /** Current dimensions of the selection rectangle after scaling */
        float selectRectWidth, selectRectHeight;

        /** Container width for relative positioning */
        private int containerWidth;

        /** Horizontal offset as fraction of container width */
        private float relativeOffset;

        /**
         * Updates the scaled dimensions based on new scale and container size.
         * <p>
         * Applies the scale factor to all base dimensions and stores the
         * container parameters for later position calculation.
         *
         * @param scale the scale factor to apply
         * @param containerWidth the container width for relative positioning
         * @param relativeOffset the relative horizontal offset (-1.0 to 1.0)
         */
        void update(float scale, int containerWidth, float relativeOffset) {
            this.width = (scale * BASE_WIDTH);
            this.height = (scale * BASE_HEIGHT);
            this.nameDim = (scale * BASE_NAME_DIM);
            this.selectTextWidth = (scale * SELECT_TEXT_WIDTH);
            this.selectTextHeight = (scale * SELECT_TEXT_HEIGHT);
            this.selectRectWidth = (scale * SELECT_RECT_WIDTH);
            this.selectRectHeight = (scale * SELECT_RECT_HEIGHT);
            this.containerWidth = containerWidth;
            this.relativeOffset = relativeOffset;
        }

        /**
         * Updates the screen-space position based on current window size.
         * <p>
         * Calculates centered position with horizontal offset based on
         * the relative offset parameter. Should be called each frame to
         * handle window resizing.
         */
        void updateScreenPosition() {
            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // Calculate horizontal position with offset
            this.x = ((screenWidth - width + relativeOffset * containerWidth) / 2.0f);
            this.y = ((screenHeight - height) / 2);
        }

        /**
         * Checks if a point is within the widget bounds.
         *
         * @param mouseX the x-coordinate to check
         * @param mouseY the y-coordinate to check
         * @return {@code true} if the point is within bounds, {@code false} otherwise
         */
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width
                    && mouseY >= y && mouseY < y + height;
        }
    }
}