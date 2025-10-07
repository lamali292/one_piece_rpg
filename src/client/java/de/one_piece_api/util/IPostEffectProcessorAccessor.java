package de.one_piece_api.util;

/**
 * Accessor interface for custom post-effect rendering functionality.
 * <p>
 * This interface is implemented via mixin to expose internal rendering methods
 * of the post-effect processor, allowing external systems to trigger post-processing
 * effects with custom timing control.
 *
 * @see de.one_piece_api.mixin.PostEffectProcessorMixin
 */
public interface IPostEffectProcessorAccessor {

    /**
     * Renders all post-effect passes with custom tick delta timing.
     * <p>
     * This method updates shader time and sequentially renders each post-effect
     * pass, allowing for frame-rate independent effect animations.
     *
     * @param tickDelta the time delta for this frame, used to update shader time
     */
    void onepiece$render(float tickDelta);
}