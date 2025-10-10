package de.one_piece_api.init;

import de.one_piece_api.network.*;
import de.one_piece_api.network.payload.ClassConfigPayload;
import de.one_piece_api.network.payload.DevilFruitPayload;
import de.one_piece_api.network.payload.SyncStylesPayload;
import de.one_piece_api.network.payload.UiPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Registry for client-side network payload receivers.
 * <p>
 * This class handles the registration of all client packet handlers for the One Piece RPG mod.
 * It binds payload types to their corresponding handler methods in {@link ClientPacketHandler}.
 *
 * @see ClientPacketHandler
 * @see ClientPlayNetworking
 */
public class MyClientPayloads {

    /**
     * Registers all client-side payload receivers.
     * <p>
     * This method should be called during client initialization to set up handlers for
     * all network packets sent from the server. It registers receivers for:
     * <ul>
     *     <li>Class configuration data</li>
     *     <li>Devil fruit configuration data</li>
     *     <li>UI update commands</li>
     *     <li>Style synchronization data</li>
     * </ul>
     */
    public static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ClassConfigPayload.TYPE, ClientPacketHandler::handleClassConfig);
        ClientPlayNetworking.registerGlobalReceiver(DevilFruitPayload.TYPE, ClientPacketHandler::handleDevilFruitConfig);
        ClientPlayNetworking.registerGlobalReceiver(UiPayload.TYPE, ClientPacketHandler::handleUi);
        ClientPlayNetworking.registerGlobalReceiver(SyncStylesPayload.TYPE, ClientPacketHandler::handleSyncStyles);
    }
}