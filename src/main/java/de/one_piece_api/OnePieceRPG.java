package de.one_piece_api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnePieceRPG {
    public static final String MOD_ID = "one_piece_api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }


    public static int getSpellSlots(PlayerEntity player) {
        return 8;
    }
}
