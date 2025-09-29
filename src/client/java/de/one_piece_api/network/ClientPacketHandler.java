package de.one_piece_api.network;

import de.one_piece_api.screens.OnePieceScreen;
import de.one_piece_api.util.Data;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class ClientPacketHandler {
    public static void handleClassConfig(ClassConfigPayload classConfigPayload, ClientPlayNetworking.Context context) {
        Data.classConfigMap = classConfigPayload.configMap();
    }


    public static void handleDevilFruitConfig(DevilFruitPayload devilFruitPayload, ClientPlayNetworking.Context context) {
        Data.devilFruitConfig = devilFruitPayload.config();
    }

    public static void handleUi(UiPayload uiPayload, ClientPlayNetworking.Context context) {
        switch (uiPayload.ui()) {
            case "reset" -> {
                if (OnePieceScreen.INSTANCE != null) {
                    OnePieceScreen.INSTANCE.reloadCategoryData();
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
    }
}
