package de.one_piece_api.network.payload;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UiPayload(String ui) implements CustomPayload {


    public static PacketCodec<PacketByteBuf, UiPayload> STREAM_CODEC = PacketCodec.of(
            (cla, buf) -> PacketCodecs.STRING.encode(buf, cla.ui()),
            buf -> new UiPayload(PacketCodecs.STRING.decode(buf))
    );

    public static final Id<UiPayload> TYPE = new Id<>(OnePieceRPG.id("ui"));


    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
