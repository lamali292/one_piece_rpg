package de.one_piece_api;

import de.one_piece_api.registries.MyClientPayloads;
import de.one_piece_api.registries.MyFonts;
import de.one_piece_api.registries.MyKeys;
import de.one_piece_api.registries.MyShaders;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

public class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OnePieceRPG.LOGGER.info("Client started");
        MyKeys.register();
        MyClientPayloads.registerReceiver();
        MyShaders.init();
        MyFonts.register();
        ModelLoadingPlugin.register(e ->
                e.addModels(Identifier.of(OnePieceRPG.MOD_ID, "fireball")));

    }


}
