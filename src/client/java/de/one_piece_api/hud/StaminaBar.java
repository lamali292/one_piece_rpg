package de.one_piece_api.hud;

import de.one_piece_api.interfaces.IStaminaPlayer;
import de.one_piece_api.registries.MyAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders a stamina bar HUD element for the player.
 * <p>
 * The stamina bar is displayed above the hotbar and shows the current stamina
 * relative to the maximum stamina value from the player's attributes.
 */
public class StaminaBar {
    /** Width of the stamina bar in pixels */
    private static final int BAR_WIDTH = 182;

    /** Height of the stamina bar in pixels */
    private static final int BAR_HEIGHT = 5;

    /** Horizontal offset from the center of the screen */
    private static final int BAR_X_OFFSET = 0;

    /** Vertical offset from the bottom of the screen (negative = above hotbar) */
    private static final int BAR_Y_OFFSET = -40;

    /** Background color of the bar (ARGB format - semi-transparent black) */
    private static final int BACKGROUND_COLOR = 0x80000000;

    /** Fill color for the stamina amount (ARGB format - cyan) */
    private static final int STAMINA_COLOR = 0xFF00FFFF;

    /** Border color around the bar (ARGB format - gray) */
    private static final int BORDER_COLOR = 0xFF555555;

    /**
     * Renders the stamina bar on the screen.
     * <p>
     * This method should be called during the HUD render phase. It displays a horizontal
     * bar centered above the hotbar showing the player's current stamina out of their
     * maximum stamina.
     *
     * @param context the drawing context used for rendering
     * @param tickCounter the render tick counter for animation timing
     */
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