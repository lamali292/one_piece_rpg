package de.one_piece_api.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.mixin_interface.ICombatPlayer;
import de.one_piece_api.mixin_interface.IStaminaCost;
import de.one_piece_api.mixin_interface.IStaminaPlayer;
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

/**
 * Custom spell hotbar widget that displays spell icons with combat mode animation.
 * <p>
 * This widget renders two hotbars on either side of the screen that smoothly fade in
 * when the player enters combat mode. Each slot displays spell icons, cooldowns,
 * stamina costs, and keybindings.
 */
public class CustomSpellHotBarWidget {
    /** Texture file for the hotbar background */
    private static final TextureFile HOTBAR = new TextureFile(Identifier.of("textures/gui/sprites/hud/hotbar.png"), 182, 22);

    /** Height of each hotbar slot in pixels */
    private static final int slotHeight = 22;

    /** Width of each hotbar slot in pixels */
    private static final int slotWidth = 20;

    /** Current alpha value for combat mode fade animation (0 = hidden, 1 = fully visible) */
    private static float combatModeAlpha = 0f;

    /** Speed at which the hotbar fades in/out per frame */
    private static final float ANIMATION_SPEED = 0.1f;

    /**
     * Renders the custom spell hotbar with combat mode animation.
     * <p>
     * The hotbar is split into two sections (left and right) that appear on either
     * side of the screen. It smoothly fades in when combat mode is active and fades
     * out when combat mode is inactive.
     *
     * @param context the drawing context used for rendering
     * @param screenWidth the width of the screen in pixels
     * @param screenHeight the height of the screen in pixels
     * @param viewModel the view model containing spell data to display
     */
    public static void render(DrawContext context, int screenWidth, int screenHeight, HudRenderHelper.SpellHotBarWidget.ViewModel viewModel) {

        MinecraftClient client = MinecraftClient.getInstance();

        boolean combat = client.player instanceof ICombatPlayer iCombatPlayer && iCombatPlayer.onepiece$isCombatMode();

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

    /**
     * Draws spell icons with their associated visual elements.
     * <p>
     * For each spell slot, this renders the icon, cooldown overlay, stamina cost indicator,
     * and keybinding display.
     *
     * @param context the drawing context
     * @param viewModel the view model containing spell data
     * @param count number of slots to render
     * @param offset starting index in the spell list
     * @param origin top-left corner position of the hotbar
     * @param textRenderer text renderer for keybindings
     */
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
            int x = (int) (origin.x + iconsOffset.x) + slotWidth * i;
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
            IStaminaCost staminaCost = (IStaminaCost) (Object) spell;
            if (staminaCost != null) {
                float stamina = staminaCost.onepiece$getStaminaCost();
                renderStamina(context, stamina, x, y);
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

    /**
     * Draws the hotbar background texture.
     * <p>
     * Renders a flexible-width hotbar by drawing the left edge, middle sections,
     * and right edge separately to accommodate any number of slots.
     *
     * @param context the drawing context
     * @param barOpacity opacity of the bar (0-1)
     * @param origin top-left corner position
     * @param count number of slots to draw
     */
    private static void drawBackground(DrawContext context, float barOpacity, Vec2f origin, int count) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, barOpacity);
        context.drawTexture(HOTBAR.id(), (int) (origin.x), (int) (origin.y), 0, 0, slotWidth / 2, slotHeight, HOTBAR.width(), HOTBAR.height());
        int middleElements = count - 1;
        for (int i = 0; i < middleElements; i++) {
            context.drawTexture(HOTBAR.id(), (int) (origin.x) + (slotWidth / 2) + (i * slotWidth), (int) (origin.y), (float) slotWidth / 2, 0, slotWidth, slotHeight, HOTBAR.width(), HOTBAR.height());
        }
        context.drawTexture(HOTBAR.id(), (int) (origin.x) + (slotWidth / 2) + (middleElements * slotWidth), (int) (origin.y), 170, 0, (slotHeight / 2) + 1, slotHeight, HOTBAR.width(), HOTBAR.height());
    }

    /**
     * Draws a keybinding label with decorative button visuals.
     *
     * @param context the drawing context
     * @param textRenderer text renderer for the label
     * @param keybinding the keybinding view model containing label and drawable
     * @param x horizontal position
     * @param y vertical position
     * @param horizontalAnchor anchor point for horizontal alignment
     */
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

    /**
     * Renders a cooldown overlay on a spell icon.
     * <p>
     * Displays a semi-transparent overlay that fills from bottom to top
     * based on the cooldown progress.
     *
     * @param context the drawing context
     * @param progress cooldown progress (0 = ready, 1 = on cooldown)
     * @param x icon x position
     * @param y icon y position
     */
    private static void renderCooldown(DrawContext context, float progress, int x, int y) {
        if (progress <= 0) return;

        int iconSize = 16;
        int fillHeight = MathHelper.ceil(iconSize * progress);
        int startY = y + iconSize - fillHeight;

        // Dark red overlay for cooldown
        context.fill(RenderLayer.getGuiOverlay(), x, startY, x + iconSize, y + iconSize, 0x88FFFFFF);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /**
     * Renders a stamina cost indicator as a vertical bar next to the spell icon.
     * <p>
     * The bar color indicates whether the player has enough stamina:
     * <ul>
     *     <li>Green: Sufficient stamina (100%+)</li>
     *     <li>Yellow: Moderate stamina (50-99%)</li>
     *     <li>Orange: Low stamina (1-49%)</li>
     *     <li>Red: Insufficient stamina (0%)</li>
     * </ul>
     *
     * @param context the drawing context
     * @param requiredStamina stamina cost of the spell
     * @param x icon x position
     * @param y icon y position
     */
    private static void renderStamina(DrawContext context, float requiredStamina, int x, int y) {
        if (requiredStamina <= 0) return;

        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (!(player instanceof IStaminaPlayer iStaminaPlayer)) return;

        double playerStamina = iStaminaPlayer.onepiece$getStamina();
        double staminaRatio = Math.min(1.0, playerStamina / requiredStamina);

        int iconSize = 17;
        int barWidth = 1;
        int barHeight = 16;

        // Background bar (dark)
        context.fill(RenderLayer.getGuiOverlay(),
                x + iconSize - barWidth, y,
                x + iconSize, y + barHeight,
                0xCC000000);

        // Foreground bar (colored based on stamina)
        int fillHeight = (int) (barHeight * staminaRatio);
        int startY = y + barHeight - fillHeight;

        // Color: Green -> Yellow -> Red based on stamina ratio
        int color;
        if (staminaRatio >= 1.0) {
            color = 0xFF00FF00; // Bright green - enough stamina
        } else if (staminaRatio >= 0.5) {
            color = 0xFFFFFF00; // Yellow - medium stamina
        } else if (staminaRatio > 0) {
            color = 0xFFFF8800; // Orange - low stamina
        } else {
            color = 0xFFFF0000; // Red - no stamina
        }

        context.fill(RenderLayer.getGuiOverlay(),
                x + iconSize - barWidth, startY,
                x + iconSize, y + barHeight,
                color);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

}