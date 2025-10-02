package de.one_piece_api;

import de.one_piece_api.content.DataLoader;
import de.one_piece_api.experience.ItemExperienceSource;
import de.one_piece_api.experience.TimeExperienceSource;
import de.one_piece_api.node.SpellContainerReward;
import de.one_piece_api.registries.MyCommands;
import de.one_piece_api.registries.MyServerPayloads;
import net.fabricmc.api.DedicatedServerModInitializer;

public class FabricServer implements DedicatedServerModInitializer {


    @Override
    public void onInitializeServer() {
        OnePieceRPG.LOGGER.info("Server initializing");

        TimeExperienceSource.register();
        ItemExperienceSource.register();

        MyServerPayloads.registerReceiver();
        MyCommands.register();
        SpellContainerReward.register();
        DataLoader.register();

        ServerEvents.register();




    }
}
