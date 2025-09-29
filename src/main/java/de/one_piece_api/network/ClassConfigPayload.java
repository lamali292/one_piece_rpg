package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Map;

public record ClassConfigPayload(Map<Identifier, ClassConfig> configMap) implements CustomPayload {
    public static final CustomPayload.Id<ClassConfigPayload> TYPE = new Id<>(OnePieceRPG.id("class_config"));

    public static PacketCodec<PacketByteBuf, ClassConfigPayload> STREAM_CODEC = PacketCodec.of(
            (cla, buf) -> MyCodecs.CLASS_CONFIG_MAP.encode(buf, cla.configMap()),
            buf -> new ClassConfigPayload(MyCodecs.CLASS_CONFIG_MAP.decode(buf))
    );

    @Override
    public Id<ClassConfigPayload> getId() {
        return TYPE;
    }

    public static class Request implements CustomPayload {
        public static final CustomPayload.Id<Request> TYPE = new CustomPayload.Id<>(OnePieceRPG.id("request_class_config"));

        public static final PacketCodec<PacketByteBuf, Request> STREAM_CODEC = PacketCodec.of(
                (payload, buf) -> {},   // nothing to write
                buf -> new Request()     // always produce a fresh instance
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return TYPE;
        }
    }
}
