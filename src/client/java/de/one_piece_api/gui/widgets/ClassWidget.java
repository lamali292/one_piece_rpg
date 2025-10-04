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
 * Widget representing a selectable character class in the UI.
 * Handles rendering and interaction for a single class option with smooth scaling.
 */
public class ClassWidget implements Drawable, Element {

    private static final Identifier SELECT_RECT = OnePieceRPG.id("textures/gui/class/select_rect.png");
    private static final Identifier SELECT_TEXT = OnePieceRPG.id("textures/gui/class/select_text.png");

    // Base dimensions at reference scale
    private static final float BASE_WIDTH = 500;
    private static final float BASE_HEIGHT = 720;
    private static final float BASE_NAME_DIM = 180;
    private static final float NAME_BOTTOM_MARGIN = 10;

    private static final float SELECT_RECT_WIDTH = 313;
    private static final float SELECT_RECT_HEIGHT = 90;
    private static final float SELECT_TEXT_WIDTH = 214;
    private static final float SELECT_TEXT_HEIGHT = 35;

    // Animation constants
    private static final float SCALE_SPEED = 0.2f; // Interpolation speed per second
    private static final float TARGET_SCALE_HOVERED = 1.1f;
    private static final float TARGET_SCALE_NORMAL = 1.0f;

    // Immutable data
    private final Identifier classId;
    private final ClassConfig config;
    private final float relativeOffset;
    private final Consumer<Identifier> onClickCallback;

    // Layout state
    private final WidgetBounds bounds;

    // Rendering state
    private boolean isHovered = false;
    private boolean anyHovered = false;

    // Animation state
    private float currentScale = 1.0f;
    private float targetScale = 1.0f;

    /**
     * Creates a new class widget.
     *
     * @param classId The identifier for this class
     * @param config Configuration containing textures and metadata
     * @param relativeOffset Horizontal offset as fraction of container width (-1.0 to 1.0)
     * @param onClickCallback Callback invoked when widget is clicked
     */
    public ClassWidget(Identifier classId, ClassConfig config, float relativeOffset,
                       Consumer<Identifier> onClickCallback) {
        this.classId = classId;
        this.config = config;
        this.relativeOffset = relativeOffset;
        this.onClickCallback = onClickCallback;
        this.bounds = new WidgetBounds();
    }

    /**
     * Updates the widget's scale factor and recalculates dimensions.
     * Should be called when tab is resized.
     *
     * @param scale The new scale factor
     * @param containerWidth The width of the container to offset within
     */
    public void updateLayout(float scale, int containerWidth) {
        this.bounds.update(scale, containerWidth, relativeOffset);
    }

    /**
     * Sets whether this widget should be rendered in color or grayscale.
     *
     * @param hovered true for color rendering, false for grayscale
     */
    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
        this.targetScale = hovered ? TARGET_SCALE_HOVERED : TARGET_SCALE_NORMAL;
    }

    public void setAnyHovered(boolean anyHovered) {
        this.anyHovered = anyHovered;
    }

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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            onClickCallback.accept(classId);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {
        // Not focusable via keyboard
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    public Identifier getClassId() {
        return classId;
    }

    /**
     * Internal class to encapsulate widget positioning and dimensions.
     * Reduces duplication and makes layout calculations explicit.
     */
    private static class WidgetBounds {
        float x, y;
        float width, height;
        float nameDim;
        float selectTextWidth, selectTextHeight;
        float selectRectWidth, selectRectHeight;

        private int containerWidth;
        private float relativeOffset;

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

        void updateScreenPosition() {
            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // Calculate horizontal position with offset
            this.x = ((screenWidth - width + relativeOffset * containerWidth) / 2.0f);
            this.y = ((screenHeight - height) / 2);
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width
                    && mouseY >= y && mouseY < y + height;
        }
    }
}