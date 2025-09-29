package de.one_piece_api.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.interfaces.IOnePiecePlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.spell_engine.client.gui.Drawable;
import net.spell_engine.client.gui.HudKeyVisuals;
import net.spell_engine.client.gui.HudRenderHelper;
import net.spell_engine.client.util.TextureFile;

// net.spell_engine.client.gui.HudRenderHelper
public class CustomSpellHotBarWidget {
    private static final TextureFile HOTBAR = new TextureFile(Identifier.of("textures/gui/sprites/hud/hotbar.png"), 182, 22);
    private static final int slotHeight = 22;
    private static final int slotWidth = 20;

    private static float combatModeAlpha = 0f; // 0 = hidden, 1 = fully visible
    private static final float ANIMATION_SPEED = 0.1f;

    public static void render(DrawContext context, int screenWidth, int screenHeight, HudRenderHelper.SpellHotBarWidget.ViewModel viewModel) {

        MinecraftClient client = MinecraftClient.getInstance();

        boolean combat = client.player instanceof IOnePiecePlayer iCombatPlayer && iCombatPlayer.onepiece$isCombatMode();

// Smoothly interpolate alpha
        if (combat) {
            combatModeAlpha += ANIMATION_SPEED;
        } else {
            combatModeAlpha -= ANIMATION_SPEED;
        }
        combatModeAlpha = MathHelper.clamp(combatModeAlpha, 0f, 1f);

// Skip rendering if fully invisible
        if (combatModeAlpha <= 0f) return;

        var textRenderer = client.inGameHud.getTextRenderer();
        float hotbarBaseY  = screenHeight - 0.5F * slotHeight - 0.5F * (float) slotHeight;
        float hotbarY = hotbarBaseY + (1 - combatModeAlpha) * 30;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float barOpacity = combatModeAlpha;
        int size = viewModel.spells().size();
        int left_count = Math.min(size, 4);
        int right_count = Math.max(0, Math.min(size-4,4));

        if (left_count > 0 ) {
            float leftEstimatedWidth = slotWidth * left_count;
            float leftHotbarX = screenWidth / 2F - 0.5F * leftEstimatedWidth - 160;
            Vec2f leftOrigin = new Vec2f(leftHotbarX, hotbarY);
            drawBackground(context, barOpacity, leftOrigin, left_count);
            drawIcons(context, viewModel, left_count, 0, leftOrigin, textRenderer);
        }
        if (right_count > 0) {
            float rightEstimatedWidth = slotWidth * right_count;
            float rightHotbarX = screenWidth / 2F - 0.5F * rightEstimatedWidth + 160;
            Vec2f rightOrigin = new Vec2f(rightHotbarX, hotbarY);
            drawBackground(context, barOpacity, rightOrigin, right_count);
            drawIcons(context, viewModel, right_count, left_count, rightOrigin, textRenderer);
        }


        RenderSystem.disableBlend();
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawIcons(DrawContext context, HudRenderHelper.SpellHotBarWidget.ViewModel viewModel, int count, int offset, Vec2f origin, TextRenderer textRenderer) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0F);
        var iconsOffset = new Vec2f(3, 3);
        int iconSize = 16;
        for (int i = 0; i < count; i++) {
            int id = i + offset;
            if (id >= viewModel.spells().size()) {
                break;
            }
            var spell = viewModel.spells().get(id);
            int x = (int) (origin.x + iconsOffset.x) + ((slotWidth) * i);
            int y = (int) (origin.y + iconsOffset.y);
            RenderSystem.enableBlend();
            if (spell.iconId() != null) {
                context.drawTexture(spell.iconId(), x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
            } else if (spell.itemStack() != null) {
                context.drawItem(spell.itemStack(), x, y);
            }
            // Cooldown
            if (spell.cooldown() > 0) {
                renderCooldown(context, spell.cooldown(), x, y);
            }

            // Keybinding
            if (spell.keybinding() != null) {
                var keybindingX = x + (iconSize / 2);
                var keybindingY = (int) origin.y + 2;
                if (spell.modifier() != null) {
                    keybindingX += 2; // Shifting to the right, because this will likely be the last
                    var spacing = 1;
                    var modifierWidth = spell.modifier().width(textRenderer);
                    var keybindingWidth = spell.keybinding().width(textRenderer);
                    var totalWidth = modifierWidth + keybindingWidth;

                    keybindingX -= (totalWidth / 2);
                    drawKeybinding(context, textRenderer, spell.modifier(), keybindingX, keybindingY, Drawable.Anchor.LEADING);
                    keybindingX += modifierWidth + spacing;
                    drawKeybinding(context, textRenderer, spell.keybinding(), keybindingX, keybindingY, Drawable.Anchor.LEADING);
                } else {
                    drawKeybinding(context, textRenderer, spell.keybinding(), keybindingX, keybindingY, Drawable.Anchor.CENTER);
                }
            }
        }
    }

    private static void drawBackground(DrawContext context, float barOpacity, Vec2f origin, int count) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, barOpacity);
        context.drawTexture(HOTBAR.id(), (int) (origin.x), (int) (origin.y), 0, 0, slotWidth / 2, slotHeight, HOTBAR.width(), HOTBAR.height());
        int middleElements = count - 1;
        for (int i = 0; i < middleElements; i++) {
            context.drawTexture(HOTBAR.id(), (int) (origin.x) + (slotWidth / 2) + (i * slotWidth), (int) (origin.y), (float) slotWidth / 2, 0, slotWidth, slotHeight, HOTBAR.width(), HOTBAR.height());
        }
        context.drawTexture(HOTBAR.id(), (int) (origin.x) + (slotWidth / 2) + (middleElements * slotWidth), (int) (origin.y), 170, 0, (slotHeight / 2) + 1, slotHeight, HOTBAR.width(), HOTBAR.height());
    }

    private static void drawKeybinding(DrawContext context, TextRenderer textRenderer, HudRenderHelper.SpellHotBarWidget.KeyBindingViewModel keybinding, int x, int y,
                                       Drawable.Anchor horizontalAnchor) {
        if (keybinding.drawable() != null) {
            keybinding.drawable().draw(context, x, y, horizontalAnchor, Drawable.Anchor.TRAILING);
        } else {
            var textLength = textRenderer.getWidth(keybinding.label());
            var xOffset = 0;
            switch (horizontalAnchor) {
                case TRAILING -> xOffset = -textLength / 2;
                case LEADING -> xOffset = textLength / 2;
            }
            x += xOffset;
            HudKeyVisuals.buttonLeading.draw(context, x - (textLength / 2), y, Drawable.Anchor.TRAILING, Drawable.Anchor.TRAILING);
            HudKeyVisuals.buttonCenter.drawFlexibleWidth(context, x - (textLength / 2), y, textLength, Drawable.Anchor.TRAILING);
            HudKeyVisuals.buttonTrailing.draw(context, x + (textLength / 2), y, Drawable.Anchor.LEADING, Drawable.Anchor.TRAILING);
            context.drawCenteredTextWithShadow(textRenderer, keybinding.label(), x, y - 10, 0xFFFFFF);
        }
    }

    private static void renderCooldown(DrawContext context, float progress, int x, int y) {
        // Copied from DrawContext.drawItemInSlot
        var k = y + MathHelper.floor(16.0F * (1.0F - progress));
        var l = k + MathHelper.ceil(16.0F * progress);
        context.fill(RenderLayer.getGuiOverlay(), x, k, x + 16, l, Integer.MAX_VALUE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}