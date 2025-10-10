package de.one_piece_api.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.init.MyShaders;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Caches shader-processed versions of textures using framebuffers.
 * Generates them once and reuses them for better performance.
 */
public class TextureFramebufferCache {
    // Cache key combines texture ID and shader name for multi-shader support
    private static final Map<CacheKey, Identifier> cache = new HashMap<>();

    private static class CacheKey {
        final Identifier textureId;
        final String shaderName;

        CacheKey(Identifier textureId, String shaderName) {
            this.textureId = textureId;
            this.shaderName = shaderName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey key)) return false;
            return textureId.equals(key.textureId) && shaderName.equals(key.shaderName);
        }

        @Override
        public int hashCode() {
            return 31 * textureId.hashCode() + shaderName.hashCode();
        }
    }

    /**
     * Gets or creates a shader-processed version of the texture.
     * The processed texture is cached and only generated once.
     *
     * @param original The original texture identifier
     * @param shader Supplier for the shader program to apply
     * @param shaderName A unique name for this shader (e.g., "grayscale", "blur")
     * @return The processed texture identifier, or original if processing fails
     */
    public static Identifier getProcessedTexture(Identifier original, Supplier<ShaderProgram> shader, String shaderName) {
        CacheKey key = new CacheKey(original, shaderName);

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();

            // Bind and ensure the texture is loaded
            client.getTextureManager().bindTexture(original);
            RenderSystem.setShaderTexture(0, original);

            // Get texture dimensions from OpenGL
            int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

            if (width <= 0 || height <= 0) {
                OnePieceRPG.LOGGER.warn("Invalid texture dimensions for {}: {}x{} (texture not loaded yet, will retry later)",
                        original, width, height);
                return original; // Return original, will retry on next frame
            }

            OnePieceRPG.LOGGER.info("Creating {} texture for {} ({}x{})", shaderName, original, width, height);

            // Store current framebuffer
            Framebuffer previousFbo = client.getFramebuffer();

            // Create framebuffer for rendering
            Framebuffer fbo = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            fbo.setClearColor(0, 0, 0, 0);
            fbo.clear(MinecraftClient.IS_SYSTEM_MAC);

            // Render processed version to framebuffer
            fbo.beginWrite(true);

            // Clear the framebuffer
            GlStateManager._clearColor(0, 0, 0, 0);
            GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

            // Set up rendering state
            RenderSystem.setShaderTexture(0, original);
            RenderSystem.setShader(shader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Set viewport to match framebuffer
            GlStateManager._viewport(0, 0, width, height);

            // Create orthographic projection for framebuffer (0,0 at top-left)
            Matrix4f projMatrix = new Matrix4f().setOrtho(0, width, height, 0, -1, 1);
            RenderSystem.setProjectionMatrix(projMatrix, VertexSorter.BY_Z);

            // Save and reset model-view matrix
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            RenderSystem.applyModelViewMatrix();

            Matrix4f modelViewMatrix = new Matrix4f().identity();

            // Draw a full-screen quad with the texture (flip V coordinates)
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(modelViewMatrix, 0, 0, 0).texture(0, 1).color(255, 255, 255, 255);
            bufferBuilder.vertex(modelViewMatrix, 0, height, 0).texture(0, 0).color(255, 255, 255, 255);
            bufferBuilder.vertex(modelViewMatrix, width, height, 0).texture(1, 0).color(255, 255, 255, 255);
            bufferBuilder.vertex(modelViewMatrix, width, 0, 0).texture(1, 1).color(255, 255, 255, 255);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            RenderSystem.disableBlend();

            // Restore model-view matrix stack
            RenderSystem.getModelViewStack().popMatrix();
            RenderSystem.applyModelViewMatrix();

            // Read pixels from framebuffer
            fbo.beginRead();
            NativeImage processedImage = new NativeImage(width, height, false);
            RenderSystem.bindTexture(fbo.getColorAttachment());
            processedImage.loadFromTextureImage(0, false);
            fbo.endRead();

            // Restore previous framebuffer and viewport
            previousFbo.beginWrite(true);
            int windowWidth = client.getWindow().getFramebufferWidth();
            int windowHeight = client.getWindow().getFramebufferHeight();
            GlStateManager._viewport(0, 0, windowWidth, windowHeight);

            // Register as new texture with shader name suffix
            Identifier processedId = Identifier.of(original.getNamespace(),
                    original.getPath() + "_" + shaderName);
            NativeImageBackedTexture processedTex = new NativeImageBackedTexture(processedImage);
            client.getTextureManager().registerTexture(processedId, processedTex);

            OnePieceRPG.LOGGER.info("Successfully created {} texture: {}", shaderName, processedId);

            // Clean up framebuffer
            fbo.delete();

            // Cache result
            cache.put(key, processedId);

            return processedId;

        } catch (Exception e) {
            OnePieceRPG.LOGGER.warn("Failed to create {} texture for {}", shaderName, original, e);
            return original;
        }
    }

    /**
     * Convenience method for grayscale textures
     */
    public static Identifier getGrayscaleTexture(Identifier original) {
        return getProcessedTexture(original, MyShaders::getGrayscaleShader, "grayscale");
    }

    /**
     * Convenience method for blurred textures
     */
    public static Identifier getBlurredTexture(Identifier original) {
        return getProcessedTexture(original, MyShaders::getGaussianShader, "blur");
    }

    /**
     * Clears the cache. Call this when textures are reloaded.
     */
    public static void clearCache() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!cache.isEmpty()) {
            OnePieceRPG.LOGGER.info("Clearing texture cache ({} entries)", cache.size());
            for (Identifier processedId : cache.values()) {
                client.getTextureManager().destroyTexture(processedId);
            }
            cache.clear();
        }
    }
}