package de.one_piece_api.mixin;


import com.mojang.blaze3d.platform.GlConst;
import de.one_piece_api.util.IPostEffectProcessorAccessor;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * Mixin for {@link PostEffectProcessor} that provides custom render access for post-processing effects.
 * <p>
 * This mixin implements {@link IPostEffectProcessorAccessor} to expose internal rendering functionality,
 * allowing external systems to trigger post-effect rendering with custom timing.
 *
 * @see PostEffectProcessor
 * @see IPostEffectProcessorAccessor
 */
@Mixin(PostEffectProcessor.class)
public class PostEffectProcessorMixin implements IPostEffectProcessorAccessor {

    /**
     * The current shader time value, incremented each frame.
     */
    @Shadow
    private float time;

    /**
     * The list of post-effect passes to render in sequence.
     */
    @Shadow
    @Final
    private List<PostEffectPass> passes;

    /**
     * Sets the texture filter mode for rendering.
     *
     * @param texFilter the OpenGL texture filter constant (e.g., GL_NEAREST, GL_LINEAR)
     */
    @Shadow
    private void setTexFilter(int texFilter) {
    }

    /**
     * Renders all post-effect passes with custom tick delta timing.
     * <p>
     * This method updates the shader time based on the provided tick delta and
     * sequentially renders each post-effect pass. The texture filter is updated
     * between passes only when necessary to minimize state changes.
     *
     * @param tickDelta the time delta for this frame, used to update shader time
     */
    @Unique
    public void onepiece$render(float tickDelta) {
        this.time += tickDelta;
        int i = GlConst.GL_NEAREST;
        for (PostEffectPass postEffectPass : this.passes) {
            int j = postEffectPass.getTexFilter();
            if (i != j) {
                this.setTexFilter(j);
                i = j;
            }
            postEffectPass.render(this.time / 20f);
        }
        this.setTexFilter(GlConst.GL_NEAREST);
    }


}