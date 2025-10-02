package de.one_piece_api;

import de.one_piece_api.util.IPostEffectProcessorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class PostProcessingManager {


    private static PostEffectProcessor myEffectProcessor;
    private static boolean effectEnabled = false;
    private static float clickX = 0.0f;
    private static float clickY = 0.0f;
    private static float time = 0.0f;

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

    public static void click(MinecraftClient client, float x, float y) {
        onClose();
        clickX = x;
        clickY = y;
        time = 0.0F;
        effectEnabled = true;
    }

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

    public static void onResized(int width, int height) {
        onClose();
    }


    public static void onClose() {
        if (myEffectProcessor != null) {
            myEffectProcessor.close();
            myEffectProcessor = null;
        }
    }
}
