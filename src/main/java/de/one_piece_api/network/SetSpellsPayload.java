package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public record SetSpellsPayload(List<String> spells) implements CustomPayload {
    public static final Id<SetSpellsPayload> TYPE = new Id<>(OnePieceRPG.id("set_spells"));

    public static void write(SetSpellsPayload packet, PacketByteBuf buf) {
        List<String> spells = packet.spells();
        buf.writeVarInt(spells.size());
        for (String s : spells) {
            buf.writeString(s);
        }
    }

    public static SetSpellsPayload read(PacketByteBuf buf) {
        int size = buf.readVarInt();
        List<String> spells = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            spells.add(buf.readString());
        }
        return new SetSpellsPayload(spells);
    }

    public static final PacketCodec<PacketByteBuf, SetSpellsPayload> STREAM_CODEC =
            PacketCodec.of(
                    SetSpellsPayload::write,
                    SetSpellsPayload::read
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

}
