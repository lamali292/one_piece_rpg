package de.one_piece_api;

import de.one_piece_api.ability.handler.PassiveAbilityHandler;
import de.one_piece_api.data.loader.DataLoaders;
import de.one_piece_api.data.source.ItemExperienceSource;
import de.one_piece_api.data.source.TimeExperienceSource;
import de.one_piece_api.event.ServerEvents;
import de.one_piece_api.init.MyCommands;
import de.one_piece_api.init.MyRewards;
import de.one_piece_api.init.MyServerPayloads;
import net.fabricmc.api.DedicatedServerModInitializer;

public class FabricServer implements DedicatedServerModInitializer {


    @Override
    public void onInitializeServer() {
        OnePieceRPG.LOGGER.info("Server initializing");

        TimeExperienceSource.register();
        ItemExperienceSource.register();

        MyServerPayloads.registerReceiver();
        MyCommands.register();
        MyRewards.register();
        DataLoaders.register();

        ServerEvents.register();
        PassiveAbilityHandler.init();



    }
}
