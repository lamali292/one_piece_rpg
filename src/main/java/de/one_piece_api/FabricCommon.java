package de.one_piece_api;

import de.one_piece_api.registries.MyAttributes;
import de.one_piece_api.registries.MyDataComponentTypes;
import de.one_piece_api.registries.MyItems;
import de.one_piece_api.registries.MyPayloads;
import net.fabricmc.api.ModInitializer;

public class FabricCommon implements ModInitializer {

    @Override
    public void onInitialize() {
        OnePieceRPG.LOGGER.info("Common started");
        MyDataComponentTypes.register();
        MyItems.register();
        MyPayloads.register();
        MyAttributes.initialize();
    }
}
