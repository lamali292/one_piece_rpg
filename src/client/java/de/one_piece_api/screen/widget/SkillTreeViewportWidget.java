package de.one_piece_api.screen.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.screen.OnePieceScreen;
import de.one_piece_api.mixin_interface.IHidden;
import de.one_piece_api.mixin_interface.StyledConnection;
import de.one_piece_api.registry.ClientStyleRegistry;
import de.one_piece_api.init.MyFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.texture.Scaling;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.ClientFrameConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer;
import net.puffish.skillsmod.client.rendering.ItemBatchedRenderer;
import net.puffish.skillsmod.client.rendering.TextureBatchedRenderer;
import net.puffish.skillsmod.util.Bounds2i;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Widget for rendering and interacting with the skill tree viewport.
 * <p>
 * This widget provides a zoomable and pannable view of the skill tree, with
 * interactive skills that can be hovered and clicked. It handles coordinate
 * transformations, viewport clipping, and all skill tree rendering logic.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Pan and zoom with smooth scrolling</li>
 *     <li>Viewport clipping for efficient rendering</li>
 *     <li>Mouse-to-world coordinate transformation</li>
 *     <li>Skill hover detection with tooltips</li>
 *     <li>Connection rendering between skills</li>
 *     <li>State-based skill coloring (locked, available, unlocked, etc.)</li>
 *     <li>Support for hidden skills (IHidden interface)</li>
 *     <li>Styled connections with custom colors</li>
 * </ul>
 *
 * <h2>Coordinate Systems:</h2>
 * <ul>
 *     <li>Screen space - Raw mouse coordinates</li>
 *     <li>Viewport space - Relative to viewport origin</li>
 *     <li>World space - Transformed coordinates within the skill tree</li>
 * </ul>
 *
 * @see Drawable
 * @see Element
 * @see ClientCategoryData
 */
public class SkillTreeViewportWidget implements Drawable, Element {

    // ==================== Constants ====================

