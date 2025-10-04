package de.one_piece_api;

import de.one_piece_api.registries.*;
import de.one_piece_api.util.ClientData;
import de.one_piece_api.util.TextureFramebufferCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OnePieceRPG.LOGGER.info("Client started");
        MyKeys.register();
        MyClientPayloads.registerReceiver();
        MyShaders.init();
        MyFonts.register();
        MySounds.register();
        ClientData.init();
        ModelLoadingPlugin.register(e ->
                e.addModels(Identifier.of(OnePieceRPG.MOD_ID, "fireball")));


        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return OnePieceRPG.id("texture_cache_clear");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        TextureFramebufferCache.clearCache();
                    }
                }
        );
        ClientEvents.register();
    }





}
