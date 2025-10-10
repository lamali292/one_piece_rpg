package de.one_piece_api;

import de.one_piece_api.init.MyAttributes;
import de.one_piece_api.init.MyDataComponentTypes;
import de.one_piece_api.init.MyItems;
import de.one_piece_api.init.MyPayloads;
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