    /** Color for unlocked and available skills (white) */
    private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);

    /** Color for locked and excluded skills (dark gray) */
    private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);

    /** Extra padding added to content bounds in pixels */
    private static final int CONTENT_GROW = 32;

    /** Multiplier for scroll wheel sensitivity */
    private static final double SCROLL_SENSITIVITY = 0.25;

    // ==================== Fields ====================

    /** X-coordinate of the viewport's left edge */
    private final int viewportX;

    /** Y-coordinate of the viewport's top edge */
    private final int viewportY;

    /** Width of the viewport in pixels */
    private final int viewportWidth;

    /** Height of the viewport in pixels */
    private final int viewportHeight;

    /** Minecraft client instance for rendering */
    private final MinecraftClient client;

    /** Current category data containing skills and state */
    private ClientCategoryData categoryData;

    /** Bounds of the skill tree content in world space */
    private Bounds2i bounds = Bounds2i.zero();

    /** Minimum allowed zoom scale */
    private float minScale = 1f;

    /** Maximum allowed zoom scale */
    private float maxScale = 2f;

    /** Callback invoked when a skill is hovered */
    private Consumer<SkillHoverInfo> onSkillHover;

    /** Callback invoked when no skill is hovered */
    private Runnable onNoSkillHover;

    // ==================== Constructor ====================

    /**
     * Creates a new skill tree viewport widget.
     *
     * @param x the x-coordinate of the viewport's left edge
     * @param y the y-coordinate of the viewport's top edge
     * @param width the width of the viewport in pixels
     * @param height the height of the viewport in pixels
     */
    public SkillTreeViewportWidget(int x, int y, int width, int height) {
        this.viewportX = x;
        this.viewportY = y;
        this.viewportWidth = width;
        this.viewportHeight = height;
        this.client = MinecraftClient.getInstance();
    }

    // ==================== Configuration ====================

    /**
     * Sets the category data to display in the viewport.
     * <p>
     * Recalculates bounds and zoom limits based on the new category's
     * skill layout.
     *
     * @param categoryData the category data, or {@code null} to clear
     */
    public void setCategoryData(ClientCategoryData categoryData) {
        this.categoryData = categoryData;
        calculateBounds();
    }

    /**
     * Sets the callback for skill hover events.
     * <p>
     * The callback receives information about the hovered skill including
     * title, description, and ID.
     *
     * @param handler the hover callback, or {@code null} to remove
     */
    public void setOnSkillHover(Consumer<SkillHoverInfo> handler) {
        this.onSkillHover = handler;
    }

    /**
     * Sets the callback for when no skill is hovered.
     *
     * @param handler the callback, or {@code null} to remove
     */
    public void setOnNoSkillHover(Runnable handler) {
        this.onNoSkillHover = handler;
    }

    // ==================== Bounds Calculation ====================

    /**
     * Calculates the bounds and zoom constraints for the skill tree.
     * <p>
     * Determines the world-space bounds of all skills, adds padding,
     * and calculates minimum/maximum zoom levels based on viewport size.
     */
    private void calculateBounds() {
        if (categoryData == null) {
            this.bounds = Bounds2i.zero();
            return;
        }

        this.bounds = categoryData.getConfig().getBounds();
        this.bounds.grow(CONTENT_GROW);

        int halfContentWidth = MathHelper.ceilDiv(this.bounds.height() * viewportWidth, viewportHeight * 2);
        int halfContentHeight = MathHelper.ceilDiv(this.bounds.width() * viewportHeight, viewportWidth * 2);

        this.bounds.extend(new Vector2i(-halfContentWidth, -halfContentHeight));
        this.bounds.extend(new Vector2i(halfContentWidth, halfContentHeight));

        this.minScale = Math.max(
                (float) viewportWidth / this.bounds.width(),
                (float) viewportHeight / this.bounds.height()
        ) * 0.75F;
        this.maxScale = 2f;

        applyViewChanges(
                categoryData.getX(),
                categoryData.getY(),
                categoryData.getScale()
        );
    }

    /**
     * Applies and clamps view changes for pan and zoom operations.
     * <p>
     * Ensures the viewport stays within valid bounds and zoom levels.
     * Allows some dragging beyond viewport edges for better UX.
     *
     * @param x the new x-offset
     * @param y the new y-offset
     * @param scale the new zoom scale
     */
    private void applyViewChanges(int x, int y, float scale) {
        if (this.bounds.width() <= 0 || this.bounds.height() <= 0 || categoryData == null) {
            return;
        }

        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;

        // Calculate the actual content size at this scale
        int scaledWidth = (int) (bounds.width() * scale);
        int scaledHeight = (int) (bounds.height() * scale);

        // Add padding to allow dragging beyond viewport edges
        int dragPadding = 40;

        int minX, maxX, minY, maxY;

        if (scaledWidth < viewportWidth) {
            // Allow dragging with padding even when smaller than viewport
            int centerOffset = (viewportWidth - scaledWidth) / 2;
            minX = -centerOffset - dragPadding;
            maxX = centerOffset + dragPadding;
        } else {
            minX = (int) Math.ceil(halfWidth - bounds.max().x() * scale);
            maxX = (int) Math.floor(-halfWidth - bounds.min().x() * scale);
        }

        if (scaledHeight < viewportHeight) {
            // Allow dragging with padding even when smaller than viewport
            int centerOffset = (viewportHeight - scaledHeight) / 2;
            minY = -centerOffset - dragPadding;
            maxY = centerOffset + dragPadding;
        } else {
            minY = (int) Math.ceil(halfHeight - bounds.max().y() * scale);
            maxY = (int) Math.floor(-halfHeight - bounds.min().y() * scale);
        }

        categoryData.setX(MathHelper.clamp(x, minX, maxX));
        categoryData.setY(MathHelper.clamp(y, minY, maxY));
        categoryData.setScale(MathHelper.clamp(scale, minScale, maxScale));
    }

    // ==================== Rendering ====================

    /**
     * Renders the skill tree viewport.
     * <p>
     * Sets up scissor testing for clipping, renders the skill tree with
     * all skills and connections, and handles hover detection.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @param delta the frame delta time (unused)
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (categoryData == null) return;

        setupRenderState();
        context.enableScissor(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight);

        double relativeMouseX = mouseX - viewportX;
        double relativeMouseY = mouseY - viewportY;

        renderSkillTree(context, relativeMouseX, relativeMouseY);
        renderTitle(context);
        context.disableScissor();
    }

    /**
     * Renders the viewport title.
     * <p>
     * Displays "Skill Tree" centered at the top of the viewport.
     *
     * @param context the drawing context
     */
    private void renderTitle(DrawContext context) {
        Text titleText = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".skill.skilltree")
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withFormatting(Formatting.UNDERLINE));
        int textWidth = client.textRenderer.getWidth(titleText);
        int textX = OnePieceScreen.Layout.SKILLTREE_OFFSET_X + (OnePieceScreen.Layout.SKILLTREE_WIDTH - textWidth) / 2 + viewportX;
        int textY = 4 + viewportY;

        context.drawText(client.textRenderer, titleText, textX, textY, 0xffffffff, false);
    }

    /**
     * Sets up OpenGL render state for skill tree rendering.
     * <p>
     * Configures shader color, color masking, blending, and depth testing.
     */
    private void setupRenderState() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    /**
     * Renders the complete skill tree with transformations.
     * <p>
     * Applies pan and zoom transformations, renders connections and skills,
     * and handles hover detection and highlighting.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate in viewport space
     * @param mouseY the mouse y-coordinate in viewport space
     */
    private void renderSkillTree(DrawContext context, double mouseX, double mouseY) {
        var activeCategory = categoryData.getConfig();
        Vector2i mouse = new Vector2i((int) mouseX, (int) mouseY);
        Vector2i transformedMouse = getTransformedMousePos(mouseX, mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(
                categoryData.getX() + viewportWidth / 2F + viewportX,
                categoryData.getY() + viewportHeight / 2f + viewportY,
                0f
        );
        context.getMatrices().scale(categoryData.getScale(), categoryData.getScale(), 1f);

        ConnectionBatchedRenderer connectionRenderer = renderConnections(context, activeCategory);

        Optional<ClientSkillConfig> hoveredSkill = findHoveredSkill(mouse, transformedMouse, activeCategory);
        if (hoveredSkill.isPresent()) {
            handleSkillHover(hoveredSkill.get(), activeCategory, connectionRenderer, context);
        } else if (onNoSkillHover != null) {
            onNoSkillHover.run();
        }

        context.draw();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        connectionRenderer.draw();

        renderSkills(context, activeCategory);

        context.getMatrices().pop();
    }

    /**
     * Renders all skill connections with custom styling.
     * <p>
     * Connections are rendered with colors from style registry or default colors.
     *
     * @param context the drawing context
     * @param activeCategory the category configuration
     * @return the connection renderer for additional rendering
     */
    private ConnectionBatchedRenderer renderConnections(DrawContext context, ClientCategoryConfig activeCategory) {
        ConnectionBatchedRenderer renderer = new ConnectionBatchedRenderer();

        for (ClientSkillConnectionConfig connection : activeCategory.normalConnections()) {
            StyledConnection styled = (StyledConnection) (Object) connection;
            if (styled == null) continue;
            categoryData.getConnection(connection).ifPresent(relation -> {
                int color = styled.onepiece$getStyle().flatMap(ClientStyleRegistry::getStyle).map(style ->
                        style.color().argb()
                ).orElse(relation.getColor().fill().argb());
                renderer.emitConnection(
                        context,
                        relation.getSkillA().x(), relation.getSkillA().y(),
                        relation.getSkillB().x(), relation.getSkillB().y(),
                        true,
                        color,
                        relation.getColor().stroke().argb()
                );

            });
        }
        return renderer;
    }

    /**
     * Finds the skill under the mouse cursor.
     *
     * @param mouse mouse position in viewport space
     * @param transformedMouse mouse position in world space
     * @param activeCategory the category configuration
     * @return an {@link Optional} containing the hovered skill, or empty if none
     */
    private Optional<ClientSkillConfig> findHoveredSkill(Vector2i mouse, Vector2i transformedMouse,
                                                         ClientCategoryConfig activeCategory) {
        if (isOutsideViewport(mouse)) {
            return Optional.empty();
        }

        return activeCategory.skills().values().stream()
                .filter(skill -> !shouldIgnoreSkill(skill))
                .filter(skill -> activeCategory.getDefinitionById(skill.definitionId())
                        .map(definition -> isInsideSkill(transformedMouse, skill, definition))
                        .orElse(false))
                .findFirst();
    }

    /**
     * Handles hover events for a skill.
     * <p>
     * Invokes the hover callback with skill information and renders
     * exclusive connections for the hovered skill.
     *
     * @param skill the hovered skill
     * @param activeCategory the category configuration
     * @param connectionRenderer the connection renderer
     * @param context the drawing context
     */
    private void handleSkillHover(ClientSkillConfig skill, ClientCategoryConfig activeCategory,
                                  ConnectionBatchedRenderer connectionRenderer, DrawContext context) {
        var definition = activeCategory.definitions().get(skill.definitionId());
        if (definition == null) return;

        if (onSkillHover != null) {
            onSkillHover.accept(new SkillHoverInfo(
                    definition.title(),
                    definition.description(),
                    definition.extraDescription(),
                    skill.id()
            ));
        }

        var connections = activeCategory.skillExclusiveConnections().get(skill.id());
        if (connections != null) {
            for (var connection : connections) {
                categoryData.getConnection(connection).ifPresent(relation ->
                        connectionRenderer.emitConnection(
                                context,
                                relation.getSkillA().x(), relation.getSkillA().y(),
                                relation.getSkillB().x(), relation.getSkillB().y(),
                                connection.bidirectional(),
                                relation.getColor().fill().argb(),
                                relation.getColor().stroke().argb()
                        )
                );
            }
        }
    }

    /**
     * Renders all visible skills.
     * <p>
     * Skills are rendered with frames and icons based on their state
     * and configuration. Hidden skills are skipped.
     *
     * @param context the drawing context
     * @param activeCategory the category configuration
     */
    private void renderSkills(DrawContext context, ClientCategoryConfig activeCategory) {
        TextureBatchedRenderer textureRenderer = new TextureBatchedRenderer();
        ItemBatchedRenderer itemRenderer = new ItemBatchedRenderer();

        for (var skill : activeCategory.skills().values()) {
            if (shouldIgnoreSkill(skill)) continue;

            activeCategory.getDefinitionById(skill.definitionId()).ifPresent(definition -> {
                Skill.State skillState = categoryData.getSkillState(skill);

                drawFrame(context, textureRenderer, definition.frame(),
                        definition.size(), skill.x(), skill.y(), skillState);
                drawIcon(context, textureRenderer, itemRenderer, definition.icon(),
                        definition.size(), skill.x(), skill.y());
            });
        }

        textureRenderer.draw();
        itemRenderer.draw();
    }

    /**
     * Draws a skill frame based on state.
     * <p>
     * Supports both advancement-style frames and custom texture frames,
     * with state-based coloring.
     *
     * @param context the drawing context
     * @param textureRenderer the texture renderer
     * @param frame the frame configuration
     * @param sizeScale the size scaling factor
     * @param x the x-coordinate in world space
     * @param y the y-coordinate in world space
     * @param state the skill state
     */
    private void drawFrame(DrawContext context, TextureBatchedRenderer textureRenderer,
                           ClientFrameConfig frame, float sizeScale, int x, int y, Skill.State state) {
        int halfSize = Math.round(13f * sizeScale);
        int size = halfSize * 2;

        switch (frame) {
            case ClientFrameConfig.AdvancementFrameConfig advFrame -> {
                var guiAtlasManager = client.getGuiAtlasManager();
                var status = switch (state) {
                    case LOCKED, EXCLUDED, AVAILABLE, AFFORDABLE ->
                            net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus.UNOBTAINED;
                    case UNLOCKED -> net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus.OBTAINED;
                };
                var texture = status.getFrameTexture(advFrame.frame());
                var sprite = guiAtlasManager.getSprite(texture);
                var scaling = guiAtlasManager.getScaling(sprite);
                var color = switch (state) {
                    case LOCKED, EXCLUDED -> COLOR_GRAY;
                    case AFFORDABLE, UNLOCKED -> COLOR_WHITE;
                    case AVAILABLE -> new Vector4f(0.8f, 0.4f, 0.4f, 1.0F);
                };
                textureRenderer.emitSprite(context, sprite, scaling,
                        x - halfSize, y - halfSize, size, size, color);
            }
            case ClientFrameConfig.TextureFrameConfig texFrame -> {
                Identifier texture = getTextureForState(state, texFrame);
                Vector4fc color = getColorForState(state, texFrame);
                textureRenderer.emitTexture(context, texture,
                        x - halfSize, y - halfSize, size, size, color);
            }
        }
    }

    /**
     * Draws a skill icon.
     * <p>
     * Supports item icons, effect icons, and texture icons with proper scaling.
     *
     * @param context the drawing context
     * @param textureRenderer the texture renderer
     * @param itemRenderer the item renderer
     * @param icon the icon configuration
     * @param sizeScale the size scaling factor
     * @param x the x-coordinate in world space
     * @param y the y-coordinate in world space
     */
    private void drawIcon(DrawContext context, TextureBatchedRenderer textureRenderer,
                          ItemBatchedRenderer itemRenderer, ClientIconConfig icon,
                          float sizeScale, int x, int y) {
        var matrices = context.getMatrices();
        matrices.push();

        switch (icon) {
            case ClientIconConfig.ItemIconConfig itemIcon -> {
                matrices.translate(x * (1f - sizeScale), y * (1f - sizeScale), 1.0f);
                matrices.scale(sizeScale, sizeScale, 1);
                itemRenderer.emitItem(context, itemIcon.item(), x, y);
            }
            case ClientIconConfig.EffectIconConfig effectIcon -> {
                matrices.translate(0f, 0f, 1f);
                var sprite = client.getStatusEffectSpriteManager()
                        .getSprite(Registries.STATUS_EFFECT.getEntry(effectIcon.effect()));
                int halfSize = Math.round(9f * sizeScale);
                int size = halfSize * 2;
                textureRenderer.emitSprite(context, sprite, Scaling.STRETCH,
                        x - halfSize, y - halfSize, size, size, COLOR_WHITE);
            }
            case ClientIconConfig.TextureIconConfig textureIcon -> {
                matrices.translate(0f, 0f, 1f);
                int halfSize = Math.round(8f * sizeScale);
                int size = halfSize * 2;
                textureRenderer.emitTexture(context, textureIcon.texture(),
                        x - halfSize, y - halfSize, size, size, COLOR_WHITE);
            }
        }

        matrices.pop();
    }

    /**
     * Gets the appropriate texture for a skill frame based on state.
     *
     * @param state the skill state
     * @param texFrame the texture frame configuration
     * @return the texture identifier
     */
    private Identifier getTextureForState(Skill.State state, ClientFrameConfig.TextureFrameConfig texFrame) {
        return switch (state) {
            case LOCKED -> texFrame.lockedTexture().orElse(texFrame.availableTexture());
            case AVAILABLE -> texFrame.availableTexture();
            case AFFORDABLE -> texFrame.affordableTexture().orElse(texFrame.availableTexture());
            case UNLOCKED -> texFrame.unlockedTexture();
            case EXCLUDED -> texFrame.excludedTexture().orElse(texFrame.availableTexture());
        };
    }

    /**
     * Gets the appropriate color for a skill frame based on state.
     *
     * @param state the skill state
     * @param texFrame the texture frame configuration
     * @return the color vector
     */
    private Vector4fc getColorForState(Skill.State state, ClientFrameConfig.TextureFrameConfig texFrame) {
        return switch (state) {
            case LOCKED -> texFrame.lockedTexture().isPresent() ? COLOR_WHITE : COLOR_GRAY;
            case EXCLUDED -> texFrame.excludedTexture().isPresent() ? COLOR_WHITE : COLOR_GRAY;
            default -> COLOR_WHITE;
        };
    }

    // ==================== Position Queries ====================

    /**
     * Gets the skill ID at a specific screen position.
     *
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @return an {@link Optional} containing the skill ID, or empty if none
     */
    public Optional<String> getSkillAtPosition(double mouseX, double mouseY) {
        if (categoryData == null) return Optional.empty();

        double relativeX = mouseX - viewportX;
        double relativeY = mouseY - viewportY;

        Vector2i mouse = new Vector2i((int) relativeX, (int) relativeY);
        if (isOutsideViewport(mouse)) return Optional.empty();

        Vector2i transformedMouse = getTransformedMousePos(relativeX, relativeY);
        var activeCategory = categoryData.getConfig();

        return activeCategory.skills().values().stream()
                .filter(skill -> !shouldIgnoreSkill(skill))
                .filter(skill -> activeCategory.getDefinitionById(skill.definitionId())
                        .map(definition -> isInsideSkill(transformedMouse, skill, definition))
                        .orElse(false))
                .findFirst()
                .map(ClientSkillConfig::id);
    }

    // ==================== View Manipulation ====================

    /**
     * Applies zoom centered on a specific point.
     * <p>
     * Zooms while keeping the point under the mouse stationary, creating
     * an intuitive zoom-to-cursor effect.
     *
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @param verticalAmount the scroll amount (positive = zoom in)
     */
    public void applyZoom(double mouseX, double mouseY, double verticalAmount) {
        if (categoryData == null) return;

        double relativeX = mouseX - viewportX;
        double relativeY = mouseY - viewportY;

        float factor = (float) Math.pow(2, verticalAmount * SCROLL_SENSITIVITY);

        int x0 = categoryData.getX();
        int y0 = categoryData.getY();
        float currentScale = categoryData.getScale();
        float newScale = MathHelper.clamp(currentScale * factor, minScale, maxScale);

        float actualFactor = newScale / currentScale;

        applyViewChanges(
                x0 - (int) Math.round((actualFactor - 1f) * (relativeX - x0 - viewportWidth / 2f)),
                y0 - (int) Math.round((actualFactor - 1f) * (relativeY - y0 - viewportHeight / 2f)),
                newScale
        );
    }

    /**
     * Applies panning based on drag offset.
     *
     * @param adjustedMouseX the current mouse x-coordinate adjusted for drag
     * @param adjustedMouseY the current mouse y-coordinate adjusted for drag
     * @param startX the drag start x-coordinate
     * @param startY the drag start y-coordinate
     */
    public void applyPan(double adjustedMouseX, double adjustedMouseY, double startX, double startY) {
        if (categoryData == null) return;

        applyViewChanges(
                (int) Math.round(adjustedMouseX - startX),
                (int) Math.round(adjustedMouseY - startY),
                categoryData.getScale()
        );
    }

    // ==================== Coordinate Transformation ====================

    /**
     * Transforms mouse position from viewport space to world space.
     *
     * @param mouseX the mouse x-coordinate in viewport space
     * @param mouseY the mouse y-coordinate in viewport space
     * @return the position in world space
     */
    private Vector2i getTransformedMousePos(double mouseX, double mouseY) {
        return new Vector2i(
                (int) Math.round((mouseX - categoryData.getX() - viewportWidth / 2.0) / categoryData.getScale()),
                (int) Math.round((mouseY - categoryData.getY() - viewportHeight / 2.0) / categoryData.getScale())
        );
    }

    /**
     * Checks if a position is outside the viewport bounds.
     *
     * @param mouse the position in viewport space
     * @return {@code true} if outside viewport, {@code false} otherwise
     */
    private boolean isOutsideViewport(Vector2i mouse) {
        return mouse.x < 0 || mouse.y < 0 || mouse.x >= viewportWidth || mouse.y >= viewportHeight;
    }

    /**
     * Checks if a skill should be ignored (hidden).
     *
     * @param skill the skill to check
     * @return {@code true} if the skill should be ignored, {@code false} otherwise
     */
    private boolean shouldIgnoreSkill(ClientSkillConfig skill) {
        IHidden iHidden = (IHidden) (Object) skill;
        if (iHidden == null ) return true;
        return iHidden.onepiece$isHidden();
    }

    private boolean isInsideSkill(Vector2i transformedMouse, ClientSkillConfig skill,
                                  ClientSkillDefinitionConfig definition) {
        if (shouldIgnoreSkill(skill)) return false;

        int halfSize = Math.round(13f * definition.size());
        return transformedMouse.x >= skill.x() - halfSize &&
                transformedMouse.y >= skill.y() - halfSize &&
                transformedMouse.x < skill.x() + halfSize &&
                transformedMouse.y < skill.y() + halfSize;
    }

    public boolean isMouseNotInViewport(double mouseX, double mouseY) {
        return !(mouseX >= viewportX) || !(mouseY >= viewportY) ||
                !(mouseX < viewportX + viewportWidth) || !(mouseY < viewportY + viewportHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false; // Handled by parent
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    public record SkillHoverInfo(net.minecraft.text.Text title, net.minecraft.text.Text description,
                                 net.minecraft.text.Text extraDescription, String skillId) {
    }
}