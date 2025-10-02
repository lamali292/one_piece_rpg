package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.config.StyleConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncStylesPayload(Map<Identifier, StyleConfig> styles) implements CustomPayload {
    public static final Id<SyncStylesPayload> TYPE = new Id<>(OnePieceRPG.id("sync_styles"));

    public static final PacketCodec<RegistryByteBuf, Map<Identifier, StyleConfig>> MAP_CODEC = PacketCodecs.map(
            HashMap::new,
            Identifier.PACKET_CODEC,
            MyCodecs.STYLE
    );

    public static final PacketCodec<RegistryByteBuf, SyncStylesPayload> STREAM_CODEC = MAP_CODEC.xmap(SyncStylesPayload::new, SyncStylesPayload::styles);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}