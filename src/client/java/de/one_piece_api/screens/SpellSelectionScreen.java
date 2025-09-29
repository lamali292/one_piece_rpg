package de.one_piece_api.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.interfaces.IOnePiecePlayer;
import de.one_piece_api.network.SetSpellsPayload;
import de.one_piece_api.registries.MyKeys;
import de.one_piece_api.registries.MyShaders;
import de.one_piece_api.util.RenderUtil;
import de.one_piece_api.util.SpellUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.gui.Drawable;
import net.spell_engine.client.gui.HudKeyVisuals;
import net.spell_engine.client.gui.HudRenderHelper;
import net.spell_engine.client.input.Keybindings;
import net.spell_engine.client.input.WrappedKeybinding;
import net.spell_engine.client.util.SpellRender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpellSelectionScreen extends Screen {
    private final ClientPlayerEntity player;
    private static final Identifier BACKGROUND_TEXTURE = OnePieceRPG.id("textures/gui/background_skills.png");
    static final Identifier EMPTY_SLOT_TEXTURE = OnePieceRPG.id("textures/gui/empty.png");

    private final List<RegistryEntry<Spell>> learned;
    private final int size;
    private final int popupHeight, popupWidth;
    private int x, y;


    public SpellSelectionScreen(ClientPlayerEntity player) {
        super(ScreenTexts.EMPTY);
        this.player = player;
        learned = SpellUtil.getLearnedSpells(player);
        size = learned.size();
        int rows = (size / SPELL_SLOTS) + 1;
        int columns = Math.max(Math.min(size, SPELL_SLOTS), 1);
        popupHeight = (rows - 1) * ICON_SPACING + ICON_SIZE + 2 * UI_PADDING;
        popupWidth = (columns - 1) * ICON_SPACING + ICON_SIZE + 2 * UI_PADDING;
    }

    private boolean selectingSpell = false;
    private int targetSlot = -1;

    // Animation variables
    private long animationStartTime = 0;
    private boolean isAnimating = false;
    private static final int ANIMATION_DURATION = 250; // milliseconds
    private float popupOffset = 0f;

    // UI Constants
    private static final int ICON_SIZE = 16;
    private static final int ICON_SPACING = 22;
    private static final int SPELL_SLOTS = 8;
    private static final int UI_PADDING = 6;
    private static final int HOTBAR_OFFSET = 40;

    private static final int HOTBAR_WIDTH = (SPELL_SLOTS - 1) * ICON_SPACING + ICON_SIZE;
    private static final int HOTBAR_HEIGHT = ICON_SIZE + ICON_SPACING;
    private static final int TEXTURE_WIDTH = HOTBAR_WIDTH + 2 * UI_PADDING;
    private static final int TEXTURE_HEIGHT = HOTBAR_HEIGHT + 2 * UI_PADDING;

    private void updateHotbarPosition() {
        x = (this.width - HOTBAR_WIDTH) / 2;
        y = (this.height - HOTBAR_HEIGHT) / 2 - HOTBAR_OFFSET;
    }


    record SpellViewModel(Identifier iconId, boolean learned) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (client == null) return;




        List<RegistryEntry<Spell>> playerSpells = SpellUtil.getPlayerSpells(player);
        List<SpellViewModel> viewModels = new ArrayList<>();
        for (RegistryEntry<Spell> spell  : playerSpells) {
            if (spell == null) {
                viewModels.add(new SpellViewModel(null, true));
            } else {
                Identifier spellId = Identifier.of(spell.getIdAsString());
                Identifier iconId = SpellRender.iconTexture(spellId);
                SpellViewModel viewModel = new SpellViewModel(iconId, learned.contains(spell));
                viewModels.add(viewModel);
            }
        }

        TextRenderer textRenderer = client.inGameHud.getTextRenderer();
        int hoveredHotbarSlot = getHoveredSlot(mouseX, mouseY);


        updateHotbarPosition();
        updateAnimation();
        if (selectingSpell || isAnimating) {
            renderAnimatedSpellPopup(context, mouseX, mouseY);
        }
        context.drawTexture(BACKGROUND_TEXTURE, x - UI_PADDING, y - UI_PADDING, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, 256, 256);
        drawIcons(context, viewModels, new Vec2f(x, y + ICON_SPACING), hoveredHotbarSlot);
        drawKeybindings(context, textRenderer, new Vec2f(x, y));
        RegistryEntry<Spell> hoveredSpell = hoveredHotbarSlot != -1 ? playerSpells.get(hoveredHotbarSlot) : null;
        if (!selectingSpell && hoveredSpell != null) {
            renderSpellTooltip(context, mouseX, mouseY, hoveredSpell);
        }

    }

    private void renderSpellTooltip(DrawContext context, int mouseX, int mouseY, RegistryEntry<Spell> spell) {
        if (spell == null || client == null) return;

        String spellId = spell.getIdAsString();
        List<Text> header = new ArrayList<>();

        // Spell name
        String nameKey = "spell." + spellId.replace(":", ".") + ".name";
        Text title = Text.translatable(nameKey).formatted(Formatting.WHITE);
        header.add(title);

        if (hasShiftDown()) {
            // Spell description
            String descKey = "spell." + spellId.replace(":", ".") + ".description";
            Text descText = Text.translatable(descKey).formatted(Formatting.GRAY);

            // Wrap preserving styles
            List<OrderedText> wrapped = client.textRenderer.wrapLines(descText, 200);

            // Convert: header (List<Text>) + wrapped (List<OrderedText>)
            List<OrderedText> tooltipLines = new ArrayList<>();
            tooltipLines.addAll(header.stream().map(Text::asOrderedText).toList());
            tooltipLines.addAll(wrapped);

            // Draw combined tooltip
            context.drawOrderedTooltip(client.textRenderer, tooltipLines, mouseX, mouseY);
        } else {
            // Just the name
            context.drawTooltip(client.textRenderer, header, mouseX, mouseY);
        }
    }


    private void updateAnimation() {
        if (isAnimating) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - animationStartTime;
            float progress = Math.min(elapsed / (float) ANIMATION_DURATION, 1.0f);
            float easedProgress = 1 - (float) Math.pow(1 - progress, 3);
            if (selectingSpell) {
                popupOffset = -(1 - easedProgress) * popupHeight;
            } else {
                popupOffset = -easedProgress * popupHeight;
            }
            if (progress >= 1.0f) {
                isAnimating = false;
                popupOffset = selectingSpell ? 0 : popupHeight;
            }
        }
    }

    private void startAnimation() {
        animationStartTime = System.currentTimeMillis();
        isAnimating = true;
    }

    private int getHoveredSlot(double mouseX, double mouseY) {
        if (selectingSpell) return -1;
        return getClickedSpellIndex(mouseX, mouseY);
    }

    private void drawKeybindings(DrawContext context, TextRenderer textRenderer, Vec2f hotbarPos) {
        if (client == null) return;
        List<WrappedKeybinding> bind = Keybindings.Wrapped.all();
        for (int i = 0; i < Math.min(SPELL_SLOTS, bind.size()); i++) {
            WrappedKeybinding wrappedKeybinding = bind.get(i);
            var unwrapped = wrappedKeybinding.get(client.options);
            if (unwrapped == null) continue;

            var keybinding = unwrapped.keyBinding();
            var model = HudRenderHelper.SpellHotBarWidget.KeyBindingViewModel.from(keybinding);

            int x = (int) (hotbarPos.x + (i * ICON_SPACING));
            int y = (int) (hotbarPos.y) + 6;

            drawKeybinding(context, textRenderer, model, x, y);
        }
    }

    private void renderAnimatedSpellPopup(DrawContext context, int mouseX, int mouseY) {
        int x0 = (this.width - popupWidth) / 2;
        int y0 = (int) (y - 3 + HOTBAR_HEIGHT + UI_PADDING + popupOffset);
        int y1 = y - 3 + HOTBAR_HEIGHT + UI_PADDING;
        // Draw popup background with border
        int border = 4;
        context.enableScissor(x0, y1, x0 + popupWidth, y1 + popupHeight);
        context.drawTexture(BACKGROUND_TEXTURE, x0, y0, 0, 50, popupWidth - border, popupHeight - border, 256, 256);
        context.drawTexture(BACKGROUND_TEXTURE, x0, y0 + popupHeight - border, 0, 176 - border, popupWidth - border, border, 256, 256);
        context.drawTexture(BACKGROUND_TEXTURE, x0 + popupWidth - border, y0, 182 - border, 50, border, popupHeight - border, 256, 256);
        context.drawTexture(BACKGROUND_TEXTURE, x0 + popupWidth - border, y0 + popupHeight - border, 182 - border, 176 - border, border, border, 256, 256);

        x0 += UI_PADDING;
        y0 += UI_PADDING;

        int hoveredSpell = getClickedPopupSpellIndex(mouseX, mouseY);
        if (size == 0) {
            context.drawTexture(EMPTY_SLOT_TEXTURE, x0, y0, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }


        for (int i = 0; i < size; i++) {
            int row = i / SPELL_SLOTS;
            int col = i % SPELL_SLOTS;
            int x = x0 + col * ICON_SPACING;
            int y = y0 + row * ICON_SPACING;

            // Add hover effect for popup spells
            if (i == hoveredSpell && selectingSpell) {
                context.fill(x - 2, y - 2, x + ICON_SIZE + 2, y + ICON_SIZE + 2, 0x80FFFFFF);
            }

            var spell = learned.get(i);
            if (spell != null) {
                Identifier icon = SpellRender.iconTexture(Identifier.of(spell.getIdAsString()));
                context.drawTexture(icon, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }
        }
        context.disableScissor();
        // Render popup tooltip
        if (hoveredSpell != -1 && selectingSpell && hoveredSpell < learned.size()) {
            renderSpellTooltip(context, mouseX, mouseY, learned.get(hoveredSpell));
        }


    }


    private int getClickedPopupSpellIndex(double mouseX, double mouseY) {
        if (!selectingSpell) return -1;
        int x0 = (this.width - popupWidth) / 2;
        int y0 = (int) (y - 3 + HOTBAR_HEIGHT + UI_PADDING + popupOffset);

        x0 += UI_PADDING;
        y0 += UI_PADDING;

        for (int i = 0; i < size; i++) {
            int row = i / SPELL_SLOTS;
            int col = i % SPELL_SLOTS;
            int x = x0 + col * ICON_SPACING;
            int y = y0 + row * ICON_SPACING;

            if (mouseX >= x && mouseX <= x + ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ICON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (client == null || client.player == null) return false;

        if (button == 0) { // Left click
            if (selectingSpell) {
                int clickedLearned = getClickedPopupSpellIndex(mouseX, mouseY);
                if (clickedLearned != -1 && targetSlot != -1) {
                    updatePlayerSpell(targetSlot, SpellUtil.getLearnedSpells(player).get(clickedLearned));
                    selectingSpell = false;
                    targetSlot = -1;
                    startAnimation(); // Start closing animation
                    playClickSound();
                    return true;
                }
                // Click outside popup to close
                selectingSpell = false;
                targetSlot = -1;
                startAnimation();
                return true;
            } else {
                int clickedActive = getClickedSpellIndex(mouseX, mouseY);
                if (clickedActive != -1) {
                    selectingSpell = true;
                    targetSlot = clickedActive;
                    startAnimation(); // Start opening animation
                    playClickSound();
                    return true;
                }
            }
        } else if (button == 1 && !selectingSpell) { // Right-click to remove
            int clickedActive = getClickedSpellIndex(mouseX, mouseY);
            if (clickedActive != -1 && player instanceof IOnePiecePlayer onePiecePlayer) {
                if (removeSpellFromSlot(onePiecePlayer, clickedActive)) {
                    playClickSound();
                    targetSlot = -1;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (client != null && MyKeys.getKeyBinding(MyKeys.OPEN_SKILLS_KEY).matchesKey(keyCode, scanCode)) {
            client.setScreen(null);
            return true;
        }

        // ESC to close popup
        if (selectingSpell && keyCode == 256) { // GLFW.GLFW_KEY_ESCAPE
            selectingSpell = false;
            targetSlot = -1;
            startAnimation();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playClickSound() {
        if (client != null && client.player != null) {
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
    }

    private boolean removeSpellFromSlot(IOnePiecePlayer onePiecePlayer, int slotIndex) {
        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());
        while (current.size() <= slotIndex) current.add(null);

        if (current.get(slotIndex) != null) {
            RegistryEntry<Spell> oldValue = current.get(slotIndex);
            current.set(slotIndex, null);

            List<String> spellIds = current.stream()
                    .map(spell -> spell != null ? spell.getIdAsString() : "")
                    .toList();

            if (oldValue != null) {
                onePiecePlayer.onepiece$setSelectedSpellIds(spellIds);
                ClientPlayNetworking.send(new SetSpellsPayload(spellIds));
                return true;
            }
        }
        return false;
    }

    private int getClickedSpellIndex(double mouseX, double mouseY) {
        for (int i = 0; i < SPELL_SLOTS; i++) {
            int x0 = x + (ICON_SPACING * i);
            int y0 = y + ICON_SPACING;
            if (mouseX >= x0 && mouseX <= x0 + ICON_SIZE &&
                    mouseY >= y0 && mouseY <= y0 + ICON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private void updatePlayerSpell(int slotIndex, RegistryEntry<Spell> newSpell) {
        if (!(player instanceof IOnePiecePlayer onePiecePlayer)) return;

        List<RegistryEntry<Spell>> current = new ArrayList<>(onePiecePlayer.onepiece$getSelectedSpells());
        while (current.size() <= slotIndex) current.add(null);

        RegistryEntry<Spell> oldValue = current.get(slotIndex);
        if (Objects.equals(oldValue, newSpell)) return;

        int existingIndex = findSpellIndex(current, newSpell);
        if (existingIndex != -1 && existingIndex != slotIndex) {
            current.set(existingIndex, oldValue);
        }

        current.set(slotIndex, newSpell);

        List<String> spellIds = current.stream()
                .map(spell -> spell != null ? spell.getIdAsString() : "")
                .toList();

        onePiecePlayer.onepiece$setSelectedSpellIds(spellIds);
        ClientPlayNetworking.send(new SetSpellsPayload(spellIds));
    }

    private int findSpellIndex(List<RegistryEntry<Spell>> spells, RegistryEntry<Spell> targetSpell) {
        for (int i = 0; i < spells.size(); i++) {
            if (Objects.equals(spells.get(i), targetSpell)) {
                return i;
            }
        }
        return -1;
    }

    private void drawIcons(DrawContext context, List<SpellViewModel> viewModel,
                           Vec2f origin, int highlightedIndex) {
        context.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();

        for (int i = 0; i < viewModel.size(); i++) {
            int x = (int) (origin.x) + (ICON_SPACING * i);
            int y = (int) (origin.y);
            if (i == highlightedIndex) {
                context.fill(x - 2, y - 2, x + ICON_SIZE + 2, y + ICON_SIZE + 2, 0x80FFFFFF);
            } else if (i == targetSlot) {
                context.fill(x - 2, y - 2, x + ICON_SIZE + 2, y + ICON_SIZE + 2, 0x8000FF00);
            }
            SpellViewModel spell = viewModel.get(i);
            if (spell != null && spell.iconId() != null) {
                if (!spell.learned()) {
                    RenderUtil.drawTexture(context, MyShaders::getGrayscaleShaderProgram, spell.iconId(), x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                } else {
                    context.drawTexture(spell.iconId(), x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                }
            } else {
                context.drawTexture(EMPTY_SLOT_TEXTURE, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

        }

        RenderSystem.disableBlend();
    }




    private static void drawKeybinding(DrawContext context, TextRenderer textRenderer,
                                       HudRenderHelper.SpellHotBarWidget.KeyBindingViewModel keybinding, int x, int y) {
        if (keybinding.drawable() != null) {
            keybinding.drawable().draw(context, x, y, Drawable.Anchor.LEADING, Drawable.Anchor.TRAILING);
        } else {
            int textLength = textRenderer.getWidth(keybinding.label());
            int xOffset = textLength / 2;
            int centeredX = x + (ICON_SIZE / 2);
            int centeredY = y + (ICON_SIZE / 2);

            HudKeyVisuals.buttonLeading.draw(context, centeredX - xOffset, centeredY, Drawable.Anchor.TRAILING, Drawable.Anchor.TRAILING);
            HudKeyVisuals.buttonCenter.drawFlexibleWidth(context, centeredX - xOffset, centeredY, textLength, Drawable.Anchor.TRAILING);
            HudKeyVisuals.buttonTrailing.draw(context, centeredX + xOffset, centeredY, Drawable.Anchor.LEADING, Drawable.Anchor.TRAILING);
            context.drawCenteredTextWithShadow(textRenderer, keybinding.label(), centeredX, centeredY - 10, 0xFFFFFF);
        }
    }
}