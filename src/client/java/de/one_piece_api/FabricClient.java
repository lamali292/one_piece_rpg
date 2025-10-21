package de.one_piece_api;

import de.one_piece_api.event.ClientEvents;
import de.one_piece_api.init.*;
import de.one_piece_api.render.DevilFruitItemRenderer;
import de.one_piece_api.render.DevilFruitModelLoader;
import de.one_piece_api.util.ClientData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.util.Identifier;

/**
 * Client-side initialization handler for the One Piece RPG mod.
 * <p>
 * This class implements {@link ClientModInitializer} and handles all client-specific
 * initialization tasks including keybinding registration, network packet handlers,
 * shader loading, model registration, and resource reload listeners.
 *
 * @see ClientModInitializer
 */
public class FabricClient implements ClientModInitializer {

    /**
     * Initializes all client-side components of the mod.
     * <p>
     * This method is called by Fabric during the client startup and performs the following:
     * <ul>
     *     <li>Registers custom keybindings via {@link MyKeys}</li>
     *     <li>Registers network packet receivers via {@link MyClientPayloads}</li>
     *     <li>Initializes custom shaders via {@link MyShaders}</li>
     *     <li>Registers custom fonts via {@link MyFonts}</li>
     *     <li>Registers custom sounds via {@link MySounds}</li>
     *     <li>Initializes client data storage via {@link ClientData}</li>
     *     <li>Registers custom models (e.g., fireball)</li>
     *     <li>Sets up texture cache clearing on resource reload</li>
     *     <li>Registers client event callbacks via {@link ClientEvents}</li>
     * </ul>
     */
    @Override
    public void onInitializeClient() {
        OnePieceRPG.LOGGER.info("Client started");
        MyKeys.register();
        MyClientPayloads.registerReceiver();
        MyShaders.init();
        MyFonts.register();
        MySounds.register();
        ClientData.init();
        ModelLoadingPlugin.register(e -> e.addModels(Identifier.of(OnePieceRPG.MOD_ID, "fireball")));
        ClientEvents.register();

        PreparableModelLoadingPlugin.register(
                DevilFruitModelLoader.DATA_LOADER,
                new DevilFruitModelLoader()
        );

        BuiltinItemRendererRegistry.INSTANCE.register(
                MyItems.DEVIL_FRUIT.item(),
                new DevilFruitItemRenderer()
        );

    }





}