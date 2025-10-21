package de.one_piece_api.screen.widget.main.skill_tree;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.mixin_interface.ISkillTypeProvider;
import de.one_piece_api.mixin_interface.SkillType;
import de.one_piece_api.screen.OnePieceScreen;
import de.one_piece_api.mixin_interface.StyledConnection;
import de.one_piece_api.registry.ClientStyleRegistry;
import de.one_piece_api.init.MyFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.texture.Scaling;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.*;
import java.util.function.Consumer;

/**
 * Pure view component for rendering the skill tree.
 * <p>
 * This widget is a stateless renderer that displays the skill tree based on
 * provided data and viewport state. It does not manage state internally,
 * instead firing events for user interactions.
 *
 * <h2>Design Philosophy:</h2>
 * <ul>
 *     <li><b>Stateless</b> - All state passed in via method parameters</li>
 *     <li><b>Event-driven</b> - Reports user interactions via callbacks</li>
 *     <li><b>Render-only</b> - Does not modify data or manage viewport state</li>
 *     <li><b>Coordinate transformation</b> - Converts between spaces using ViewportState</li>
 * </ul>
 *
 * <h2>Event Callbacks:</h2>
 * <ul>
 *     <li>{@link #setOnSkillHover} - Fired when hovering over a skill</li>
 *     <li>{@link #setOnNoSkillHover} - Fired when not hovering any skill</li>
 * </ul>
 *
 * @see Drawable
 * @see ViewportState
 * @see ClientCategoryData
 */
public class SkillTreeViewportWidget {

    // ==================== Constants ====================

