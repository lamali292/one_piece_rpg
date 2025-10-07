package de.one_piece_api;

import de.one_piece_api.util.IPostEffectProcessorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

/**
 * Manager for custom post-processing effects applied to the game render.
 * <p>
 * This class handles the lifecycle of a click-triggered visual effect that applies
 * shader-based post-processing to the entire screen. The effect is time-limited and
 * automatically disables after a set duration.
 *
 * @see PostEffectProcessor
 * @see IPostEffectProcessorAccessor
 */
public class PostProcessingManager {

    /** The post-effect processor instance managing the shader pipeline */
    private static PostEffectProcessor myEffectProcessor;

    /** Whether the post-processing effect is currently enabled */
    private static boolean effectEnabled = false;

    /** X-coordinate of the click position that triggered the effect (normalized 0-1) */
    private static float clickX = 0.0f;

    /** Y-coordinate of the click position that triggered the effect (normalized 0-1) */
    private static float clickY = 0.0f;

    /** Time elapsed since the effect was triggered, in ticks */
    private static float time = 0.0f;

    /**
     * Applies the post-processing effect to the current frame.
     * <p>
     * This method should be called each frame during the render cycle. It updates
     * shader uniforms, manages the effect timer, and automatically disables the
     * effect after 40 ticks. The effect is only applied when enabled and when the
     * game world and framebuffer are available.
     *
     * @param client the Minecraft client instance
     * @param tickCounter the render tick counter for timing
     * @param tick whether this is a tick frame
     */
    public static void applyPostEffect(MinecraftClient client, RenderTickCounter tickCounter, boolean tick) {
        if (effectEnabled && client.world != null && client.getFramebuffer() != null) {
            if (time > 40.0F) {
                effectEnabled = false;
            }


            if (myEffectProcessor == null) {
                initializeEffect(client);
            }
            if (myEffectProcessor instanceof IPostEffectProcessorAccessor accessor) {
                try {
                    float width = client.getWindow().getWidth();
                    float height = client.getWindow().getHeight();
                    float aspect = width/height;
                    myEffectProcessor.setUniforms("ClickX", clickX);
                    myEffectProcessor.setUniforms("ClickY", clickY);
                    myEffectProcessor.setUniforms("Aspect", aspect);
                    time += tickCounter.getTickDelta(tick);
                    accessor.onepiece$render(tickCounter.getTickDelta(tick));
                } catch (Exception e) {
                    System.err.println("Error applying post-processing effect: " + e.getMessage());
                    effectEnabled = false;
                }
            }
        }
    }

    /**
     * Triggers the post-processing effect at a specific screen position.
     * <p>
     * This method enables the effect, resets the timer, and stores the click
     * coordinates for use in shader uniforms. Any previously active effect
     * is cleaned up before starting the new effect.
     *
     * @param client the Minecraft client instance
     * @param x the x-coordinate of the click (normalized 0-1)
     * @param y the y-coordinate of the click (normalized 0-1)
     */
    public static void click(MinecraftClient client, float x, float y) {
        onClose();
        clickX = x;
        clickY = y;
        time = 0.0F;
        effectEnabled = true;
    }

    /**
     * Initializes the post-effect processor with the shader configuration.
     * <p>
     * This method loads the shader JSON file and sets up the effect processor
     * with the current framebuffer dimensions. If initialization fails, the
     * processor is set to null and an error message is logged.
     *
     * @param client the Minecraft client instance
     */
    private static void initializeEffect(MinecraftClient client) {
        try {
            if (client.getFramebuffer() == null || client.getWindow() == null) {
                return;
            }

            myEffectProcessor = new PostEffectProcessor(
                    client.getTextureManager(),
                    client.getResourceManager(),
                    client.getFramebuffer(),
                    Identifier.of("minecraft", "shaders/post/myeffect.json")
            );

            myEffectProcessor.setupDimensions(
                    client.getWindow().getFramebufferWidth(),
                    client.getWindow().getFramebufferHeight()
            );

        } catch (Exception e) {
            System.err.println("Failed to initialize post-processing effect: " + e.getMessage());
            myEffectProcessor = null;
        }
    }

    /**
     * Handles window resize events by reinitializing the effect.
     * <p>
     * When the game window is resized, the post-effect processor must be
     * recreated to match the new framebuffer dimensions. This method cleans
     * up the existing processor, which will be recreated on the next render.
     *
     * @param width the new window width in pixels
     * @param height the new window height in pixels
     */
    public static void onResized(int width, int height) {
        onClose();
    }

    /**
     * Cleans up and closes the post-effect processor.
     * <p>
     * This method releases all resources associated with the effect processor,
     * including framebuffers and shader programs. It should be called when the
     * effect is no longer needed or when the window is resized.
     */
    public static void onClose() {
        if (myEffectProcessor != null) {
            myEffectProcessor.close();
            myEffectProcessor = null;
        }
    }
}