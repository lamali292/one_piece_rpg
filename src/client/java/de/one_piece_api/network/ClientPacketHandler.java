package de.one_piece_api.network;

import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.gui.managers.ClassScreenManager;
import de.one_piece_api.gui.tabs.DevilFruitTab;
import de.one_piece_api.registries.ClientStyleRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class ClientPacketHandler {
    public static void handleClassConfig(ClassConfigPayload classConfigPayload, ClientPlayNetworking.Context context) {
        var classConfig = classConfigPayload.configMap();
        context.client().execute(() -> ClassScreenManager.getInstance().updateClassConfigurations(classConfig));
    }


    public static void handleDevilFruitConfig(DevilFruitPayload devilFruitPayload, ClientPlayNetworking.Context context) {
        var devilFruitConfig = devilFruitPayload.config();
        context.client().execute(() -> DevilFruitTab.devilFruitConfig = devilFruitConfig);
    }

    public static void handleUi(UiPayload uiPayload, ClientPlayNetworking.Context context) {

        context.client().execute(() -> {
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
        });

    }

    public static void handleSyncStyles(SyncStylesPayload payload, ClientPlayNetworking.Context context) {
        ClientStyleRegistry.setStyles(payload.styles());
    }
}
