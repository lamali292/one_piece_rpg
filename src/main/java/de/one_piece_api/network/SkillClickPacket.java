package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SkillClickPacket(Identifier categoryId, String skillId) implements CustomPayload {
    public static final Id<SkillClickPacket> TYPE = new Id<>(OnePieceRPG.id("click_skill"));

    public static PacketCodec<PacketByteBuf, SkillClickPacket> STREAM_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            SkillClickPacket::categoryId,
            PacketCodecs.STRING,
            SkillClickPacket::skillId,
            SkillClickPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