    /** Color for unlocked and available skills (white) */
    private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);

    /** Color for locked and excluded skills (dark gray) */
    private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);

    // ==================== Fields ====================

    /** X-coordinate of the viewport's left edge in screen space */
    private final int screenX;

    /** Y-coordinate of the viewport's top edge in screen space */
    private final int screenY;

    /** Width of the viewport in pixels */
    private final int viewportWidth;

    /** Height of the viewport in pixels */
    private final int viewportHeight;

    /** Minecraft client instance for rendering */
    private final MinecraftClient client;

    /** Callback invoked when a skill is hovered */
    private Consumer<SkillHoverInfo> onSkillHover;

    /** Callback invoked when no skill is hovered */
    private Runnable onNoSkillHover;

    // Hover throttling
    private String lastHoveredSkillId = null;
    private long lastHoverUpdateTime = 0;
    private static final long HOVER_UPDATE_INTERVAL_MS = 50;

    // ==================== Constructor ====================

    /**
     * Creates a new skill tree viewport widget.
     *
     * @param x the x-coordinate of the viewport's left edge in screen space
     * @param y the y-coordinate of the viewport's top edge in screen space
     * @param width the width of the viewport in pixels
     * @param height the height of the viewport in pixels
     */
    public SkillTreeViewportWidget(int x, int y, int width, int height) {
        this.screenX = x;
        this.screenY = y;
        this.viewportWidth = width;
        this.viewportHeight = height;
        this.client = MinecraftClient.getInstance();
    }

    // ==================== Event Handlers ====================

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

    // ==================== Rendering ====================

    /**
     * Renders the skill tree viewport.
     * <p>
     * This is the main entry point for rendering. All required state must be
     * passed as parameters.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @param delta the frame delta time (unused)
     * @param viewportState the current viewport transformation state
     * @param categoryData the category data containing skills and state
     * @param classConfig the class configuration for filtering visible skills
     */
    public void render(DrawContext context, int mouseX, int mouseY, float delta,
                       ViewportState viewportState, ClientCategoryData categoryData,
                       ClassConfig classConfig) {
        if (categoryData == null || viewportState == null) {
            return;
        }

        setupRenderState();
        context.enableScissor(screenX, screenY, screenX + viewportWidth, screenY + viewportHeight);

        double relativeMouseX = mouseX - screenX;
        double relativeMouseY = mouseY - screenY;

        renderSkillTree(context, relativeMouseX, relativeMouseY, viewportState, categoryData, classConfig);
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
        int textX = OnePieceScreen.Layout.SKILLTREE_OFFSET_X +
                (OnePieceScreen.Layout.SKILLTREE_WIDTH - textWidth) / 2 + screenX;
        int textY = 4 + screenY;

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
     * @param viewportState the viewport transformation state
     * @param categoryData the category data
     * @param classConfig the class configuration
     */
    private void renderSkillTree(DrawContext context, double mouseX, double mouseY,
                                 ViewportState viewportState, ClientCategoryData categoryData,
                                 ClassConfig classConfig) {
        var activeCategory = categoryData.getConfig();
        Vector2i mouse = new Vector2i((int) mouseX, (int) mouseY);
        Vector2i transformedMouse = viewportState.viewportToWorld(mouseX, mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(
                viewportState.getX() + viewportWidth / 2f + screenX,
                viewportState.getY() + viewportHeight / 2f + screenY,
                0f
        );
        context.getMatrices().scale(
                viewportState.getScale(),
                viewportState.getScale(),
                1f
        );

        ConnectionBatchedRenderer connectionRenderer = renderConnections(
                context, activeCategory, categoryData
        );

        // Throttled hover updates
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHoverUpdateTime > HOVER_UPDATE_INTERVAL_MS) {
            updateHoverState(mouse, transformedMouse, activeCategory, categoryData,
                    classConfig, connectionRenderer, context);
            lastHoverUpdateTime = currentTime;
        }

        context.draw();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        connectionRenderer.draw();

        renderSkills(context, activeCategory, categoryData, classConfig);

        context.getMatrices().pop();
    }

    /**
     * Updates hover state and fires appropriate callbacks.
     *
     * @param mouse mouse position in viewport space
     * @param transformedMouse mouse position in world space
     * @param activeCategory the category configuration
     * @param categoryData the category data
     * @param classConfig the class configuration
     * @param connectionRenderer the connection renderer
     * @param context the drawing context
     */
    private void updateHoverState(Vector2i mouse, Vector2i transformedMouse,
                                  ClientCategoryConfig activeCategory,
                                  ClientCategoryData categoryData,
                                  ClassConfig classConfig,
                                  ConnectionBatchedRenderer connectionRenderer,
                                  DrawContext context) {
        Optional<ClientSkillConfig> hoveredSkill = findHoveredSkill(
                mouse, transformedMouse, activeCategory, classConfig
        );

        String currentHoveredId = hoveredSkill.map(ClientSkillConfig::id).orElse(null);

        // Only trigger callbacks if hover actually changed
        if (!Objects.equals(currentHoveredId, lastHoveredSkillId)) {
            if (hoveredSkill.isPresent()) {
                handleSkillHover(hoveredSkill.get(), activeCategory, categoryData,
                        connectionRenderer, context);
            } else if (onNoSkillHover != null) {
                onNoSkillHover.run();
            }
            lastHoveredSkillId = currentHoveredId;
        }
    }

    /**
     * Renders all skill connections with custom styling.
     * <p>
     * Connections are rendered with colors from style registry or default colors.
     *
     * @param context the drawing context
     * @param activeCategory the category configuration
     * @param categoryData the category data
     * @return the connection renderer for additional rendering
     */
    private ConnectionBatchedRenderer renderConnections(DrawContext context,
                                                        ClientCategoryConfig activeCategory,
                                                        ClientCategoryData categoryData) {
        ConnectionBatchedRenderer renderer = new ConnectionBatchedRenderer();

        for (ClientSkillConnectionConfig connection : activeCategory.normalConnections()) {
            StyledConnection styled = (StyledConnection) (Object) connection;
            if (styled == null) continue;

            categoryData.getConnection(connection).ifPresent(relation -> {
                int color = styled.onepiece$getStyle()
                        .flatMap(ClientStyleRegistry::getStyle)
                        .map(style -> style.color().argb())
                        .orElse(relation.getColor().fill().argb());

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
     * @param classConfig the class configuration
     * @return an {@link Optional} containing the hovered skill, or empty if none
     */
    private Optional<ClientSkillConfig> findHoveredSkill(Vector2i mouse, Vector2i transformedMouse,
                                                         ClientCategoryConfig activeCategory,
                                                         ClassConfig classConfig) {
        if (isOutsideViewport(mouse)) {
            return Optional.empty();
        }

        List<String> rewardIDs = getRewardIDs(classConfig);

        return activeCategory.skills().values().stream()
                .filter(skill -> isVisible(skill, rewardIDs))
                .filter(skill -> activeCategory.getDefinitionById(skill.definitionId())
                        .map(definition -> isInsideSkill(transformedMouse, skill, definition))
                        .orElse(false))
                .findFirst();
    }

    /**
     * Checks if a skill is visible based on its type and configuration.
     *
     * @param skill the skill to check
     * @param rewardIDs list of reward IDs from class config
     * @return {@code true} if visible, {@code false} otherwise
     */
    private boolean isVisible(ClientSkillConfig skill, List<String> rewardIDs) {
        SkillType skillType = getSkillType(skill);

        return switch (skillType) {
            case SKILL_TREE -> true;
            case CLASS -> rewardIDs != null && !rewardIDs.isEmpty() &&
                    rewardIDs.contains(skill.definitionId());
            default -> false;
        };
    }

    /**
     * Handles hover events for a skill.
     * <p>
     * Invokes the hover callback with skill information and renders
     * exclusive connections for the hovered skill.
     *
     * @param skill the hovered skill
     * @param activeCategory the category configuration
     * @param categoryData the category data
     * @param connectionRenderer the connection renderer
     * @param context the drawing context
     */
    private void handleSkillHover(ClientSkillConfig skill, ClientCategoryConfig activeCategory,
                                  ClientCategoryData categoryData,
                                  ConnectionBatchedRenderer connectionRenderer,
                                  DrawContext context) {
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
     * and configuration.
     *
     * @param context the drawing context
     * @param activeCategory the category configuration
     * @param categoryData the category data
     * @param classConfig the class configuration
     */
    private void renderSkills(DrawContext context, ClientCategoryConfig activeCategory,
                              ClientCategoryData categoryData, ClassConfig classConfig) {
        TextureBatchedRenderer textureRenderer = new TextureBatchedRenderer();
        ItemBatchedRenderer itemRenderer = new ItemBatchedRenderer();

        List<String> rewardIDs = getRewardIDs(classConfig);
        Map<String, Integer> rewardLevels = getRewardLevels(classConfig);
        activeCategory.skills().values().stream()
                .filter(skill -> isVisible(skill, rewardIDs))
                .forEach(skill -> {
                    activeCategory.getDefinitionById(skill.definitionId()).ifPresent(definition -> {
                        Skill.State skillState = categoryData.getSkillState(skill);

                        drawFrame(context, textureRenderer, definition.frame(),
                                definition.size(), skill.x(), skill.y(), skillState);
                        drawIcon(context, textureRenderer, itemRenderer, definition.icon(),
                                definition.size(), skill.x(), skill.y());
                    });
                });


        textureRenderer.draw();
        itemRenderer.draw();
        activeCategory.skills().values().stream()
                .filter(skill -> isVisible(skill, rewardIDs))
                .forEach(skill -> {
                    Integer level = rewardLevels.get(skill.definitionId());
                    if (level != null) {
                        drawLevelText(context, skill.x(), skill.y(), level);
                    }
                });
    }


    /**
     * Draws the level requirement text for a class reward skill.
     * <p>
     * Renders the level number below the skill icon with a shadow for visibility.
     *
     * @param context the drawing context
     * @param x the skill x-coordinate in world space
     * @param y the skill y-coordinate in world space
     * @param level the level requirement
     */
    private void drawLevelText(DrawContext context, int x, int y, int level) {
        String levelText = "lvl "+ level;
        var text = Text.literal(levelText).setStyle(Style.EMPTY.withFont(MyFonts.PRESS_START));
        int textWidth = client.textRenderer.getWidth(text);
        // Position text below the skill frame
        int textX = x - textWidth / 2;
        int textY = y + 15; // Below the 13px half-size frame + 2px spacing

        // Draw with shadow for visibility against any background
        context.drawText(
                client.textRenderer,
                text,
                textX,
                textY,
                0xFFFFFFFF, // White text
                true      // With shadow
        );
    }

    /**
     * Creates a map of skill definition IDs to their level requirements.
     *
     * @param classConfig the class configuration
     * @return map of definition ID to level, or empty map if config is null
     */
    private Map<String, Integer> getRewardLevels(ClassConfig classConfig) {
        if (classConfig == null) {
            return Collections.emptyMap();
        }

        Map<String, Integer> levels = new HashMap<>();
        for (ClassConfig.LevelReward reward : classConfig.rewards()) {
            levels.put(reward.reward().toString(), reward.level());
        }
        return levels;
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
                           ClientFrameConfig frame, float sizeScale, int x, int y,
                           Skill.State state) {
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
    private Identifier getTextureForState(Skill.State state,
                                          ClientFrameConfig.TextureFrameConfig texFrame) {
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
    private Vector4fc getColorForState(Skill.State state,
                                       ClientFrameConfig.TextureFrameConfig texFrame) {
        return switch (state) {
            case LOCKED -> texFrame.lockedTexture().isPresent() ? COLOR_WHITE : COLOR_GRAY;
            case EXCLUDED -> texFrame.excludedTexture().isPresent() ? COLOR_WHITE : COLOR_GRAY;
            default -> COLOR_WHITE;
        };
    }

    // ==================== Hit Testing ====================

    /**
     * Gets the skill ID at a specific screen position.
     *
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @param viewportState the viewport transformation state
     * @param activeCategory the category configuration
     * @param classConfig the class configuration
     * @return an {@link Optional} containing the skill ID, or empty if none
     */
    public Optional<String> getSkillAtPosition(double mouseX, double mouseY,
                                               ViewportState viewportState,
                                               ClientCategoryConfig activeCategory,
                                               ClassConfig classConfig) {
        double relativeX = mouseX - screenX;
        double relativeY = mouseY - screenY;

        Vector2i mouse = new Vector2i((int) relativeX, (int) relativeY);
        if (isOutsideViewport(mouse)) {
            return Optional.empty();
        }

        Vector2i transformedMouse = viewportState.viewportToWorld(relativeX, relativeY);
        List<String> rewardIDs = getRewardIDs(classConfig);

        return activeCategory.skills().values().stream()
                .filter(skill -> isVisible(skill, rewardIDs))
                .filter(skill -> activeCategory.getDefinitionById(skill.definitionId())
                        .map(definition -> isInsideSkill(transformedMouse, skill, definition))
                        .orElse(false))
                .findFirst()
                .map(ClientSkillConfig::id);
    }

    /**
     * Checks if a screen position is outside the viewport bounds.
     *
     * @param mouseX the mouse x-coordinate in screen space
     * @param mouseY the mouse y-coordinate in screen space
     * @return {@code true} if outside viewport, {@code false} otherwise
     */
    public boolean isMouseOutsideViewport(double mouseX, double mouseY) {
        return mouseX < screenX || mouseY < screenY ||
                mouseX >= screenX + viewportWidth || mouseY >= screenY + viewportHeight;
    }

    // ==================== Helper Methods ====================

    /**
     * Gets the skill type for visibility filtering.
     *
     * @param skill the skill to check
     * @return the skill type
     */
    private @NotNull SkillType getSkillType(ClientSkillConfig skill) {
        ISkillTypeProvider iSkillType = (ISkillTypeProvider) (Object) skill;
        if (iSkillType == null) return SkillType.NONE;
        return iSkillType.onepiece$getSkillType();
    }

    /**
     * Checks if the mouse is inside a skill's bounds.
     *
     * @param transformedMouse the mouse position in world space
     * @param skill the skill to check
     * @param definition the skill definition
     * @return {@code true} if inside skill bounds, {@code false} otherwise
     */
    private boolean isInsideSkill(Vector2i transformedMouse, ClientSkillConfig skill,
                                  ClientSkillDefinitionConfig definition) {
        int halfSize = Math.round(13f * definition.size());
        return transformedMouse.x >= skill.x() - halfSize &&
                transformedMouse.y >= skill.y() - halfSize &&
                transformedMouse.x < skill.x() + halfSize &&
                transformedMouse.y < skill.y() + halfSize;
    }

    /**
     * Checks if a position is outside the viewport bounds.
     *
     * @param mouse the position in viewport space
     * @return {@code true} if outside viewport, {@code false} otherwise
     */
    private boolean isOutsideViewport(Vector2i mouse) {
        return mouse.x < 0 || mouse.y < 0 ||
                mouse.x >= viewportWidth || mouse.y >= viewportHeight;
    }

    /**
     * Extracts reward IDs from class configuration.
     *
     * @param classConfig the class configuration
     * @return list of reward IDs, or empty list if config is null
     */
    private List<String> getRewardIDs(ClassConfig classConfig) {
        if (classConfig == null) {
            return Collections.emptyList();
        }
        return classConfig.rewards().stream()
                .map(ClassConfig.LevelReward::reward)
                .map(Identifier::toString)
                .toList();
    }

    // ==================== Inner Classes ====================

    /**
     * Record containing hover information for skill tooltips.
     *
     * @param title the skill title
     * @param description the skill description
     * @param extraDescription additional description text
     * @param skillId the skill identifier
     */
    public record SkillHoverInfo(
            Text title,
            Text description,
            Text extraDescription,
            String skillId
    ) {}
}