package de.one_piece_api.network.payload;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SetCombatModePayload(boolean mode) implements CustomPayload {
    public static final Id<SetCombatModePayload> TYPE = new Id<>(OnePieceRPG.id("set_combat_mode"));

    public static final PacketCodec<PacketByteBuf, SetCombatModePayload> STREAM_CODEC = PacketCodec.of(
            (packet, buf) -> {
                buf.writeBoolean(packet.mode());
            },
            buf -> new SetCombatModePayload(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

}
