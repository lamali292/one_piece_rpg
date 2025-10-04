package de.one_piece_api.hud;

import de.one_piece_api.interfaces.IStaminaPlayer;
import de.one_piece_api.registries.MyAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class StaminaBar {
    // Bar dimensions and position
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_X_OFFSET = 0; // Center of screen
    private static final int BAR_Y_OFFSET = -40; // Above hotbar

    // Colors (ARGB format)
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int STAMINA_COLOR = 0xFF00FFFF;    // Cyan
    private static final int BORDER_COLOR = 0xFF555555;     // Gray

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        var client = MinecraftClient.getInstance();
        var player = client.player;

        if (player == null) {
            return;
        }

        var attribute = player.getAttributeInstance(MyAttributes.MAX_STAMINA);
        if (attribute == null) {
            return;
        }

        var maxStamina = attribute.getValue();

        if (player instanceof IStaminaPlayer iStaminaPlayer) {
            double stamina = iStaminaPlayer.onepiece$getStamina();

            // Calculate bar position (centered horizontally, above hotbar)
            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();
            int barX = (screenWidth - BAR_WIDTH) / 2 + BAR_X_OFFSET;
            int barY = screenHeight + BAR_Y_OFFSET;

            // Calculate stamina percentage
            double staminaPercentage = Math.max(0, Math.min(1, stamina / maxStamina));
            int filledWidth = (int) (BAR_WIDTH * staminaPercentage);

            // Render background
            context.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, BORDER_COLOR);
            context.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BACKGROUND_COLOR);

            // Render stamina fill
            if (filledWidth > 0) {
                context.fill(barX, barY, barX + filledWidth, barY + BAR_HEIGHT, STAMINA_COLOR);
            }

            // Optional: Render stamina text
            String staminaText = String.format("%.0f / %.0f", stamina, maxStamina);
            int textX = barX + (BAR_WIDTH / 2) - (client.textRenderer.getWidth(staminaText) / 2);
            int textY = barY - 10;
            context.drawText(client.textRenderer, staminaText, textX, textY, 0xFFFFFF, true);
        }
    }
}