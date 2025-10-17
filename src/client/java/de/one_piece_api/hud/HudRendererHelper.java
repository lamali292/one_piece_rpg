package de.one_piece_api.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.init.MyAttributes;
import de.one_piece_api.init.MyFonts;
import de.one_piece_api.mixin_interface.IStaminaPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

/**
 * Helper class for rendering HUD elements for the player.
 * <p>
 * This class is responsible for displaying:
 * <ul>
 *     <li>Stamina Bar</li>
 *     <li>Health Bar</li>
 *     <li>Food Bar</li>
 * </ul>
 * <p>
 * All bars are rendered with custom textures and text displays.
 */
public class HudRendererHelper {

    // ========================================
    // Texture Constants
    // ========================================

    private static final Identifier TEXTURE_BAR_FILL = OnePieceRPG.id("textures/gui/hud/bar_fill.png");
    private static final Identifier TEXTURE_BAR_FRAME = OnePieceRPG.id("textures/gui/hud/stamina_bar_frame.png");
    private static final Identifier TEXTURE_BAR_FRAME_SHORT = OnePieceRPG.id("textures/gui/hud/stamina_bar_frame_short.png");

    private static final Identifier TEXTURE_STAMINA_EMPTY = OnePieceRPG.id("textures/gui/hud/stamina/container.png");
    private static final Identifier TEXTURE_STAMINA_FULL = OnePieceRPG.id("textures/gui/hud/stamina/full.png");
    private static final Identifier TEXTURE_STAMINA_BLINKING = OnePieceRPG.id("textures/gui/hud/stamina/container_blinking.png");

    private static final Identifier TEXTURE_FOOD_EMPTY_HUNGER = Identifier.ofVanilla("hud/food_empty_hunger");
    private static final Identifier TEXTURE_FOOD_HALF_HUNGER = Identifier.ofVanilla("hud/food_half_hunger");
    private static final Identifier TEXTURE_FOOD_FULL_HUNGER = Identifier.ofVanilla("hud/food_full_hunger");
    private static final Identifier TEXTURE_FOOD_EMPTY = Identifier.ofVanilla("hud/food_empty");
    private static final Identifier TEXTURE_FOOD_HALF = Identifier.ofVanilla("hud/food_half");
    private static final Identifier TEXTURE_FOOD_FULL = Identifier.ofVanilla("hud/food_full");

    // ========================================
    // Layout Constants
    // ========================================

    private static final int BAR_WIDTH = 128;
    private static final int BAR_HEIGHT = 16;
    private static final int SCREEN_OFFSET_X = 4;
    private static final int SCREEN_OFFSET_Y = 4;
    private static final int BAR_SPACING = 1;
    private static final int BAR_VERTICAL_OFFSET = BAR_HEIGHT + BAR_SPACING;
    private static final int ICON_SIZE = 10;
    private static final int ICON_PADDING = 3;
    private static final int MAX_FOOD_LEVEL = 20;
    private static final float LOW_FOOD_THRESHOLD = 6.0F;
    private static final int ABSORPTION_TEXT_SPACING = 2;

    // ========================================
    // Color Constants
    // ========================================

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_YELLOW = 0xFFFFFF00;
    private static final Vector3f COLOR_STAMINA = new Vector3f(0.2f, 0.9f, 0.0f);
    private static final Vector3f COLOR_STAMINA_BOOST = new Vector3f(0.2f, 0.4f, 0.6f);
    private static final Vector3f COLOR_HEALTH_NORMAL = new Vector3f(0.9f, 0.1f, 0.0f);
    private static final Vector3f COLOR_HEALTH_WITHERED = new Vector3f(0.3f, 0.3f, 0.3f);
    private static final Vector3f COLOR_HEALTH_POISONED = new Vector3f(139f / 255f, 135f / 255f, 18f / 255f);
    private static final Vector3f COLOR_HEALTH_FROZEN = new Vector3f(0.5f, 0.7f, 1.0f);
    private static final Vector3f COLOR_FOOD_NORMAL = new Vector3f(0.9f, 0.8f, 0.0f);
    private static final Vector3f COLOR_FOOD_HUNGER = new Vector3f(0.6f, 0.8f, 0.2f);
    private static final float SHADER_OPACITY_FULL = 1.0f;
    private static final float ABSORPTION_OPACITY = 0.6f;
    private static final float HALF_HEALTH_THRESHOLD = 2.0f;
    // ========================================
    // Main Render Method
    // ========================================

