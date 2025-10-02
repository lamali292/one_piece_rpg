package de.one_piece_api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class RenderUtil {

    /**
     * Draws a texture with optional grayscale shader applied.
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

    // Your original methods below
    public static void drawTexture(DrawContext context, Supplier<ShaderProgram> program, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        drawTexture(context, texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight, program);
    }

    public static void drawTexture(DrawContext context,
                                   Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight,  Supplier<ShaderProgram> program
    ) {
        drawTexture(context, texture, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight, program);
    }


    static void drawTexture(
            DrawContext context, Identifier texture, int x1, int x2, int y1, int y2, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight,  Supplier<ShaderProgram> program
    ) {
        drawTexturedQuad(context,
                texture, x1, x2, y1, y2, z, (u + 0.0F) / textureWidth, (u + regionWidth) / textureWidth, (v + 0.0F) / textureHeight, (v + regionHeight) / textureHeight, program
        );
    }

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