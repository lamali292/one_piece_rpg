package de.one_piece_api.registries;

import de.one_piece_api.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MyClientPayloads {

    public static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ClassConfigPayload.TYPE, ClientPacketHandler::handleClassConfig);
        ClientPlayNetworking.registerGlobalReceiver(DevilFruitPayload.TYPE, ClientPacketHandler::handleDevilFruitConfig);
        ClientPlayNetworking.registerGlobalReceiver(UiPayload.TYPE, ClientPacketHandler::handleUi);
        ClientPlayNetworking.registerGlobalReceiver(SyncStylesPayload.TYPE, ClientPacketHandler::handleSyncStyles);

    }


}
