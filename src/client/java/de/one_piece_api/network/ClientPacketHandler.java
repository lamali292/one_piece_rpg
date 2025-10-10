package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.screen.OnePieceScreen;
import de.one_piece_api.network.payload.ClassConfigPayload;
import de.one_piece_api.network.payload.DevilFruitPayload;
import de.one_piece_api.network.payload.SyncStylesPayload;
import de.one_piece_api.network.payload.UiPayload;
import de.one_piece_api.registry.ClientStyleRegistry;
import de.one_piece_api.util.ClientData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

/**
 * Handles client-side network packet processing for One Piece RPG mod.
 * <p>
 * This class contains static handlers for various payload types received from the server,
 * including class configurations, devil fruit data, UI updates, and style synchronization.
 *
 * @see ClientPlayNetworking
 */
public class ClientPacketHandler {

    /**
     * Handles class configuration data received from the server.
     * <p>
     * Updates the client-side class configuration data store with the received
     * configuration map on the client thread.
     *
     * @param classConfigPayload the payload containing class configuration data
     * @param context the client networking context
     */
    public static void handleClassConfig(ClassConfigPayload classConfigPayload, ClientPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "Received class config: {}", classConfigPayload.configMap().keySet());

        var classConfig = classConfigPayload.configMap();
        context.client().execute(() -> ClientData.CLASS_CONFIG.set(classConfig));
    }

    /**
     * Handles devil fruit configuration data received from the server.
     * <p>
     * Updates the client-side devil fruit configuration data store with the received
     * configuration on the client thread.
     *
     * @param devilFruitPayload the payload containing devil fruit configuration data
     * @param context the client networking context
     */
    public static void handleDevilFruitConfig(DevilFruitPayload devilFruitPayload, ClientPlayNetworking.Context context) {
        var id = devilFruitPayload.identifier();
        var devilFruitConfig = devilFruitPayload.config();
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "Received devil fruit config: {}", id);
        context.client().execute(() -> ClientData.DEVIL_FRUIT_CONFIG.set(devilFruitConfig));
    }

    /**
     * Handles UI update commands received from the server.
     * <p>
     * Processes UI-related actions such as screen reloads and plays appropriate
     * sound effects. Currently supports the "reset" command which reloads the
     * One Piece screen category data and plays an ominous sound effect.
     *
     * @param uiPayload the payload containing the UI command
     * @param context the client networking context
     */
    public static void handleUi(UiPayload uiPayload, ClientPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "Ui update: {} ", uiPayload.ui());

        context.client().execute(() -> {
            switch (uiPayload.ui()) {
                case "reset" -> {
                    if (OnePieceScreen.getInstance() != null) {
                        OnePieceScreen.getInstance().reloadCategoryData();
                        context.client().getSoundManager().play(
                                PositionedSoundInstance.master(
                                        SoundEvents.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS,
                                        1.0F, // Volume
                                        2.0F  // Pitch
                                )
                        );

                    }
                }
            }
        });

    }

    /**
     * Handles style synchronization data received from the server.
     * <p>
     * Updates the client-side style registry with the received style definitions
     * on the client thread. These styles are used for visual customization of
     * skill connections and other UI elements.
     *
     * @param payload the payload containing style definitions
     * @param context the client networking context
     */
    public static void handleSyncStyles(SyncStylesPayload payload, ClientPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "[Client Payload] loaded styles: {} ", payload.styles().keySet());
        context.client().execute(() -> {
            ClientStyleRegistry.setStyles(payload.styles());
        });

    }
}