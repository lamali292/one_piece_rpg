// DevilFruitScreen.java
package de.one_piece_api.gui.tabs;

import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.network.DevilFruitPayload;
import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.gui.util.Tab;
import de.one_piece_api.network.SkillClickPacket;
import de.one_piece_api.util.DataGenUtil;
import de.one_piece_api.util.OnePieceCategory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.texture.Scaling;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import net.puffish.skillsmod.client.rendering.TextureBatchedRenderer;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.*;

public class DevilFruitTab implements Tab {
    private static final int PATH_COUNT = 5;
    public static DevilFruitConfig devilFruitConfig = null;

    private  List<Optional<String>> skills = List.of();
    List<TextureData> textureData = List.of();
    private final OnePieceScreen parent;
    public DevilFruitTab(OnePieceScreen parent, Identifier identifier) {
        ClientPlayNetworking.send(new DevilFruitPayload.Request(identifier));
        this.parent = parent;
        updatePaths();
    }

    private static final Vector4fc COLOR_WHITE = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector4fc COLOR_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1f);

    @Override
    public void resize(MinecraftClient client, int width, int height) {

    }

    private record TextureData(State state, Identifier frameTexture, Identifier iconTexture, Vector4fc color, ClientSkillDefinitionConfig skill) { }
    public static class State {
        public static final State FINISHED = new State(null);
        public static final State NOTHING = new State(null);
        private final Skill.State skillState;
        private State(Skill.State skillState) {
            this.skillState = skillState;
        }
        public Skill.State getSkillState() { return skillState; }
    }

    private void updatePaths() {
        AdvancementFrame frame = AdvancementFrame.TASK;
        Identifier defaultFrameTexture = AdvancementObtainedStatus.OBTAINED.getFrameTexture(frame);
        TextureData defaultTexture = new TextureData(State.FINISHED, defaultFrameTexture, null, COLOR_WHITE, null);
        TextureData defaultTexture2 = new TextureData(State.NOTHING, defaultFrameTexture, null, COLOR_GRAY, null);
        if (!parent.hasCategoryData()|| devilFruitConfig == null) {
            textureData = List.of(defaultTexture, defaultTexture, defaultTexture, defaultTexture, defaultTexture);
            return;
        }

        ClientCategoryConfig categoryConfig = parent.getCategoryData().getConfig();
        Map<String, ClientSkillConfig> skillConfig = categoryConfig.skills();


        skills = devilFruitConfig.paths().stream().map(
                a->
                        a.skills().stream().map(DataGenUtil::generateDeterministicId).filter(
                        id->{
                            ClientSkillConfig skill = skillConfig.get(id);
                            if (skill == null) {return false;}
                            Skill.State state = parent.getCategoryData().getSkillState(skill);
                            return state == Skill.State.AFFORDABLE || state == Skill.State.AVAILABLE;
                        }
                ).findFirst()
        ).toList();
        ArrayList<TextureData> newTextureData = new ArrayList<>();
        newTextureData.addAll(skills.stream().map(
                opt->opt.map(skillConfig::get).map(skill->{
                            Skill.State state = parent.getCategoryData().getSkillState(skill);
                            Vector4fc color = switch (state) {
                                case UNLOCKED, AFFORDABLE -> COLOR_WHITE;
                                case AVAILABLE -> new Vector4f(1.0f, 0.5f, 0.5f, 1f);
                                case EXCLUDED, LOCKED -> COLOR_GRAY;
                            };
                            AdvancementObtainedStatus status = switch (state) {
                                case UNLOCKED -> AdvancementObtainedStatus.OBTAINED;
                                case EXCLUDED, LOCKED, AFFORDABLE, AVAILABLE -> AdvancementObtainedStatus.UNOBTAINED;
                            };
                            Identifier frameTexture = status.getFrameTexture(frame);
                            Optional<ClientSkillDefinitionConfig> skillDefinition = categoryConfig.getDefinitionById(skill.definitionId());
                            Optional<Identifier> iconTexture = skillDefinition.map(ClientSkillDefinitionConfig::icon).map(
                                    icon->{
                                        if (icon instanceof ClientIconConfig.TextureIconConfig(Identifier texture) && texture != null) {
                                            return texture;
                                        }
                                        return null;
                                    }
                            );
                            return new TextureData(new State(state) ,frameTexture, iconTexture.orElse(null), color, skillDefinition.orElse(null));
                        }
                ).orElse(defaultTexture)
        ).toList());
        newTextureData.addAll(Collections.nCopies(PATH_COUNT-skills.size(), defaultTexture2));
        textureData = newTextureData;

    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updatePaths();
        MinecraftClient client = MinecraftClient.getInstance();
        int centerX = client.getWindow().getScaledWidth() / 2;
        int contentY = client.getWindow().getScaledHeight() / 2;
        var guiAtlasManager = client.getGuiAtlasManager();
        var textureRenderer = new TextureBatchedRenderer();


        float sizeScale = 1.0f;

        int frameHalfSize = Math.round(13f * sizeScale);
        int frameSize = frameHalfSize * 2;
        int iconHalfSize = Math.round(8f * sizeScale);
        int iconSize = iconHalfSize * 2;
        MatrixStack matrices = context.getMatrices();

        int dist = frameSize + 5;

        for (int i = 0; i < PATH_COUNT; i++) {
            if (i >= textureData.size()) break;
            TextureData texture = textureData.get(i);
            int x = centerX + i * dist - 2 * dist;

            Sprite sprite = guiAtlasManager.getSprite(texture.frameTexture);
            Scaling scaling = guiAtlasManager.getScaling(sprite);
            textureRenderer.emitSprite(
                    context, sprite, scaling,
                    x - frameHalfSize, contentY - frameHalfSize, frameSize, frameSize,
                    texture.color
            );
            if (mouseX >= x - frameHalfSize && mouseX <= x + frameHalfSize &&
                    mouseY >= contentY - frameHalfSize && mouseY <= contentY + frameHalfSize) {
                if (texture.skill != null) {
                    var lines = new ArrayList<OrderedText>();
                    lines.add(texture.skill.title().asOrderedText());
                    lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(
                            texture.skill.description().copy(),
                            Style.EMPTY.withFormatting(Formatting.GRAY)
                    )));
                    if (Screen.hasShiftDown()) {
                        lines.addAll(Tooltip.wrapLines(client, Texts.setStyleIfAbsent(
                                texture.skill.extraDescription().copy(),
                                Style.EMPTY.withFormatting(Formatting.GRAY)
                        )));
                    }
                    if (client.options.advancedItemTooltips) {
                        lines.add(Text.literal(texture.skill.id()).formatted(Formatting.DARK_GRAY).asOrderedText());
                    }
                    parent.setTooltip(lines);
                } else {
                    var lines = new ArrayList<OrderedText>();
                    if (texture.state == State.FINISHED) {
                        lines.add(Text.literal("Alles Skill").formatted(Formatting.DARK_GRAY).asOrderedText());
                    } else if (texture.state == State.NOTHING) {
                        lines.add(Text.literal("Kein Skill").formatted(Formatting.DARK_GRAY).asOrderedText());

                    }
                    parent.setTooltip(lines);
                }

            }
            if (texture.iconTexture != null) {
                matrices.push();
                matrices.translate(0f, 0f, 1f);
                textureRenderer.emitTexture(context, texture.iconTexture, x - iconHalfSize, contentY - iconHalfSize, iconSize, iconSize, COLOR_WHITE);
                matrices.pop();
            }
        }
        textureRenderer.draw();


    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (devilFruitConfig == null) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        int centerX = client.getWindow().getScaledWidth() / 2;
        int contentY = client.getWindow().getScaledHeight() / 2;

        float sizeScale = 1.0f;
        int frameHalfSize = Math.round(13f * sizeScale);
        int frameSize = frameHalfSize * 2;
        int dist = frameSize + 5;

        for (int i = 0; i < 5; i++) {
            int x = centerX + i * dist - 2 * dist;

            // Bounding Box prüfen
            if (mouseX >= x - frameHalfSize && mouseX <= x + frameHalfSize &&
                    mouseY >= contentY - frameHalfSize && mouseY <= contentY + frameHalfSize) {

                // Wenn du z. B. Spell-Namen mit ausgeben willst:
                if (i < devilFruitConfig.paths().size()) {
                    skills.get(i).ifPresent(skillId->{
                        SkillsClientMod.getInstance()
                                .getPacketSender()
                                .send(new SkillClickOutPacket(OnePieceCategory.ID, skillId));
                        //ClientPlayNetworking.send(new SkillClickPacket(OnePieceCategory.ID, skillId));

                        updatePaths();
                        parent.updateLearned();
                        if (client.player != null) {
                            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                        }
                    });
                }

                return true; // handled
            }
        }

        return false;
    }

}