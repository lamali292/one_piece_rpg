package de.one_piece_api.registries;

import de.one_piece_api.network.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;

public class MyPayloads {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(SetClassPayload.TYPE, SetClassPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SetCombatModePayload.TYPE, SetCombatModePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SetSpellsPayload.TYPE, SetSpellsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ClassConfigPayload.Request.TYPE, ClassConfigPayload.Request.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(DevilFruitPayload.Request.TYPE, DevilFruitPayload.Request.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UiPayload.TYPE, UiPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SkillClickPacket.TYPE, SkillClickPacket.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(UiPayload.TYPE, UiPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClassConfigPayload.TYPE, ClassConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(DevilFruitPayload.TYPE, DevilFruitPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncStylesPayload.TYPE, SyncStylesPayload.STREAM_CODEC);
    }

}
