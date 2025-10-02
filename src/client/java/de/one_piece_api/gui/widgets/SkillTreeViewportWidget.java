package de.one_piece_api.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.interfaces.IHidden;
import de.one_piece_api.interfaces.StyledConnection;
import de.one_piece_api.registries.ClientStyleRegistry;
import de.one_piece_api.registries.MyFonts;
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
 * Handles panning, zooming, and skill rendering.
 */
public class SkillTreeViewportWidget implements Drawable, Element {
    private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);
    private static final int CONTENT_GROW = 32;
    private static final double SCROLL_SENSITIVITY = 0.25;

    private final int viewportX;
    private final int viewportY;
    private final int viewportWidth;
    private final int viewportHeight;
    private final MinecraftClient client;

    private ClientCategoryData categoryData;
    private Bounds2i bounds = Bounds2i.zero();
    private float minScale = 1f;
    private float maxScale = 2f;

    private Consumer<SkillHoverInfo> onSkillHover;
    private Runnable onNoSkillHover;

    public SkillTreeViewportWidget(int x, int y, int width, int height) {
        this.viewportX = x;
        this.viewportY = y;
        this.viewportWidth = width;
        this.viewportHeight = height;
        this.client = MinecraftClient.getInstance();
    }

    public void setCategoryData(ClientCategoryData categoryData) {
        this.categoryData = categoryData;
        calculateBounds();
    }

    public void setOnSkillHover(Consumer<SkillHoverInfo> handler) {
        this.onSkillHover = handler;
    }

    public void setOnNoSkillHover(Runnable handler) {
        this.onNoSkillHover = handler;
    }

    private void calculateBounds() {
        if (categoryData == null) {
            this.bounds = Bounds2i.zero();
            return;
        }

        this.bounds = categoryData.getConfig().getBounds();
        this.bounds.grow(CONTENT_GROW);

        int halfContentWidth = MathHelper.ceilDiv(this.bounds.height() * viewportWidth, viewportHeight * 2);
        int halfContentHeight = MathHelper.ceilDiv(this.bounds.width() * viewportHeight, viewportWidth * 2);

        this.bounds.extend(new Vector2i(-halfContentWidth - 20, -halfContentHeight - 20));
        this.bounds.extend(new Vector2i(halfContentWidth + 20, halfContentHeight + 20));

        this.minScale = Math.max(
                (float) viewportWidth / this.bounds.width(),
                (float) viewportHeight / this.bounds.height()
        );
        this.maxScale = 2f;

        applyViewChanges(
                categoryData.getX(),
                categoryData.getY(),
                categoryData.getScale()
        );
    }

    private void applyViewChanges(int x, int y, float scale) {
        if (this.bounds.width() <= 0 || this.bounds.height() <= 0 || categoryData == null) {
            return;
        }

        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;

        categoryData.setX(MathHelper.clamp(x,
                (int) Math.ceil(halfWidth - bounds.max().x() * scale),
                (int) Math.floor(-halfWidth - bounds.min().x() * scale)
        ));
        categoryData.setY(MathHelper.clamp(y,
                (int) Math.ceil(halfHeight - bounds.max().y() * scale),
                (int) Math.floor(-halfHeight - bounds.min().y() * scale)
        ));
        categoryData.setScale(MathHelper.clamp(scale, minScale, maxScale));
    }

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

    private void renderTitle(DrawContext context) {
        Text titleText = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".skill.skilltree")
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withFormatting(Formatting.UNDERLINE));
        int textWidth = client.textRenderer.getWidth(titleText);
        int textX = OnePieceScreen.skilltreeOffsetX + (OnePieceScreen.skilltreeWidth - textWidth) / 2 + viewportX;
        int textY = 4 + viewportY;

        context.drawText(client.textRenderer, titleText, textX, textY, 0xffffffff, false);
    }

    private void setupRenderState() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

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

    public void applyPan(double adjustedMouseX, double adjustedMouseY, double startX, double startY) {
        if (categoryData == null) return;

        applyViewChanges(
                (int) Math.round(adjustedMouseX - startX),
                (int) Math.round(adjustedMouseY - startY),
                categoryData.getScale()
        );
    }

    private Vector2i getTransformedMousePos(double mouseX, double mouseY) {
        return new Vector2i(
                (int) Math.round((mouseX - categoryData.getX() - viewportWidth / 2.0) / categoryData.getScale()),
                (int) Math.round((mouseY - categoryData.getY() - viewportHeight / 2.0) / categoryData.getScale())
        );
    }

    private boolean isOutsideViewport(Vector2i mouse) {
        return mouse.x < 0 || mouse.y < 0 || mouse.x >= viewportWidth || mouse.y >= viewportHeight;
    }

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