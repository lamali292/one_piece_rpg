package de.one_piece_api.screen.widget.main.devilfruit;

import de.one_piece_api.screen.OnePieceScreen;
import de.one_piece_api.util.OnePieceCategory;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.texture.GuiAtlasManager;
import net.minecraft.client.texture.Scaling;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import net.puffish.skillsmod.client.rendering.ItemBatchedRenderer;
import net.puffish.skillsmod.client.rendering.TextureBatchedRenderer;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Widget representing a single vertical skill path with smooth animation.
 * <p>
 * This widget displays a progression of skills arranged vertically, with the
 * current unlockable skill centered in the viewport. When a skill is unlocked,
 * it animates smoothly upward to reveal the next skill in the path.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Vertical skill progression with centered current skill</li>
 *     <li>Smooth cubic ease-out animation when unlocking skills</li>
 *     <li>Color-coded skill states (unlocked, affordable, locked, etc.)</li>
 *     <li>Connection lines between sequential skills</li>
 *     <li>Hover tooltips for all visible skills</li>
 *     <li>Click to unlock affordable skills</li>
 * </ul>
 *
 * <h2>Animation System:</h2>
 * When a skill is unlocked, the entire path smoothly shifts upward over
 * {@link #ANIMATION_DURATION_MS} milliseconds, centering the next skill.
 * Animation uses cubic ease-out for a natural feel.
 *
 * @see Drawable
 * @see Element
 * @see Skill
 */
public class SkillPathWidget implements Drawable, Element {

    // ==================== Constants ====================

    /** Color for unlocked and affordable skills (white) */
    private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);

    /** Color for locked and excluded skills (dark gray) */
    private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);

    /** Duration of the skill unlock animation in milliseconds */
    private static final long ANIMATION_DURATION_MS = 300;

    /** Half size of the skill frame in pixels */
    private static final int FRAME_HALF_SIZE = 13;

    /** Full size of the skill frame in pixels */
    private static final int FRAME_SIZE = FRAME_HALF_SIZE * 2;

    /** Half size of the skill icon in pixels */
    private static final int ICON_HALF_SIZE = 8;

    /** Full size of the skill icon in pixels */
    private static final int ICON_SIZE = ICON_HALF_SIZE * 2;

    /** Vertical spacing between skills in pixels (must match DevilFruitTab constant) */
    public static final int VERTICAL_SPACING = FRAME_SIZE + 10;

    // ==================== Fields ====================

    /** Parent screen for callback and data access */
    private final OnePieceScreen parent;

    /** List of skill IDs in this path, ordered from first to last */
    private final List<String> pathSkillIds;

    /** Category configuration containing skill definitions */
    private final ClientCategoryConfig categoryConfig;

    /** Map of skill IDs to their configurations */
    private final Map<String, ClientSkillConfig> skillConfig;

    /** Center x-coordinate of the skill path */
    private int centerX;

    /** Center y-coordinate of the current skill */
    private int centerY;

    /** Whether this widget has input focus */
    private boolean focused = false;

    // Animation state
    /** Current animation progress (0.0 to 1.0) */
    private float animationProgress = 0f;

    /** Whether an animation is currently playing */
    private boolean isAnimating = false;

    /** Timestamp when the current animation started */
    private long animationStartTime = 0;

    /** Index of the skill that was current when animation started */
    private int animationStartIndex = -1;

    // ==================== Constructor ====================

    /**
     * Creates a new skill path widget.
     *
     * @param parent the parent OnePiece screen
     * @param pathIndex the index of this path (unused but kept for compatibility)
     * @param pathSkillIds list of skill IDs in this path, ordered from first to last
     * @param categoryConfig category configuration containing skill definitions
     * @param skillConfig map of skill IDs to their configurations
     */
    public SkillPathWidget(OnePieceScreen parent, int pathIndex, List<String> pathSkillIds,
                           ClientCategoryConfig categoryConfig, Map<String, ClientSkillConfig> skillConfig) {
        this.parent = parent;
        this.pathSkillIds = new ArrayList<>(pathSkillIds);
        this.categoryConfig = categoryConfig;
        this.skillConfig = skillConfig;
    }

    // ==================== State Management ====================

    /**
     * Updates skill data while preserving animation state.
     * <p>
     * This method can be called when skill states change without
     * interrupting ongoing animations.
     */
    public void updateSkillData() {
        // Preserves animation state while refreshing data
    }

    /**
     * Sets the center position of this skill path.
     * <p>
     * The current skill will be rendered centered at these coordinates,
     * with other skills positioned relative to it.
     *
     * @param x the center x-coordinate
     * @param y the center y-coordinate
     */
    public void setPosition(int x, int y) {
        this.centerX = x;
        this.centerY = y;
    }

    // ==================== Connection Rendering ====================

    /**
     * Renders connection lines between consecutive skills in this path.
     * <p>
     * Connections are color-coded based on skill states:
     * <ul>
     *     <li>Gold: Connection to unlocked skill</li>
     *     <li>Gray: Connection to available/affordable skill from unlocked</li>
     *     <li>Dark: Connection to locked/excluded skill</li>
     * </ul>
     * Connections respect the current animation state for smooth transitions.
     *
     * @param context the drawing context
     * @param renderer the connection renderer for batched rendering
     * @param categoryConfig the category configuration (unused but kept for compatibility)
     */
    public void renderConnections(DrawContext context,
                                  net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer renderer,
                                  ClientCategoryConfig categoryConfig) {
        int renderIndex = isAnimating ? animationStartIndex : getCurrentSkillIndex();
        float animationOffset = isAnimating ? animationProgress * VERTICAL_SPACING : 0f;

        // Render ALL connections in the path
        for (int i = 0; i < pathSkillIds.size() - 1; i++) {
            String skillId = pathSkillIds.get(i);
            String nextSkillId = pathSkillIds.get(i + 1);

            ClientSkillConfig skill = skillConfig.get(skillId);
            ClientSkillConfig nextSkill = skillConfig.get(nextSkillId);

            if (skill == null || nextSkill == null) continue;

            // Calculate positions relative to current skill
            int offset = renderIndex - i;
            int currentY = centerY + offset * VERTICAL_SPACING + (int) animationOffset;
            int nextY = centerY + (offset - 1) * VERTICAL_SPACING + (int) animationOffset;

            // Determine connection colors
            Skill.State currentState = parent.getCategoryData().getSkillState(skill);
            Skill.State nextState = parent.getCategoryData().getSkillState(nextSkill);

            int fillColor = getConnectionFillColor(currentState, nextState);
            int strokeColor = getConnectionStrokeColor(currentState, nextState);

            renderer.emitConnection(
                    context,
                    centerX, currentY,
                    centerX, nextY,
                    true,
                    fillColor,
                    strokeColor
            );
        }
    }

    /**
     * Gets the fill color for a connection based on skill states.
     *
     * @param fromState the state of the skill at the start of the connection
     * @param toState the state of the skill at the end of the connection
     * @return ARGB color value for the connection fill
     */
    private int getConnectionFillColor(Skill.State fromState, Skill.State toState) {
        if (toState == Skill.State.UNLOCKED) {
            return 0xFFb37d12; // Gold for unlocked
        }
        if (fromState == Skill.State.UNLOCKED &&
                (toState == Skill.State.AFFORDABLE || toState == Skill.State.AVAILABLE)) {
            return 0xFF808080; // Gray for available from unlocked
        }
        if (toState == Skill.State.LOCKED || toState == Skill.State.EXCLUDED) {
            return 0xFF3A3A3A; // Dark gray for locked
        }
        return 0xFF808080; // Default gray
    }

    /**
     * Gets the stroke color for a connection based on skill states.
     *
     * @param fromState the state of the skill at the start of the connection
     * @param toState the state of the skill at the end of the connection
     * @return ARGB color value for the connection stroke
     */
    private int getConnectionStrokeColor(Skill.State fromState, Skill.State toState) {
        if (toState == Skill.State.UNLOCKED) {
            return 0xFFbf8c26; // Brighter gold for unlocked
        }
        if (fromState == Skill.State.UNLOCKED &&
                (toState == Skill.State.AFFORDABLE || toState == Skill.State.AVAILABLE)) {
            return 0xFF808080; // Gray for available from unlocked
        }
        if (toState == Skill.State.LOCKED || toState == Skill.State.EXCLUDED) {
            return 0xFF3D3D3D; // Dark gray for locked
        }
        return 0xFF808080; // Default gray
    }

    /**
     * Applies an alpha value to a color.
     *
     * @param color the ARGB color value
     * @param alpha the alpha multiplier (0.0 to 1.0)
     * @return the color with adjusted alpha channel
     */
    private int applyAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ==================== Skill Navigation ====================

    /**
     * Gets the index of the current skill (next affordable or last skill).
     * <p>
     * Finds the first skill that is affordable or available. If none found,
     * returns the last skill in the path (fully progressed).
     *
     * @return the index of the current skill (0-based)
     */
    private int getCurrentSkillIndex() {
        ClientCategoryData categoryData = parent.getCategoryData();
        if (categoryData == null || skillConfig == null) return 0;
        for (int i = 0; i < pathSkillIds.size(); i++) {
            String skillId = pathSkillIds.get(i);
            ClientSkillConfig skill = skillConfig.get(skillId);
            if (skill == null) continue;

            Skill.State state = categoryData.getSkillState(skill);
            if (state == Skill.State.AFFORDABLE || state == Skill.State.AVAILABLE) {
                return i;
            }
        }
        return pathSkillIds.size() - 1;
    }

    /**
     * Gets the ID of the current skill.
     *
     * @return an {@link Optional} containing the current skill ID, or empty if invalid
     */
    private Optional<String> getCurrentSkillId() {
        int index = getCurrentSkillIndex();
        if (index >= 0 && index < pathSkillIds.size()) {
            return Optional.of(pathSkillIds.get(index));
        }
        return Optional.empty();
    }

    // ==================== Rendering ====================

    /**
     * Renders all skills in the path with animation support.
     * <p>
     * Updates the animation state and renders all skills positioned relative
     * to the current skill at the center. Skills above and below are rendered
     * with vertical offset based on {@link #VERTICAL_SPACING}.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time (unused, animation is time-based)
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        var guiAtlasManager = client.getGuiAtlasManager();
        var textureRenderer = new TextureBatchedRenderer();
        MatrixStack matrices = context.getMatrices();

        // Update animation
        if (isAnimating) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - animationStartTime;
            animationProgress = Math.min(1.0f, elapsed / (float) ANIMATION_DURATION_MS);

            // Smooth easing (ease-out cubic)
            animationProgress = 1 - (float) Math.pow(1 - animationProgress, 3);

            if (animationProgress >= 1.0f) {
                isAnimating = false;
                animationProgress = 0f;
                animationStartIndex = -1;
            }
        }

        int renderIndex = isAnimating ? animationStartIndex : getCurrentSkillIndex();
        float animationOffset = isAnimating ? animationProgress * VERTICAL_SPACING : 0f;
        ItemBatchedRenderer itemRenderer = new ItemBatchedRenderer();

        // Render ALL skills in the path
        for (int i = 0; i < pathSkillIds.size(); i++) {
            String skillId = pathSkillIds.get(i);
            ClientSkillConfig skill = skillConfig.get(skillId);
            if (skill == null) continue;

            // Calculate position relative to current skill
            int offset = renderIndex - i;
            int y = centerY + offset * VERTICAL_SPACING + (int) animationOffset;

            // Render skill frame and icon
            renderSkill(context, textureRenderer, itemRenderer, guiAtlasManager, matrices, skill,
                    centerX, y, mouseX, mouseY);
        }

        textureRenderer.draw();
        itemRenderer.draw();
    }

    /**
     * Renders a single skill including its frame, icon, and tooltip.
     * <p>
     * Skill appearance is determined by its state:
     * <ul>
     *     <li>Unlocked/Affordable: White frame</li>
     *     <li>Available: Red-tinted frame</li>
     *     <li>Locked/Excluded: Gray frame</li>
     * </ul>
     *  @param context the drawing context
     *
     * @param textureRenderer renderer for batched texture drawing
     * @param itemRenderer    renderer for batched item drawing
     * @param guiAtlasManager manager for GUI sprites and textures
     * @param matrices        matrix stack for transformations
     * @param skill           the skill configuration to render
     * @param x               the x-coordinate of the skill center
     * @param y               the y-coordinate of the skill center
     * @param mouseX          the mouse x-coordinate
     * @param mouseY          the mouse y-coordinate
     */
    private void renderSkill(DrawContext context, TextureBatchedRenderer textureRenderer,
                             ItemBatchedRenderer itemRenderer,
                             GuiAtlasManager guiAtlasManager,
                             MatrixStack matrices, ClientSkillConfig skill,
                             int x, int y, int mouseX, int mouseY) {
        Skill.State state = parent.getCategoryData().getSkillState(skill);

        // Determine colors based on skill state
        Vector4fc baseColor = switch (state) {
            case UNLOCKED, AFFORDABLE -> COLOR_WHITE;
            case AVAILABLE -> new Vector4f(0.8f, 0.4f, 0.4f,  1.0f); // Red tint
            case EXCLUDED, LOCKED -> COLOR_GRAY;
        };
        Vector4fc color = new Vector4f(baseColor.x(), baseColor.y(), baseColor.z(), 1.0f);

        // Render frame
        AdvancementObtainedStatus status = state == Skill.State.UNLOCKED ?
                AdvancementObtainedStatus.OBTAINED : AdvancementObtainedStatus.UNOBTAINED;
        Identifier frameTexture = status.getFrameTexture(AdvancementFrame.TASK);

        Sprite sprite = guiAtlasManager.getSprite(frameTexture);
        Scaling scaling = guiAtlasManager.getScaling(sprite);
        textureRenderer.emitSprite(context, sprite, scaling,
                x - FRAME_HALF_SIZE, y - FRAME_HALF_SIZE, FRAME_SIZE, FRAME_SIZE, color);

        // Render icon
        categoryConfig.getDefinitionById(skill.definitionId()).ifPresent(definition -> {
            matrices.push();
            matrices.translate(0f, 0f, 1f);
            if (definition.icon() instanceof ClientIconConfig.TextureIconConfig(Identifier texture)) {
                Vector4fc iconColor = new Vector4f(1f, 1f, 1f, (float) 1.0);
                textureRenderer.emitTexture(context, texture,
                        x - ICON_HALF_SIZE, y - ICON_HALF_SIZE, ICON_SIZE, ICON_SIZE, iconColor);
            } else if (definition.icon() instanceof ClientIconConfig.ItemIconConfig(ItemStack item)) {
                itemRenderer.emitItem(context, item, x, y);
            }
            matrices.pop();

            // Show tooltip on hover for ANY skill, not just center
            if (!isAnimating && isMouseOverSkill(mouseX, mouseY, x, y)) {
                parent.setHoveredSkillInfo(
                        definition.title(),
                        definition.description(),
                        definition.extraDescription(),
                        Text.literal(skill.id())
                );
            }
        });
    }

    /**
     * Checks if the mouse is over a specific skill.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param skillX the skill center x-coordinate
     * @param skillY the skill center y-coordinate
     * @return {@code true} if mouse is within skill frame bounds, {@code false} otherwise
     */
    private boolean isMouseOverSkill(double mouseX, double mouseY, int skillX, int skillY) {
        return mouseX >= skillX - FRAME_HALF_SIZE && mouseX <= skillX + FRAME_HALF_SIZE &&
                mouseY >= skillY - FRAME_HALF_SIZE && mouseY <= skillY + FRAME_HALF_SIZE;
    }

    // ==================== Input Handling ====================

    /**
     * Handles mouse click events on the centered skill.
     * <p>
     * If the current (centered) skill is affordable and clicked:
     * <ol>
     *     <li>Sends unlock packet to server</li>
     *     <li>Starts animation if not at the last skill</li>
     *     <li>Plays click sound</li>
     * </ol>
     * Clicks are ignored during animation to prevent spam.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (only left click is handled)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isAnimating) {
            return false;
        }

        // Check if clicking on the current (center) skill
        if (isMouseOver(mouseX, mouseY)) {
            return getCurrentSkillId().map(skillId -> {
                ClientSkillConfig skill = skillConfig.get(skillId);
                if (skill == null) return false;

                Skill.State state = parent.getCategoryData().getSkillState(skill);
                if (state != Skill.State.AFFORDABLE) {
                    return false;
                }

                // Send packet to unlock skill
                SkillsClientMod.getInstance()
                        .getPacketSender()
                        .send(new SkillClickOutPacket(OnePieceCategory.ID, skillId));

                // Only animate if not at the last skill
                int currentIndex = getCurrentSkillIndex();
                if (currentIndex < pathSkillIds.size() - 1) {
                    startAnimation();
                }

                // Play sound
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                }

                return true;
            }).orElse(false);
        }

        return false;
    }

    /**
     * Starts the skill unlock animation.
     * <p>
     * Records the current time and skill index, then begins animating
     * the path upward to center the next skill.
     */
    private void startAnimation() {
        isAnimating = true;
        animationStartTime = System.currentTimeMillis();
        animationProgress = 0f;
        animationStartIndex = getCurrentSkillIndex();
    }

    /**
     * Checks if the mouse is over the centered skill.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @return {@code true} if mouse is within the centered skill bounds, {@code false} otherwise
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= centerX - FRAME_HALF_SIZE && mouseX <= centerX + FRAME_HALF_SIZE &&
                mouseY >= centerY - FRAME_HALF_SIZE && mouseY <= centerY + FRAME_HALF_SIZE;
    }

    // ==================== Element Interface ====================

    /**
     * Sets focus state for this widget.
     *
     * @param focused the focus state
     */
    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /**
     * Checks if this widget has focus.
     *
     * @return {@code true} if focused, {@code false} otherwise
     */
    @Override
    public boolean isFocused() {
        return focused;
    }
}