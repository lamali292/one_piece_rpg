package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.DevilFruitConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DevilFruitPayload(Identifier identifier, DevilFruitConfig config) implements CustomPayload {
    public static final Id<DevilFruitPayload> TYPE = new Id<>(OnePieceRPG.id("devil_fruit_config"));

    public static PacketCodec<PacketByteBuf, DevilFruitPayload> STREAM_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            DevilFruitPayload::identifier,
            MyCodecs.DEVIL_FRUIT_CONFIG,
            DevilFruitPayload::config,
            DevilFruitPayload::new
        );


    @Override
    public Id<DevilFruitPayload> getId() {
        return TYPE;
    }

    public record Request(Identifier identifier) implements CustomPayload {
        public static final Id<Request> TYPE = new Id<>(OnePieceRPG.id("request_devil_fruit_config"));
        public static PacketCodec<PacketByteBuf, Request> STREAM_CODEC = PacketCodec.of(
                (cla, buf) -> Identifier.PACKET_CODEC.encode(buf, cla.identifier()),
                buf -> new Request(Identifier.PACKET_CODEC.decode(buf))
        );
        @Override
        public Id<? extends CustomPayload> getId() {
            return TYPE;
        }
    }
}
