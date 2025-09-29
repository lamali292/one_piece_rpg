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

public class MyShaders {
    public static ShaderProgram grayscaleShaderProgram;

    public static void init() {
        register(manager -> {
            grayscaleShaderProgram = new ShaderProgram(manager, "grayscale", VertexFormats.POSITION_TEXTURE_COLOR);
        });
    }

    public static ShaderProgram getGrayscaleShaderProgram() {
        return grayscaleShaderProgram;
    }


    public static void register(SimpleReloadAction action) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(simpleReloader(OnePieceRPG.id("shaders"), action));
    }

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

    @FunctionalInterface
    public interface SimpleReloadAction {
        void reload(ResourceManager manager) throws IOException;
    }
}