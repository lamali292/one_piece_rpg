package de.one_piece_api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class OnePieceRPG {
    public static final String MOD_ID = "one_piece_api";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Marker CLIENT_PAYLOAD_MARKER = MarkerFactory.getMarker("CLIENT PAYLOAD");
    public static final Marker SERVER_PAYLOAD_MARKER = MarkerFactory.getMarker("SERVER PAYLOAD");
    public static final Marker CLASS_REWARD_HANDLER = MarkerFactory.getMarker("ClassRewardHandler");
    public static final Marker UI_UPDATE = MarkerFactory.getMarker("UIUpdate");
    public static final Marker LOADING_DATA = MarkerFactory.getMarker("Dataloading");;

    public static final boolean DEBUG = true;


    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }


    public static int getSpellSlots(PlayerEntity player) {
        return 8;
    }

    public static void debug(Marker marker, String s, Object... arguments) {
        if (DEBUG) {
            String message = "["+ marker.getName() +"] "+ s;
            LOGGER.info(marker, message, arguments);
        }
    }

}
