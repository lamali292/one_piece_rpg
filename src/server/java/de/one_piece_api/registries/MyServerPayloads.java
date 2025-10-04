package de.one_piece_api.registries;

import de.one_piece_api.network.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class MyServerPayloads {

    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(SetCombatModePayload.TYPE, ServerPacketHandler::handleSetCombatModePayload);
        ServerPlayNetworking.registerGlobalReceiver(DevilFruitPayload.Request.TYPE, ServerPacketHandler::handleDevilFruitRequest);
        ServerPlayNetworking.registerGlobalReceiver(ClassConfigPayload.Request.TYPE, ServerPacketHandler::handleClassConfigRequest);
        ServerPlayNetworking.registerGlobalReceiver(SetSpellsPayload.TYPE, ServerPacketHandler::handleSetSpellsPayload);
        ServerPlayNetworking.registerGlobalReceiver(SetClassPayload.TYPE, ServerPacketHandler::handleSetClassPayload);
        ServerPlayNetworking.registerGlobalReceiver(UiPayload.TYPE, ServerPacketHandler::handleUi);
    }

}
