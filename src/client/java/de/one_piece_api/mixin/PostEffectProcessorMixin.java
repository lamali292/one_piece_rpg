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

@Mixin(PostEffectProcessor.class)
public class PostEffectProcessorMixin implements IPostEffectProcessorAccessor {


    @Shadow
    private float time;

    @Shadow
    @Final
    private List<PostEffectPass> passes;

    @Shadow
    private void setTexFilter(int texFilter) {
    }

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
