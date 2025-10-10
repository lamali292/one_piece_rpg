package de.one_piece_api.network.payload;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetClassPayload(Identifier className) implements CustomPayload {
    public static final Id<SetClassPayload> TYPE = new Id<>(OnePieceRPG.id("set_class"));

    public static final PacketCodec<PacketByteBuf, SetClassPayload> STREAM_CODEC = PacketCodec.of(
            (packet, buf) -> Identifier.PACKET_CODEC.encode(buf, packet.className)
            , buf -> new SetClassPayload(Identifier.PACKET_CODEC.decode(buf))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

}
