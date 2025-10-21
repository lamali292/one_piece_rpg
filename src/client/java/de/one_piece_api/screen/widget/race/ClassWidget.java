package de.one_piece_api.screen.widget.race;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.render.TextureFramebufferCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

/**
 * Widget representing a selectable character class in the class selection UI.
 */
public class ClassWidget implements Drawable, Element {

    // ==================== Constants ====================

    private static final Identifier SELECT_RECT = OnePieceRPG.id("textures/gui/class/select_rect.png");
    private static final Identifier SELECT_TEXT = OnePieceRPG.id("textures/gui/class/select_text.png");

    // Base dimensions at reference scale (before scaling)
    private static final float BASE_WIDTH = 500;
    private static final float BASE_HEIGHT = 720;
    private static final float BASE_NAME_DIM = 180;
    private static final float NAME_BOTTOM_MARGIN = 40;
    private static final float SELECT_RECT_WIDTH = 313;
    private static final float SELECT_RECT_HEIGHT = 90;
    private static final float SELECT_TEXT_WIDTH = 214;
    private static final float SELECT_TEXT_HEIGHT = 35;

    // Selection box positioning (base coordinates that will be scaled)
    private static final float SELECT_RECT_X_OFFSET = 60;
    private static final float SELECT_RECT_Y_FROM_BOTTOM = 50;
    private static final float SELECT_TEXT_X_OFFSET = 45; // Offset from rect
    private static final float SELECT_TEXT_Y_OFFSET = 20;  // Offset from rect

    // Animation constants
    private static final float SCALE_SPEED = 0.2f;
    private static final float TARGET_SCALE_HOVERED = 1.1f;
    private static final float TARGET_SCALE_NORMAL = 1.0f;

    // ==================== Fields ====================

    private final Identifier classId;
    private final ClassConfig config;
    private final float relativeOffset;
    private final Consumer<Identifier> onClickCallback;
    private final WidgetBounds bounds;

    private boolean isHovered = false;
    private boolean anyHovered = false;
    private float currentScale = 1.0f;
    private float targetScale = 1.0f;

    // ==================== Constructor ====================

    public ClassWidget(Identifier classId, ClassConfig config, float relativeOffset,
                       Consumer<Identifier> onClickCallback) {
        this.classId = classId;
        this.config = config;
        this.relativeOffset = relativeOffset;
        this.onClickCallback = onClickCallback;
        this.bounds = new WidgetBounds();
    }

    // ==================== Layout Management ====================

    public void updateLayout(float scale, int containerWidth) {
        this.bounds.update(scale, containerWidth, relativeOffset);
        this.bounds.updateScreenPosition();
    }

    // ==================== State Management ====================

    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
        this.targetScale = hovered ? TARGET_SCALE_HOVERED : TARGET_SCALE_NORMAL;
    }

    public void setAnyHovered(boolean anyHovered) {
        this.anyHovered = anyHovered;
    }

    // ==================== Rendering ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Smoothly interpolate current scale towards target scale
        float lerpFactor = 1.0f - (float) Math.pow(0.001, delta * SCALE_SPEED);
        currentScale = MathHelper.lerp(lerpFactor, currentScale, targetScale);

        if (Math.abs(currentScale - targetScale) < 0.001f) {
            currentScale = targetScale;
        }

        // Calculate dimensions with hover scale applied
        int scaledWidth = (int) (bounds.width * currentScale);
        int scaledHeight = (int) (bounds.height * currentScale);
        float scaledOffsetX = bounds.width * (currentScale - 1) / 2;
        float scaledOffsetY = bounds.height * (currentScale - 1) / 2;

        int scaledNameDim = (int) (bounds.nameDim * currentScale);
        int scaledSelectRectWidth = (int) (bounds.selectRectWidth * currentScale);
        int scaledSelectRectHeight = (int) (bounds.selectRectHeight * currentScale);
        int scaledSelectTextWidth = (int) (bounds.selectTextWidth * currentScale);
        int scaledSelectTextHeight = (int) (bounds.selectTextHeight * currentScale);

        // Determine textures
        Identifier backTexture = isHovered || !anyHovered ?
                config.backTexture() :
                TextureFramebufferCache.getGrayscaleTexture(config.backTexture());

        // Calculate base position (top-left corner after scaling)
        float renderX = bounds.x - scaledOffsetX;
        float renderY = bounds.y - scaledOffsetY;

        // Draw background
        RenderSystem.enableBlend();
        context.drawTexture(
                backTexture,
                (int) renderX,
                (int) renderY,
                0f, 0f,
                scaledWidth, scaledHeight,
                scaledWidth, scaledHeight
        );
        RenderSystem.disableBlend();

        // Draw name badge
        float scaledNameMargin = NAME_BOTTOM_MARGIN * bounds.baseScale * currentScale;
        int nameY = (int) (renderY + scaledHeight - scaledNameDim - scaledNameMargin);

        RenderSystem.enableBlend();
        context.drawTexture(
                config.nameTexture(),
                (int) renderX,
                nameY,
                0, 0,
                scaledNameDim, scaledNameDim,
                scaledNameDim, scaledNameDim
        );
        RenderSystem.disableBlend();

        // Draw selection rectangle and text
        // Position relative to the RENDERED (scaled) background
        float baseRectX = SELECT_RECT_X_OFFSET * bounds.baseScale * currentScale;
        float baseRectYFromBottom = SELECT_RECT_Y_FROM_BOTTOM * bounds.baseScale * currentScale;

        // Position from the rendered background's position
        float rectX = renderX + baseRectX;
        float rectY = renderY + scaledHeight - baseRectYFromBottom;

        // Text offset from rect (also scaled)
        float textOffsetX = SELECT_TEXT_X_OFFSET * bounds.baseScale * currentScale;
        float textOffsetY = SELECT_TEXT_Y_OFFSET * bounds.baseScale * currentScale;

        int textX = (int) (rectX + textOffsetX);
        int textY = (int) (rectY + textOffsetY);

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
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    public Identifier getClassId() {
        return classId;
    }

    // ==================== Inner Classes ====================

    private static class WidgetBounds {
        float x, y;
        float width, height;
        float nameDim;
        float selectTextWidth, selectTextHeight;
        float selectRectWidth, selectRectHeight;
        float baseScale;

        private int containerWidth;
        private float relativeOffset;

        void update(float scale, int containerWidth, float relativeOffset) {
            this.baseScale = scale;
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

            this.x = ((screenWidth - width + relativeOffset * containerWidth) / 2.0f);
            this.y = ((screenHeight - height) / 2);
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width
                    && mouseY >= y && mouseY < y + height;
        }
    }
}