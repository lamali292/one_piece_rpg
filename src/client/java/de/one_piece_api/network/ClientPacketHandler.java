package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.registries.ClientStyleRegistry;
import de.one_piece_api.util.ListenerUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class ClientPacketHandler {
    public static void handleClassConfig(ClassConfigPayload classConfigPayload, ClientPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "Received class config: {}", classConfigPayload.configMap().keySet());

        var classConfig = classConfigPayload.configMap();
        context.client().execute(() -> ListenerUtil.CLASS_CONFIG.set(classConfig));
    }


    public static void handleDevilFruitConfig(DevilFruitPayload devilFruitPayload, ClientPlayNetworking.Context context) {
        var id = devilFruitPayload.identifier();
        var devilFruitConfig = devilFruitPayload.config();
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "Received devil fruit config: {}", id);
        context.client().execute(() -> ListenerUtil.DEVIL_FRUIT_CONFIG.set(devilFruitConfig));
    }

    public static void handleUi(UiPayload uiPayload, ClientPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "Ui update: {} ", uiPayload.ui());

        context.client().execute(() -> {
            switch (uiPayload.ui()) {
                case "reset" -> {
                    if (OnePieceScreen.getInstance() != null) {
                        OnePieceScreen.getInstance().reloadCategoryData();
                        context.client().getSoundManager().play(
                                PositionedSoundInstance.master(
                                        SoundEvents.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS, // dein SoundEvent
                                        1.0F, // Lautstärke
                                        2.0F  // Pitch
                                )
                        );

                    }
                }
            }
        });

    }

    public static void handleSyncStyles(SyncStylesPayload payload, ClientPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.CLIENT_PAYLOAD_MARKER, "[Client Payload] loaded styles: {} ", payload.styles().keySet());
        context.client().execute(() -> {
            ClientStyleRegistry.setStyles(payload.styles());
        });

    }
}
