package de.one_piece_api.gui.widgets;

import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.util.OnePieceCategory;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.render.item.ItemRenderer;
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
 * Widget representing a single skill path with its own animation state
 */
public class SkillPathWidget implements Drawable, Element {
    private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);
    private static final long ANIMATION_DURATION_MS = 300;

    private final OnePieceScreen parent;
    private final List<String> pathSkillIds;
    private final ClientCategoryConfig categoryConfig;
    private final Map<String, ClientSkillConfig> skillConfig;

    private int centerX;
    private int centerY;
    private boolean focused = false;

    // Animation state
    private float animationProgress = 0f;
    private boolean isAnimating = false;
    private long animationStartTime = 0;
    private int animationStartIndex = -1;

    // Layout constants
    private static final int FRAME_HALF_SIZE = 13;
    private static final int FRAME_SIZE = FRAME_HALF_SIZE * 2;
    private static final int ICON_HALF_SIZE = 8;
    private static final int ICON_SIZE = ICON_HALF_SIZE * 2;
    public static final int VERTICAL_SPACING = FRAME_SIZE + 10;

    public SkillPathWidget(OnePieceScreen parent, int pathIndex, List<String> pathSkillIds,
                           ClientCategoryConfig categoryConfig, Map<String, ClientSkillConfig> skillConfig) {
        this.parent = parent;
        this.pathSkillIds = new ArrayList<>(pathSkillIds);
        this.categoryConfig = categoryConfig;
        this.skillConfig = skillConfig;
    }

    public void updateSkillData() {
        // Preserves animation state while refreshing data
    }

    public void setPosition(int x, int y) {
        this.centerX = x;
        this.centerY = y;
    }

    /**
     * Renders connections between consecutive skills in this path
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

    private int getConnectionFillColor(Skill.State fromState, Skill.State toState) {
        if (toState == Skill.State.UNLOCKED) {
            return 0xFFb37d12;
        }
        if (fromState == Skill.State.UNLOCKED &&
                (toState == Skill.State.AFFORDABLE || toState == Skill.State.AVAILABLE)) {
            return 0xFF808080;
        }
        if (toState == Skill.State.LOCKED || toState == Skill.State.EXCLUDED) {
            return 0xFF3A3A3A;
        }
        return 0xFF808080;
    }

    private int getConnectionStrokeColor(Skill.State fromState, Skill.State toState) {
        if (toState == Skill.State.UNLOCKED) {
            return 0xFFbf8c26;
        }
        if (fromState == Skill.State.UNLOCKED &&
                (toState == Skill.State.AFFORDABLE || toState == Skill.State.AVAILABLE)) {
            return 0xFF808080;
        }
        if (toState == Skill.State.LOCKED || toState == Skill.State.EXCLUDED) {
            return 0xFF3D3D3D;
        }
        return 0xFF808080;
    }

    private int applyAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int getCurrentSkillIndex() {
        for (int i = 0; i < pathSkillIds.size(); i++) {
            String skillId = pathSkillIds.get(i);
            ClientSkillConfig skill = skillConfig.get(skillId);
            if (skill == null) continue;

            Skill.State state = parent.getCategoryData().getSkillState(skill);
            if (state == Skill.State.AFFORDABLE || state == Skill.State.AVAILABLE) {
                return i;
            }
        }
        return pathSkillIds.size() - 1;
    }

    private Optional<String> getCurrentSkillId() {
        int index = getCurrentSkillIndex();
        if (index >= 0 && index < pathSkillIds.size()) {
            return Optional.of(pathSkillIds.get(index));
        }
        return Optional.empty();
    }

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
                    centerX, y, 1.0f, mouseX, mouseY);
        }

        textureRenderer.draw();
        itemRenderer.draw();
    }

    private void renderSkill(DrawContext context, TextureBatchedRenderer textureRenderer,
                             ItemBatchedRenderer itemRenderer,
                             GuiAtlasManager guiAtlasManager,
                             MatrixStack matrices, ClientSkillConfig skill,
                             int x, int y, float alpha, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        Skill.State state = parent.getCategoryData().getSkillState(skill);

        // Determine colors
        Vector4fc baseColor = switch (state) {
            case UNLOCKED, AFFORDABLE -> COLOR_WHITE;
            case AVAILABLE -> new Vector4f(0.8f, 0.4f, 0.4f, alpha);
            case EXCLUDED, LOCKED -> COLOR_GRAY;
        };
        Vector4fc color = new Vector4f(baseColor.x(), baseColor.y(), baseColor.z(), alpha);

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
                Vector4fc iconColor = new Vector4f(1f, 1f, 1f, alpha);
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

    private boolean isMouseOverSkill(double mouseX, double mouseY, int skillX, int skillY) {
        return mouseX >= skillX - FRAME_HALF_SIZE && mouseX <= skillX + FRAME_HALF_SIZE &&
                mouseY >= skillY - FRAME_HALF_SIZE && mouseY <= skillY + FRAME_HALF_SIZE;
    }

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

    private void startAnimation() {
        isAnimating = true;
        animationStartTime = System.currentTimeMillis();
        animationProgress = 0f;
        animationStartIndex = getCurrentSkillIndex();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= centerX - FRAME_HALF_SIZE && mouseX <= centerX + FRAME_HALF_SIZE &&
                mouseY >= centerY - FRAME_HALF_SIZE && mouseY <= centerY + FRAME_HALF_SIZE;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }
}