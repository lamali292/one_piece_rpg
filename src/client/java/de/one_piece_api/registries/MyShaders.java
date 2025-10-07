package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Registry for custom shader programs used in post-processing effects.
 * <p>
 * This class manages the loading and access of custom shaders, including grayscale
 * and gaussian blur effects. Shaders are automatically reloaded when resource packs change.
 *
 * @see ShaderProgram
 * @see IdentifiableResourceReloadListener
 */
public class MyShaders {

    /**
     * Shader program that converts colors to grayscale.
     * <p>
     * Used for visual effects that require color desaturation.
     */
    public static ShaderProgram grayscaleShader;

    /**
     * Shader program that applies gaussian blur.
     * <p>
     * Used for blur effects and depth of field rendering.
     */
    public static ShaderProgram gaussianShader;

    /**
     * Initializes and registers all custom shaders.
     * <p>
     * This method should be called during client initialization to load shader programs
     * and set up resource reload listeners. The shaders are loaded with the
     * POSITION_TEXTURE_COLOR vertex format.
     */
    public static void init() {
        register(manager -> {
            grayscaleShader = new ShaderProgram(manager, "grayscale", VertexFormats.POSITION_TEXTURE_COLOR);
            gaussianShader = new ShaderProgram(manager, "gaussian", VertexFormats.POSITION_TEXTURE_COLOR);
        });

    }

    /**
     * Gets the grayscale shader program.
     *
     * @return the grayscale {@link ShaderProgram}, or {@code null} if not yet loaded
     */
    public static ShaderProgram getGrayscaleShader() {
        return grayscaleShader;
    }

    /**
     * Gets the gaussian blur shader program.
     *
     * @return the gaussian blur {@link ShaderProgram}, or {@code null} if not yet loaded
     */
    public static ShaderProgram getGaussianShader() {
        return gaussianShader;
    }

    /**
     * Registers a shader reload action with the resource manager.
     * <p>
     * The provided action will be executed whenever client resources are reloaded,
     * such as when changing resource packs or using F3+T.
     *
     * @param action the reload action to execute when resources are reloaded
     */
    public static void register(SimpleReloadAction action) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(simpleReloader(OnePieceRPG.id("shaders"), action));
    }

    /**
     * Creates a simple identifiable resource reload listener.
     * <p>
     * This helper method wraps a {@link SimpleReloadAction} in the full
     * {@link IdentifiableResourceReloadListener} interface, handling the
     * asynchronous reload process.
     *
     * @param id the identifier for this reload listener
     * @param action the action to execute during resource reload
     * @return an {@link IdentifiableResourceReloadListener} that executes the given action
     */
    public static IdentifiableResourceReloadListener simpleReloader(Identifier id, SimpleReloadAction action) {
        return new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id;
            }

            @Override
            public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager,
                                                  Profiler profiler, Profiler profiler2,
                                                  Executor executor, Executor executor2) {
                return CompletableFuture.runAsync(() -> {
                    try {
                        action.reload(manager);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor2).thenCompose(synchronizer::whenPrepared);
            }
        };
    }

    /**
     * Functional interface for simple resource reload actions.
     * <p>
     * Implementations of this interface can perform shader loading or other
     * resource-dependent operations during resource pack reloads.
     */
    @FunctionalInterface
    public interface SimpleReloadAction {
        /**
         * Executes the reload action using the provided resource manager.
         *
         * @param manager the resource manager to load resources from
         * @throws IOException if an I/O error occurs during reload
         */
        void reload(ResourceManager manager) throws IOException;
    }
}