    /**
     * Renders all HUD elements on the screen.
     * <p>
     * This method should be called during the HUD render phase.
     * It checks if the player and interaction manager are available
     * and then renders the stamina bar if the corresponding attributes
     * are present.
     *
     * @param drawContext the drawing context for rendering
     * @param tickCounter the render tick counter for animation timing
     */
    public static void render(DrawContext drawContext, int ticks, RenderTickCounter tickCounter, boolean hasFoodBar) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null || client.interactionManager == null) {
            return;
        }

        EntityAttributeInstance maxStaminaAttribute = player.getAttributeInstance(MyAttributes.MAX_STAMINA);

        if (maxStaminaAttribute != null
                && player instanceof IStaminaPlayer staminaPlayer
                && client.interactionManager.hasStatusBars()) {
            renderStamina(drawContext, staminaPlayer, maxStaminaAttribute, ticks, hasFoodBar);
        }
    }

    // ========================================
    // Stamina Bar
    // ========================================

    /**
     * Renders the stamina bar on the screen.
     * <p>
     * The bar displays the player's current stamina relative to
     * their maximum stamina. The bar is displayed in green
     * and includes text showing current and maximum values.
     *
     * @param drawContext         the drawing context for rendering
     * @param staminaPlayer       the player's IStaminaPlayer interface
     * @param maxStaminaAttribute the attribute instance for maximum stamina
     */
    public static void renderStamina(
            DrawContext drawContext,
            IStaminaPlayer staminaPlayer,
            EntityAttributeInstance maxStaminaAttribute,
            int ticks,
            boolean hasFoodBar
    ) {
        RenderSystem.enableBlend();
        MinecraftClient client = MinecraftClient.getInstance();
        double maxStamina = maxStaminaAttribute.getValue();
        double currentStamina = staminaPlayer.onepiece$getStamina();
        int staminaBarWidth = (int) (currentStamina / maxStamina * BAR_WIDTH);

        int staminaBarPositionY;
        if (hasFoodBar) {
            staminaBarPositionY = SCREEN_OFFSET_Y + 2 * BAR_VERTICAL_OFFSET;
        } else {
            staminaBarPositionY = SCREEN_OFFSET_Y +  BAR_VERTICAL_OFFSET;
        }

        // Render icon frame
        drawContext.drawTexture(
                TEXTURE_BAR_FRAME_SHORT,
                SCREEN_OFFSET_X,
                staminaBarPositionY,
                0, 0,
                BAR_HEIGHT, BAR_HEIGHT,
                BAR_HEIGHT, BAR_HEIGHT
        );

        boolean hasBoost = staminaPlayer.onepiece$hasStaminaBoost();
        boolean blinking = ticks / 5 % 2 == 0;
        drawContext.drawTexture(
                hasBoost && blinking ? TEXTURE_STAMINA_BLINKING :TEXTURE_STAMINA_EMPTY,
                SCREEN_OFFSET_X + ICON_PADDING,
                staminaBarPositionY + ICON_PADDING,
                0, 0,ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE
        );


        // Render main bar frame
        drawContext.drawTexture(
                TEXTURE_BAR_FRAME,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                staminaBarPositionY,
                0, 0,
                BAR_WIDTH, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );

        // Render green fill
        if (hasBoost) {
            RenderSystem.setShaderColor(
                    COLOR_STAMINA_BOOST.x,
                    COLOR_STAMINA_BOOST.y,
                    COLOR_STAMINA_BOOST.z,
                    SHADER_OPACITY_FULL
            );
        } else {
            RenderSystem.setShaderColor(
                    COLOR_STAMINA.x,
                    COLOR_STAMINA.y,
                    COLOR_STAMINA.z,
                    SHADER_OPACITY_FULL
            );
        }


        drawContext.drawTexture(
                TEXTURE_STAMINA_FULL,
                SCREEN_OFFSET_X + ICON_PADDING,
                staminaBarPositionY + ICON_PADDING,
                0f, 0f, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE
        );

        drawContext.drawTexture(
                TEXTURE_BAR_FILL,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                staminaBarPositionY,
                0, 0,
                staminaBarWidth, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );
        RenderSystem.setShaderColor(
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL
        );

        // Render text
        String staminaText = String.format("%.0f / %.0f", currentStamina, maxStamina);
        renderBarText(
                drawContext,
                client,
                staminaText,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                staminaBarPositionY,
                false
        );
        RenderSystem.disableBlend();
    }

    // ========================================
    // Health Bar
    // ========================================

    /**
     * Renders the health bar on the screen.
     * <p>
     * The bar displays the player's current health and adjusts
     * color based on status effects (poison, wither, frozen).
     * Additionally, absorption health is displayed in yellow.
     *
     * @param drawContext            the drawing context for rendering
     * @param player                 the player
     * @param maxHealth              maximum health
     * @param displayedHealth        last/displayed health
     * @param absorptionAmount       absorption value
     * @param shouldBlink            whether the display should blink
     */
    public static void renderHealthBar(
            DrawContext drawContext,
            PlayerEntity player,
            float maxHealth,
            int displayedHealth,
            int absorptionAmount,
            boolean shouldBlink
    ) {
        RenderSystem.enableBlend();
        MinecraftClient client = MinecraftClient.getInstance();
        HeartType heartType = HeartType.fromPlayerState(player);

        // Render icon container frame
        drawContext.drawTexture(
                TEXTURE_BAR_FRAME_SHORT,
                SCREEN_OFFSET_X,
                SCREEN_OFFSET_Y,
                0, 0,
                BAR_HEIGHT, BAR_HEIGHT,
                BAR_HEIGHT, BAR_HEIGHT
        );

        // Render container heart
        drawContext.drawGuiTexture(
                HeartType.CONTAINER.getTexture(false, false, shouldBlink),
                SCREEN_OFFSET_X + ICON_PADDING,
                SCREEN_OFFSET_Y + ICON_PADDING,
                ICON_SIZE, ICON_SIZE
        );

        // Render status heart
        boolean isLowHealth = displayedHealth <= maxHealth / HALF_HEALTH_THRESHOLD;
        drawContext.drawGuiTexture(
                heartType.getTexture(false, isLowHealth, shouldBlink),
                SCREEN_OFFSET_X + ICON_PADDING,
                SCREEN_OFFSET_Y + ICON_PADDING,
                ICON_SIZE, ICON_SIZE
        );

        // Render health bar frame
        int healthBarWidth = (int) (displayedHealth * BAR_WIDTH / maxHealth);
        drawContext.drawTexture(
                TEXTURE_BAR_FRAME,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                SCREEN_OFFSET_Y,
                0, 0,
                BAR_WIDTH, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );

        // Render health bar fill with color
        Vector3f healthColor = getHealthColor(heartType);
        drawContext.setShaderColor(healthColor.x, healthColor.y, healthColor.z, SHADER_OPACITY_FULL);
        drawContext.drawTexture(
                TEXTURE_BAR_FILL,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                SCREEN_OFFSET_Y,
                0, 0,
                healthBarWidth, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );
        RenderSystem.setShaderColor(
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL
        );

        // Render absorption bar (if present)
        if (absorptionAmount > 0) {
            renderAbsorption(drawContext, absorptionAmount, maxHealth);
        }

        // Render health text
        renderHealthText(drawContext, client, displayedHealth, maxHealth, absorptionAmount);

        RenderSystem.disableBlend();
    }

    /**
     * Returns the color for the health bar based on the HeartType.
     *
     * @param heartType the type of heart
     * @return the RGB color as Vector3f
     */
    private static Vector3f getHealthColor(HeartType heartType) {
        return switch (heartType) {
            case WITHERED -> COLOR_HEALTH_WITHERED;
            case POISONED -> COLOR_HEALTH_POISONED;
            case FROZEN -> COLOR_HEALTH_FROZEN;
            default -> COLOR_HEALTH_NORMAL;
        };
    }

    /**
     * Renders the absorption display on the health bar.
     *
     * @param drawContext      the drawing context
     * @param absorptionAmount the absorption value
     * @param maxHealth        the maximum health
     */
    private static void renderAbsorption(DrawContext drawContext, int absorptionAmount, float maxHealth) {
        int absorptionBarWidth = (int) (absorptionAmount * BAR_WIDTH / maxHealth);
        absorptionBarWidth = Math.min(absorptionBarWidth, BAR_WIDTH);

        drawContext.setShaderColor(
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                0.0f,
                ABSORPTION_OPACITY
        );
        drawContext.drawTexture(
                TEXTURE_BAR_FILL,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                SCREEN_OFFSET_Y,
                0, 0,
                absorptionBarWidth, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );
        drawContext.setShaderColor(
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL
        );
    }

    /**
     * Renders the health text with optional absorption value.
     *
     * @param drawContext      the drawing context
     * @param client           the Minecraft client instance
     * @param displayedHealth  the displayed health
     * @param maxHealth        the maximum health
     * @param absorptionAmount the absorption value
     */
    private static void renderHealthText(
            DrawContext drawContext,
            MinecraftClient client,
            int displayedHealth,
            float maxHealth,
            int absorptionAmount
    ) {
        String healthText = String.format("%d / %.0f", displayedHealth, maxHealth);
        Text formattedHealthText = Text.literal(healthText)
                .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT));
        int healthTextWidth = client.textRenderer.getWidth(formattedHealthText);

        renderBarText(
                drawContext,
                client,
                healthText,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                SCREEN_OFFSET_Y,
                false
        );

        // Render absorption text (if present)
        if (absorptionAmount > 0) {
            String absorptionText = String.format("(+%d)", absorptionAmount);
            Text formattedAbsorptionText = Text.literal(absorptionText)
                    .setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withColor(COLOR_YELLOW));

            int absorptionTextPositionX = SCREEN_OFFSET_X
                    + (BAR_WIDTH - healthTextWidth) / 2
                    + BAR_VERTICAL_OFFSET
                    + healthTextWidth
                    + ABSORPTION_TEXT_SPACING;
            int textCenterY = SCREEN_OFFSET_Y + (BAR_HEIGHT - client.textRenderer.fontHeight) / 2;

            drawContext.drawText(
                    client.textRenderer,
                    formattedAbsorptionText,
                    absorptionTextPositionX,
                    textCenterY,
                    COLOR_WHITE,
                    false
            );
        }
    }

    // ========================================
    // Food Bar
    // ========================================

    /**
     * Renders the food bar on the screen.
     * <p>
     * The bar displays the player's current hunger value.
     * Color and texture adapt when the player has the
     * hunger status effect.
     *
     * @param drawContext   the drawing context for rendering
     * @param player        the player
     * @param topPosition   top position (currently unused)
     * @param rightPosition right position (currently unused)
     */
    public static void renderFood(
            DrawContext drawContext,
            PlayerEntity player,
            int topPosition,
            int rightPosition
    ) {
        RenderSystem.enableBlend();
        MinecraftClient client = MinecraftClient.getInstance();

        HungerManager hungerManager = player.getHungerManager();
        int currentFoodLevel = hungerManager.getFoodLevel();
        int foodBarWidth = currentFoodLevel * BAR_WIDTH / MAX_FOOD_LEVEL;
        int foodBarPositionY = SCREEN_OFFSET_Y + BAR_VERTICAL_OFFSET;

        // Render icon frame
        drawContext.drawTexture(
                TEXTURE_BAR_FRAME_SHORT,
                SCREEN_OFFSET_X,
                foodBarPositionY,
                0, 0,
                BAR_HEIGHT, BAR_HEIGHT,
                BAR_HEIGHT, BAR_HEIGHT
        );

        // Determine textures and color based on hunger effect
        boolean hasHungerEffect = player.hasStatusEffect(StatusEffects.HUNGER);
        Identifier emptyFoodTexture = hasHungerEffect ? TEXTURE_FOOD_EMPTY_HUNGER : TEXTURE_FOOD_EMPTY;
        Identifier halfFoodTexture = hasHungerEffect ? TEXTURE_FOOD_HALF_HUNGER : TEXTURE_FOOD_HALF;
        Identifier fullFoodTexture = hasHungerEffect ? TEXTURE_FOOD_FULL_HUNGER : TEXTURE_FOOD_FULL;
        Vector3f foodBarColor = hasHungerEffect ? COLOR_FOOD_HUNGER : COLOR_FOOD_NORMAL;

        // Render empty food icon as base
        drawContext.drawGuiTexture(
                emptyFoodTexture,
                SCREEN_OFFSET_X + ICON_PADDING,
                foodBarPositionY + ICON_PADDING,
                ICON_SIZE,
                ICON_SIZE
        );

        // Render filled food icon (half or full)
        boolean isLowFood = currentFoodLevel <= LOW_FOOD_THRESHOLD;
        Identifier currentFoodTexture = isLowFood ? halfFoodTexture : fullFoodTexture;
        drawContext.drawGuiTexture(
                currentFoodTexture,
                SCREEN_OFFSET_X + ICON_PADDING,
                foodBarPositionY + ICON_PADDING,
                ICON_SIZE,
                ICON_SIZE
        );

        // Render food bar frame
        drawContext.drawTexture(
                TEXTURE_BAR_FRAME,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                foodBarPositionY,
                0, 0,
                BAR_WIDTH, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );

        // Render food bar fill with color
        RenderSystem.setShaderColor(
                foodBarColor.x,
                foodBarColor.y,
                foodBarColor.z,
                SHADER_OPACITY_FULL
        );
        drawContext.drawTexture(
                TEXTURE_BAR_FILL,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                foodBarPositionY,
                0, 0,
                foodBarWidth, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );
        RenderSystem.setShaderColor(
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL,
                SHADER_OPACITY_FULL
        );

        // Render food text
        String foodText = String.format("%d / %d", currentFoodLevel, MAX_FOOD_LEVEL);
        renderBarText(
                drawContext,
                client,
                foodText,
                SCREEN_OFFSET_X + BAR_VERTICAL_OFFSET,
                foodBarPositionY,
                false
        );

        RenderSystem.disableBlend();
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Renders centered text on a bar.
     *
     * @param drawContext  the drawing context
     * @param client       the Minecraft client instance
     * @param textContent  the text to display
     * @param barPositionX the X position of the bar
     * @param barPositionY the Y position of the bar
     * @param withShadow   whether the text should have a shadow
     */
    private static void renderBarText(
            DrawContext drawContext,
            MinecraftClient client,
            String textContent,
            int barPositionX,
            int barPositionY,
            boolean withShadow
    ) {
        Text formattedText = Text.literal(textContent).setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT));
        int textWidth = client.textRenderer.getWidth(formattedText);
        int textHeight = client.textRenderer.fontHeight;
        int centeredTextX = barPositionX + (BAR_WIDTH - textWidth) / 2;
        int centeredTextY = barPositionY + (BAR_HEIGHT - textHeight) / 2;

        drawContext.drawText(
                client.textRenderer,
                formattedText,
                centeredTextX,
                centeredTextY,
                COLOR_WHITE,
                withShadow
        );
    }

    // ========================================
    // HeartType Enum
    // ========================================

    /**
     * Enum for different heart types in the HUD.
     * <p>
     * Each type represents a different state of the health display
     * and has corresponding textures for different representations
     * (full, half, blinking, hardcore mode).
     */
    @Environment(value = EnvType.CLIENT)
    enum HeartType {
        /**
         * Container heart (empty heart background).
         */
        CONTAINER(
                Identifier.ofVanilla("hud/heart/container"),
                Identifier.ofVanilla("hud/heart/container_blinking"),
                Identifier.ofVanilla("hud/heart/container"),
                Identifier.ofVanilla("hud/heart/container_blinking"),
                Identifier.ofVanilla("hud/heart/container_hardcore"),
                Identifier.ofVanilla("hud/heart/container_hardcore_blinking"),
                Identifier.ofVanilla("hud/heart/container_hardcore"),
                Identifier.ofVanilla("hud/heart/container_hardcore_blinking")
        ),

        /**
         * Normal heart (standard health).
         */
        NORMAL(
                Identifier.ofVanilla("hud/heart/full"),
                Identifier.ofVanilla("hud/heart/full_blinking"),
                Identifier.ofVanilla("hud/heart/half"),
                Identifier.ofVanilla("hud/heart/half_blinking"),
                Identifier.ofVanilla("hud/heart/hardcore_full"),
                Identifier.ofVanilla("hud/heart/hardcore_full_blinking"),
                Identifier.ofVanilla("hud/heart/hardcore_half"),
                Identifier.ofVanilla("hud/heart/hardcore_half_blinking")
        ),

        /**
         * Poisoned heart (with poison effect).
         */
        POISONED(
                Identifier.ofVanilla("hud/heart/poisoned_full"),
                Identifier.ofVanilla("hud/heart/poisoned_full_blinking"),
                Identifier.ofVanilla("hud/heart/poisoned_half"),
                Identifier.ofVanilla("hud/heart/poisoned_half_blinking"),
                Identifier.ofVanilla("hud/heart/poisoned_hardcore_full"),
                Identifier.ofVanilla("hud/heart/poisoned_hardcore_full_blinking"),
                Identifier.ofVanilla("hud/heart/poisoned_hardcore_half"),
                Identifier.ofVanilla("hud/heart/poisoned_hardcore_half_blinking")
        ),

        /**
         * Withered heart (with wither effect).
         */
        WITHERED(
                Identifier.ofVanilla("hud/heart/withered_full"),
                Identifier.ofVanilla("hud/heart/withered_full_blinking"),
                Identifier.ofVanilla("hud/heart/withered_half"),
                Identifier.ofVanilla("hud/heart/withered_half_blinking"),
                Identifier.ofVanilla("hud/heart/withered_hardcore_full"),
                Identifier.ofVanilla("hud/heart/withered_hardcore_full_blinking"),
                Identifier.ofVanilla("hud/heart/withered_hardcore_half"),
                Identifier.ofVanilla("hud/heart/withered_hardcore_half_blinking")
        ),

        /**
         * Absorbing heart (with absorption effect).
         */
        ABSORBING(
                Identifier.ofVanilla("hud/heart/absorbing_full"),
                Identifier.ofVanilla("hud/heart/absorbing_full_blinking"),
                Identifier.ofVanilla("hud/heart/absorbing_half"),
                Identifier.ofVanilla("hud/heart/absorbing_half_blinking"),
                Identifier.ofVanilla("hud/heart/absorbing_hardcore_full"),
                Identifier.ofVanilla("hud/heart/absorbing_hardcore_full_blinking"),
                Identifier.ofVanilla("hud/heart/absorbing_hardcore_half"),
                Identifier.ofVanilla("hud/heart/absorbing_hardcore_half_blinking")
        ),

        /**
         * Frozen heart (when frozen).
         */
        FROZEN(
                Identifier.ofVanilla("hud/heart/frozen_full"),
                Identifier.ofVanilla("hud/heart/frozen_full_blinking"),
                Identifier.ofVanilla("hud/heart/frozen_half"),
                Identifier.ofVanilla("hud/heart/frozen_half_blinking"),
                Identifier.ofVanilla("hud/heart/frozen_hardcore_full"),
                Identifier.ofVanilla("hud/heart/frozen_hardcore_full_blinking"),
                Identifier.ofVanilla("hud/heart/frozen_hardcore_half"),
                Identifier.ofVanilla("hud/heart/frozen_hardcore_half_blinking")
        );

        private final Identifier textureFullNormal;
        private final Identifier textureFullNormalBlinking;
        private final Identifier textureHalfNormal;
        private final Identifier textureHalfNormalBlinking;
        private final Identifier textureFullHardcore;
        private final Identifier textureFullHardcoreBlinking;
        private final Identifier textureHalfHardcore;
        private final Identifier textureHalfHardcoreBlinking;

        /**
         * Constructor for HeartType.
         *
         * @param textureFullNormal           texture for full normal heart
         * @param textureFullNormalBlinking   texture for blinking full normal heart
         * @param textureHalfNormal           texture for half normal heart
         * @param textureHalfNormalBlinking   texture for blinking half normal heart
         * @param textureFullHardcore         texture for full hardcore heart
         * @param textureFullHardcoreBlinking texture for blinking full hardcore heart
         * @param textureHalfHardcore         texture for half hardcore heart
         * @param textureHalfHardcoreBlinking texture for blinking half hardcore heart
         */
        HeartType(
                Identifier textureFullNormal,
                Identifier textureFullNormalBlinking,
                Identifier textureHalfNormal,
                Identifier textureHalfNormalBlinking,
                Identifier textureFullHardcore,
                Identifier textureFullHardcoreBlinking,
                Identifier textureHalfHardcore,
                Identifier textureHalfHardcoreBlinking
        ) {
            this.textureFullNormal = textureFullNormal;
            this.textureFullNormalBlinking = textureFullNormalBlinking;
            this.textureHalfNormal = textureHalfNormal;
            this.textureHalfNormalBlinking = textureHalfNormalBlinking;
            this.textureFullHardcore = textureFullHardcore;
            this.textureFullHardcoreBlinking = textureFullHardcoreBlinking;
            this.textureHalfHardcore = textureHalfHardcore;
            this.textureHalfHardcoreBlinking = textureHalfHardcoreBlinking;
        }

        /**
         * Returns the appropriate heart texture based on the parameters.
         *
         * @param isHardcoreMode whether hardcore mode is active
         * @param isHalfHeart    whether a half heart should be displayed
         * @param shouldBlink    whether the heart should blink
         * @return the corresponding Identifier texture
         */
        public Identifier getTexture(boolean isHardcoreMode, boolean isHalfHeart, boolean shouldBlink) {
            if (!isHardcoreMode) {
                if (isHalfHeart) {
                    return shouldBlink ? this.textureHalfNormalBlinking : this.textureHalfNormal;
                }
                return shouldBlink ? this.textureFullNormalBlinking : this.textureFullNormal;
            }

            if (isHalfHeart) {
                return shouldBlink ? this.textureHalfHardcoreBlinking : this.textureHalfHardcore;
            }
            return shouldBlink ? this.textureFullHardcoreBlinking : this.textureFullHardcore;
        }

        /**
         * Determines the HeartType based on the player's current status.
         *
         * @param player the player
         * @return the corresponding HeartType
         */
        static HeartType fromPlayerState(PlayerEntity player) {
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                return POISONED;
            }
            if (player.hasStatusEffect(StatusEffects.WITHER)) {
                return WITHERED;
            }
            if (player.isFrozen()) {
                return FROZEN;
            }
            return NORMAL;
        }
    }
}