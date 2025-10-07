package de.one_piece_api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.function.Supplier;

/**
 * Utility class for advanced texture rendering with custom shader support.
 * <p>
 * This class provides methods for drawing textures with optional shader effects,
 * including grayscale conversion and custom shader programs. It extends the standard
 * {@link DrawContext} texture rendering capabilities with shader-based transformations.
 *
 * @see DrawContext
 * @see ShaderProgram
 */
public class RenderUtil {

    /**
     * Draws a texture with optional grayscale shader applied.
     * <p>
     * If grayscale is enabled, the texture is rendered using the provided shader supplier.
     * Otherwise, it uses the standard {@link DrawContext} rendering method.
     *
     * @param context the drawing context
     * @param texture the texture identifier to render
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param width the width of the rendered texture
     * @param height the height of the rendered texture
     * @param applyGrayscale whether to apply grayscale effect
     * @param grayscaleShader supplier for the grayscale shader program
     */
    public static void drawTexture(DrawContext context, Identifier texture,
                                   int x, int y, int width, int height,
                                   boolean applyGrayscale, Supplier<ShaderProgram> grayscaleShader) {
        if (applyGrayscale) {
            drawTextureWithShader(context, texture, x, y, width, height, grayscaleShader);
        } else {
            // Use normal DrawContext method
            RenderSystem.enableBlend();
            context.drawTexture(texture, x, y, 0, 0, width, height, width, height);
            RenderSystem.disableBlend();
        }
    }

    /**
     * Draws a texture with a custom shader applied.
     * <p>
     * This method manually constructs a quad and renders it with the specified shader,
     * allowing for custom visual effects like color transformations or filters.
     *
     * @param context the drawing context
     * @param texture the texture identifier to render
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param width the width of the rendered texture
     * @param height the height of the rendered texture
     * @param shaderSupplier supplier for the custom shader program
     */
    private static void drawTextureWithShader(DrawContext context, Identifier texture,
                                              int x, int y, int width, int height,
                                              Supplier<ShaderProgram> shaderSupplier) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(shaderSupplier);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f posMatrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_TEXTURE_COLOR
        );

        bufferBuilder.vertex(posMatrix, x, y, 0).texture(0, 0).color(1f, 1f, 1f, 1f);
        bufferBuilder.vertex(posMatrix, x, y + height, 0).texture(0, 1).color(1f, 1f, 1f, 1f);
        bufferBuilder.vertex(posMatrix, x + width, y + height, 0).texture(1, 1).color(1f, 1f, 1f, 1f);
        bufferBuilder.vertex(posMatrix, x + width, y, 0).texture(1, 0).color(1f, 1f, 1f, 1f);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * Draws a texture region with a custom shader.
     * <p>
     * Simplified version that renders the full specified region dimensions.
     *
     * @param context the drawing context
     * @param program supplier for the shader program to use
     * @param texture the texture identifier to render
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param u the u-coordinate in the texture (left edge)
     * @param v the v-coordinate in the texture (top edge)
     * @param width the width to render
     * @param height the height to render
     * @param textureWidth the total width of the texture file
     * @param textureHeight the total height of the texture file
     */
    public static void drawTexture(DrawContext context, Supplier<ShaderProgram> program, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        drawTexture(context, texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight, program);
    }

    /**
     * Draws a texture region with a custom shader and region dimensions.
     * <p>
     * This method allows rendering a specific region of a texture atlas with
     * custom dimensions, useful for sprite sheets and texture atlases.
     *
     * @param context the drawing context
     * @param texture the texture identifier to render
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param width the width to render on screen
     * @param height the height to render on screen
     * @param u the u-coordinate in the texture (left edge)
     * @param v the v-coordinate in the texture (top edge)
     * @param regionWidth the width of the region in the texture
     * @param regionHeight the height of the region in the texture
     * @param textureWidth the total width of the texture file
     * @param textureHeight the total height of the texture file
     * @param program supplier for the shader program to use
     */
    public static void drawTexture(DrawContext context,
                                   Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight,  Supplier<ShaderProgram> program
    ) {
        drawTexture(context, texture, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight, program);
    }

    /**
     * Internal method for drawing a texture region with precise coordinate control.
     * <p>
     * This method handles the actual coordinate transformation from texture space
     * to screen space with custom shader support.
     *
     * @param context the drawing context
     * @param texture the texture identifier to render
     * @param x1 the left x-coordinate
     * @param x2 the right x-coordinate
     * @param y1 the top y-coordinate
     * @param y2 the bottom y-coordinate
     * @param z the z-coordinate (depth)
     * @param regionWidth the width of the region in the texture
     * @param regionHeight the height of the region in the texture
     * @param u the u-coordinate in the texture (left edge)
     * @param v the v-coordinate in the texture (top edge)
     * @param textureWidth the total width of the texture file
     * @param textureHeight the total height of the texture file
     * @param program supplier for the shader program to use
     */
    static void drawTexture(
            DrawContext context, Identifier texture, int x1, int x2, int y1, int y2, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight,  Supplier<ShaderProgram> program
    ) {
        drawTexturedQuad(context,
                texture, x1, x2, y1, y2, z, (u + 0.0F) / textureWidth, (u + regionWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + regionHeight) / textureHeight, program
        );
    }

    /**
     * Draws a textured quad with custom shader and UV coordinates.
     * <p>
     * This is the low-level rendering method that constructs and renders a quad
     * with the specified texture coordinates and shader program. It handles all
     * OpenGL state setup including blending and shader binding.
     *
     * @param context the drawing context
     * @param texture the texture identifier to render
     * @param x1 the left x-coordinate
     * @param x2 the right x-coordinate
     * @param y1 the top y-coordinate
     * @param y2 the bottom y-coordinate
     * @param z the z-coordinate (depth)
     * @param u1 the left u-coordinate (normalized 0-1)
     * @param u2 the right u-coordinate (normalized 0-1)
     * @param v1 the top v-coordinate (normalized 0-1)
     * @param v2 the bottom v-coordinate (normalized 0-1)
     * @param program supplier for the shader program to use
     */
    static void drawTexturedQuad(DrawContext context, Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, Supplier<ShaderProgram> program) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(program);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        var matrices = context.getMatrices();
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(posMatrix, x1, y1, z).texture(u1, v1).color(1f,1f,1f,1f);
        bufferBuilder.vertex(posMatrix, x1, y2, z).texture(u1, v2).color(1f,1f,1f,1f);
        bufferBuilder.vertex(posMatrix, x2, y2, z).texture(u2, v2).color(1f,1f,1f,1f);
        bufferBuilder.vertex(posMatrix, x2, y1, z).texture(u2, v1).color(1f,1f,1f,1f);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
}