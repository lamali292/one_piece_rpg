package de.one_piece_api.gui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class CustomButton {
    private final int relativeX, relativeY; // Relative Position zum Background
    private final int width, height;
    private final Text text;
    private final ButtonTextures textures;
    private final Runnable onClick;
    private final List<Text> tooltip; // Tooltip für aktivierten Zustand
    private final List<Text> disabledTooltip; // Tooltip für deaktivierten Zustand
    private boolean enabled = true;
    private boolean hovered = false;

    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, ButtonTextures textures, Runnable onClick) {
        this(relativeX, relativeY, width, height, text, textures, onClick, null, null);
    }

    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, ButtonTextures textures, Runnable onClick, List<Text> tooltip) {
        this(relativeX, relativeY, width, height, text, textures, onClick, tooltip, null);
    }

    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, ButtonTextures textures, Runnable onClick, List<Text> tooltip, List<Text> disabledTooltip) {
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.width = width;
        this.height = height;
        this.text = text;
        this.textures = textures;
        this.onClick = onClick;
        this.tooltip = tooltip;
        this.disabledTooltip = disabledTooltip;
    }

    /**
     * Einfacher Konstruktor für Buttons ohne Texturen
     */
    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, Runnable onClick) {
        this(relativeX, relativeY, width, height, text, null, onClick, null, null);
    }

    /**
     * Konstruktor für Buttons ohne Texturen aber mit Tooltips
     */
    public CustomButton(int relativeX, int relativeY, int width, int height, Text text, Runnable onClick, List<Text> tooltip, List<Text> disabledTooltip) {
        this(relativeX, relativeY, width, height, text, null, onClick, tooltip, disabledTooltip);
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int backgroundX, int backgroundY) {
        int x = backgroundX + relativeX;
        int y = backgroundY + relativeY;

        this.hovered = isMouseOver(mouseX, mouseY, x, y);

        if (textures != null) {
            Identifier texture = textures.get(enabled, hovered);
            context.drawTexture(texture, x, y, 0, 0, width, height, width, height);
        } else {
            int color = getButtonColor();
            context.fill(x, y, x + width, y + height, color);
        }

        // Text zentriert rendern
        int textX = x + (width - textRenderer.getWidth(text)) / 2;
        int textY = y + (height - textRenderer.fontHeight) / 2;
        int textColor = enabled ? 0xFFFFFF : 0x999999;

        context.drawText(textRenderer, text, textX, textY, textColor, false);
    }

    public void renderTooltip(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, int backgroundX, int backgroundY) {
        if (hovered) {
            if (enabled && tooltip != null) {
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            } else if (!enabled && disabledTooltip != null) {
                context.drawTooltip(textRenderer, disabledTooltip, mouseX, mouseY);
            }
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button, int backgroundX, int backgroundY) {
        int x = backgroundX + relativeX;
        int y = backgroundY + relativeY;

        if (enabled && button == 0 && isMouseOver(mouseX, mouseY, x, y)) {
            // Button-Sound abspielen
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            if (onClick != null) {
                onClick.run();
            }
            return true;
        }
        return false;
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private int getButtonColor() {
        if (!enabled) {
            return 0xFF333333;
        } else if (hovered) {
            return 0xFF666666;
        } else {
            return 0xFF444444;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHovered() {
        return hovered;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getRelativeX() { return relativeX; }
    public int getRelativeY() { return relativeY; }
}