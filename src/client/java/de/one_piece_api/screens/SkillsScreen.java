package de.one_piece_api.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.registries.MyFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.Scaling;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.ClientFrameConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer;
import net.puffish.skillsmod.client.rendering.ItemBatchedRenderer;
import net.puffish.skillsmod.client.rendering.TextureBatchedRenderer;
import net.puffish.skillsmod.util.Bounds2i;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class SkillsScreen extends Screen {

    // Constants
    private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);
    private static final int CONTENT_GROW = 32;
    private static final int BOX_WIDTH = OnePieceScreen.skilltreeWidth;
    private static final int BOX_HEIGHT = OnePieceScreen.contentHeight;
    private static final double DRAG_THRESHOLD = 2.0;
    private static final double SCROLL_SENSITIVITY = 0.25;

    private final OnePieceScreen parent;

    // Viewport and scaling
    private Bounds2i bounds = Bounds2i.zero();
    private float minScale = 1f;
    private float maxScale = 1f;

    // Drag state
    private final DragState dragState = new DragState();

    public SkillsScreen(OnePieceScreen parent, ClientPlayerEntity player) {
        super(ScreenTexts.EMPTY);
        this.client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;
        this.width = client.getWindow().getScaledWidth();
        this.height = client.getWindow().getScaledHeight();
        // Core fields
        this.parent = parent;
        calculateBounds();
    }

    @Override
    protected void init() {
        super.init();
        calculateBounds();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.width = width;
        this.height = height;
        calculateBounds();
    }

    private void calculateBounds() {
        if (parent.categoryData == null) {
            this.bounds = Bounds2i.zero();
            return;
        }

        this.bounds = parent.categoryData.getConfig().getBounds();
        this.bounds.grow(CONTENT_GROW);

        // Calculate content extensions
        int halfContentWidth = MathHelper.ceilDiv(this.bounds.height() * BOX_WIDTH, BOX_HEIGHT * 2);
        int halfContentHeight = MathHelper.ceilDiv(this.bounds.width() * BOX_HEIGHT, BOX_WIDTH * 2);

        this.bounds.extend(new Vector2i(-halfContentWidth - 20, -halfContentHeight - 20));
        this.bounds.extend(new Vector2i(halfContentWidth + 20, halfContentHeight + 20));

        // Calculate scale limits
        this.minScale = Math.max(
                (float) BOX_WIDTH / this.bounds.width(),
                (float) BOX_HEIGHT / this.bounds.height()
        );
        this.maxScale = 2f;

        // Apply current view settings if available
        if (parent.categoryData != null) {
            applyViewChanges(
                    parent.categoryData.getX(),
                    parent.categoryData.getY(),
                    parent.categoryData.getScale()
            );
        }
    }

    private void applyViewChanges(int x, int y, float scale) {
        if (this.bounds.width() <= 0 || this.bounds.height() <= 0 || parent.categoryData == null) {
            return;
        }
        int halfWidth = BOX_WIDTH / 2;
        int halfHeight = BOX_HEIGHT / 2;
        parent.categoryData.setX(MathHelper.clamp(x,
                (int) Math.ceil(halfWidth - bounds.max().x() * scale),
                (int) Math.floor(-halfWidth - bounds.min().x() * scale)
        ));
        parent.categoryData.setY(MathHelper.clamp(y,
                (int) Math.ceil(halfHeight - bounds.max().y() * scale),
                (int) Math.floor(-halfHeight - bounds.min().y() * scale)
        ));
        parent.categoryData.setScale(MathHelper.clamp(scale, minScale, maxScale));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ViewportInfo viewport = getViewport();

        context.getMatrices().push();
        context.getMatrices().translate(viewport.x(), viewport.y(), 0);

        double adjustedMouseX = mouseX - viewport.x();
        double adjustedMouseY = mouseY - viewport.y();

        renderContent(context, adjustedMouseX, adjustedMouseY);

        context.getMatrices().pop();
    }

    private void renderContent(DrawContext context, double mouseX, double mouseY) {
        setupRenderState();

        ViewportInfo viewport = getViewport();
        context.enableScissor(viewport.x(), viewport.y(),
                viewport.x() + BOX_WIDTH, viewport.y() + BOX_HEIGHT);

        if (parent.categoryData == null) {
            renderEmptyState(context);
        } else {
            renderSkillTree(context, mouseX, mouseY);
        }

        context.disableScissor();
    }

    private void setupRenderState() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private void renderEmptyState(DrawContext context) {
        ViewportInfo viewport = getViewport();
        int centerX = viewport.x() + BOX_WIDTH / 2;
        int centerY = viewport.y() + BOX_HEIGHT / 2;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("advancements.sad_label"),
                centerX, centerY - this.textRenderer.fontHeight,
                0xffffffff
        );
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("advancements.empty"),
                centerX, centerY + this.textRenderer.fontHeight,
                0xffffffff
        );
    }

    private void renderSkillTree(DrawContext context, double mouseX, double mouseY) {
        if (client == null || parent.categoryData == null) return;

        ClientCategoryData categoryData = parent.categoryData;
        var activeCategory = categoryData.getConfig();

        Vector2i mouse = new Vector2i((int) mouseX, (int) mouseY);
        Vector2i transformedMouse = getTransformedMousePos(mouseX, mouseY, categoryData);

        context.getMatrices().push();
        context.getMatrices().translate(
                categoryData.getX() + BOX_WIDTH / 2f,
                categoryData.getY() + BOX_HEIGHT / 2f,
                0f
        );
        context.getMatrices().scale(categoryData.getScale(), categoryData.getScale(), 1f);

        // Render connections
        ConnectionBatchedRenderer connectionRenderer = renderConnections(context, activeCategory, categoryData);

        // Handle skill hover effects
        Optional<ClientSkillConfig> hoveredSkill = findHoveredSkill(mouse, transformedMouse, activeCategory);
        hoveredSkill.ifPresent(skill -> handleSkillHover(skill, activeCategory, categoryData, connectionRenderer, context));

        // Draw all elements
        context.draw();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        connectionRenderer.draw();

        renderSkills(context, activeCategory, categoryData);

        context.getMatrices().pop();

        renderTitle(context);
    }

    private ConnectionBatchedRenderer renderConnections(DrawContext context,
                                                        net.puffish.skillsmod.client.config.ClientCategoryConfig activeCategory,
                                                        ClientCategoryData categoryData) {
        ConnectionBatchedRenderer renderer = new ConnectionBatchedRenderer();

        for (var connection : activeCategory.normalConnections()) {
            categoryData.getConnection(connection).ifPresent(relation ->
                    renderer.emitConnection(
                            context,
                            relation.getSkillA().x(), relation.getSkillA().y(),
                            relation.getSkillB().x(), relation.getSkillB().y(),
                            connection.bidirectional(),
                            relation.getColor().fill().argb(),
                            relation.getColor().stroke().argb()
                    )
            );
        }

        return renderer;
    }

    private Optional<ClientSkillConfig> findHoveredSkill(Vector2i mouse, Vector2i transformedMouse,
                                                         ClientCategoryConfig activeCategory) {
        if (!isInsideContent(mouse)) {
            return Optional.empty();
        }

        return activeCategory.skills().values().stream()
                .filter(skill -> !shouldIgnoreSkill(skill))
                .filter(skill -> activeCategory.getDefinitionById(skill.definitionId())
                        .map(definition -> isInsideSkill(transformedMouse, skill, definition))
                        .orElse(false))
                .findFirst();
    }

    private void handleSkillHover(ClientSkillConfig skill,
                                  net.puffish.skillsmod.client.config.ClientCategoryConfig activeCategory,
                                  ClientCategoryData categoryData,
                                  ConnectionBatchedRenderer connectionRenderer,
                                  DrawContext context) {
        var definition = activeCategory.definitions().get(skill.definitionId());
        if (definition == null) return;

        // Store hovered skill info for description box rendering
        OnePieceScreen.TextData hoveredSkillInfo = new OnePieceScreen.TextData(definition.title(), definition.description(), definition.extraDescription(), Text.literal(skill.id()));
        parent.setHoveredSkillInfo(hoveredSkillInfo);

        // Render exclusive connections for hovered skill
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

    private void renderSkills(DrawContext context,
                              net.puffish.skillsmod.client.config.ClientCategoryConfig activeCategory,
                              ClientCategoryData categoryData) {
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

    private void renderTitle(DrawContext context) {
        Text titleText = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".skill.skilltree")
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withFormatting(Formatting.UNDERLINE));
        int textWidth = textRenderer.getWidth(titleText);
        int textX = OnePieceScreen.skilltreeOffsetX + (OnePieceScreen.skilltreeWidth - textWidth) / 2;
        int textY = 4;

        context.drawText(this.textRenderer, titleText, textX, textY, 0xffffffff, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) return false;

        ViewportInfo viewport = getViewport();
        double adjustedMouseX = mouseX - viewport.x();
        double adjustedMouseY = mouseY - viewport.y();

        if (!isMouseInBounds(adjustedMouseX, adjustedMouseY)) return false;
        if (parent.categoryData == null) return false;

        Vector2i mouse = new Vector2i((int) adjustedMouseX, (int) adjustedMouseY);

        if (isInsideContent(mouse)) {
            dragState.startDrag(adjustedMouseX, adjustedMouseY, parent.categoryData);
            return handleSkillClick(adjustedMouseX, adjustedMouseY);
        }

        return false;
    }

    private boolean handleSkillClick(double mouseX, double mouseY) {
        if (parent.categoryData == null) return false;

        Vector2i transformedMouse = getTransformedMousePos(mouseX, mouseY, parent.categoryData);
;        var activeCategory = parent.categoryData.getConfig();

        for (var skill : activeCategory.skills().values()) {
            if (shouldIgnoreSkill(skill)) continue;

            var definition = activeCategory.definitions().get(skill.definitionId());
            if (definition == null) continue;

            if (isInsideSkill(transformedMouse, skill, definition)) {
                SkillsClientMod.getInstance()
                        .getPacketSender()
                        .send(new SkillClickOutPacket(activeCategory.id(), skill.id()));
                parent.updateLearned();
                dragState.preventDrag();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 || !dragState.canDrag()) return false;

        dragState.updateDrag(deltaX, deltaY);

        if (dragState.shouldStartDragging() && parent.categoryData != null) {
            ViewportInfo viewport = getViewport();
            double adjustedMouseX = mouseX - viewport.x();
            double adjustedMouseY = mouseY - viewport.y();

            applyViewChanges(
                    (int) Math.round(adjustedMouseX - dragState.startX),
                    (int) Math.round(adjustedMouseY - dragState.startY),
                    parent.categoryData.getScale()
            );
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) return false;

        if (dragState.isSmallDrag() && parent.categoryData != null) {
            ViewportInfo viewport = getViewport();
            double adjustedMouseX = mouseX - viewport.x();
            double adjustedMouseY = mouseY - viewport.y();
            handleSkillClick(adjustedMouseX, adjustedMouseY);
        }

        dragState.endDrag();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        ViewportInfo viewport = getViewport();
        if (!isMouseInViewport(mouseX, mouseY, viewport) || parent.categoryData == null) {
            return false;
        }

        double adjustedMouseX = mouseX - viewport.x();
        double adjustedMouseY = mouseY - viewport.y();

        float factor = (float) Math.pow(2, verticalAmount * SCROLL_SENSITIVITY);

        int x0 = parent.categoryData.getX();
        int y0 = parent.categoryData.getY();
        float currentScale = parent.categoryData.getScale();
        float newScale = MathHelper.clamp(currentScale * factor, minScale, maxScale);

        float actualFactor = newScale / currentScale;

        applyViewChanges(
                x0 - (int) Math.round((actualFactor - 1f) * (adjustedMouseX - x0 - BOX_WIDTH / 2f)),
                y0 - (int) Math.round((actualFactor - 1f) * (adjustedMouseY - y0 - BOX_HEIGHT / 2f)),
                newScale
        );

        return true;
    }

    // Helper methods
    private ViewportInfo getViewport() {
        int x = (width - OnePieceScreen.backgroundWidth) / 2 + OnePieceScreen.skilltreeOffsetX;
        int y = (height - OnePieceScreen.backgroundHeight) / 2 + OnePieceScreen.contentOffsetY;
        return new ViewportInfo(x, y);
    }

    private boolean isMouseInBounds(double mouseX, double mouseY) {
        return mouseX >= 0 && mouseY >= 0 && mouseX < BOX_WIDTH && mouseY < BOX_HEIGHT;
    }

    private boolean isMouseInViewport(double mouseX, double mouseY, ViewportInfo viewport) {
        return mouseX >= viewport.x() && mouseY >= viewport.y() &&
                mouseX < viewport.x() + BOX_WIDTH && mouseY < viewport.y() + BOX_HEIGHT;
    }

    private Vector2i getTransformedMousePos(double mouseX, double mouseY, ClientCategoryData categoryData) {
        return new Vector2i(
                (int) Math.round((mouseX - categoryData.getX() - BOX_WIDTH / 2.0) / categoryData.getScale()),
                (int) Math.round((mouseY - categoryData.getY() - BOX_HEIGHT / 2.0) / categoryData.getScale())
        );
    }

    private boolean isInsideContent(Vector2i mouse) {
        return mouse.x >= 0 && mouse.y >= 0 && mouse.x < BOX_WIDTH && mouse.y < BOX_HEIGHT;
    }

    private boolean shouldIgnoreSkill(ClientSkillConfig skill) {
        return skill.y() == 0 && skill.x() == 0;
    }

    private boolean isInsideSkill(Vector2i transformedMouse, ClientSkillConfig skill, ClientSkillDefinitionConfig definition) {
        if (shouldIgnoreSkill(skill)) return false;

        int halfSize = Math.round(13f * definition.size());
        return transformedMouse.x >= skill.x() - halfSize &&
                transformedMouse.y >= skill.y() - halfSize &&
                transformedMouse.x < skill.x() + halfSize &&
                transformedMouse.y < skill.y() + halfSize;
    }

    private void drawIcon(DrawContext context, TextureBatchedRenderer textureRenderer,
                          ItemBatchedRenderer itemRenderer, ClientIconConfig icon,
                          float sizeScale, int x, int y) {
        if (client == null) return;

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

    private void drawFrame(DrawContext context, TextureBatchedRenderer textureRenderer,
                           ClientFrameConfig frame, float sizeScale, int x, int y, Skill.State state) {
        if (client == null) return;

        int halfSize = Math.round(13f * sizeScale);
        int size = halfSize * 2;

        switch (frame) {
            case ClientFrameConfig.AdvancementFrameConfig advFrame -> {
                var guiAtlasManager = client.getGuiAtlasManager();
                var status = switch (state) {
                    case LOCKED, EXCLUDED, AVAILABLE, AFFORDABLE -> AdvancementObtainedStatus.UNOBTAINED;
                    case UNLOCKED -> AdvancementObtainedStatus.OBTAINED;
                };
                var texture = status.getFrameTexture(advFrame.frame());
                var sprite = guiAtlasManager.getSprite(texture);
                var scaling = guiAtlasManager.getScaling(sprite);
                var color = switch (state) {
                    case LOCKED, EXCLUDED -> COLOR_GRAY;
                    case AVAILABLE, AFFORDABLE, UNLOCKED -> COLOR_WHITE;
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

    private Identifier getTextureForState(Skill.State state, ClientFrameConfig.TextureFrameConfig texFrame) {
        return switch (state) {
            case LOCKED -> texFrame.lockedTexture().orElse(texFrame.availableTexture());
            case AVAILABLE -> texFrame.availableTexture();
            case AFFORDABLE -> texFrame.affordableTexture().orElse(texFrame.availableTexture());
            case UNLOCKED -> texFrame.unlockedTexture();
            case EXCLUDED -> texFrame.excludedTexture().orElse(texFrame.availableTexture());
        };
    }

    private Vector4fc getColorForState(Skill.State state, ClientFrameConfig.TextureFrameConfig texFrame) {
        return switch (state) {
            case LOCKED -> texFrame.lockedTexture().isPresent() ? COLOR_WHITE : COLOR_GRAY;
            case EXCLUDED -> texFrame.excludedTexture().isPresent() ? COLOR_WHITE : COLOR_GRAY;
            default -> COLOR_WHITE;
        };
    }

    // Helper classes
    private static class DragState {
        double startX = 0;
        double startY = 0;
        double totalDrag = 0;
        boolean canDrag = false;

        void startDrag(double mouseX, double mouseY, ClientCategoryData categoryData) {
            this.startX = mouseX - categoryData.getX();
            this.startY = mouseY - categoryData.getY();
            this.totalDrag = 0;
            this.canDrag = true;
        }

        void updateDrag(double deltaX, double deltaY) {
            this.totalDrag += Math.abs(deltaX) + Math.abs(deltaY);
        }

        void preventDrag() {
            this.canDrag = false;
        }

        void endDrag() {
            this.canDrag = false;
            this.totalDrag = 0;
        }

        boolean canDrag() {
            return canDrag;
        }

        boolean shouldStartDragging() {
            return totalDrag > DRAG_THRESHOLD;
        }

        boolean isSmallDrag() {
            return canDrag && totalDrag <= DRAG_THRESHOLD;
        }
    }

    private record ViewportInfo(int x, int y) {
    }
}