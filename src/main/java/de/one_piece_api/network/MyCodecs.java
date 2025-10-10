package de.one_piece_api.network;

import de.one_piece_api.config.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MyCodecs {




    public static final PacketCodec<PacketByteBuf, ColorConfig> COLOR = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            ColorConfig::argb,
            ColorConfig::new
    );

    public static final PacketCodec<PacketByteBuf, StyleConfig> STYLE = PacketCodec.tuple(
            COLOR,
            StyleConfig::color,
            StyleConfig::new
    );

    public static final PacketCodec<ByteBuf, StatusEffect> STATUS_EFFECT = PacketCodecs.codec(Registries.STATUS_EFFECT.getCodec());
    public static final PacketCodec<ByteBuf, Item> ITEM = PacketCodecs.codec(Registries.ITEM.getCodec());
    public static final PacketCodec<ByteBuf, ItemStack> ITEM_STACK = ITEM.xmap(ItemStack::new, ItemStack::getItem);


    public static final PacketCodec<PacketByteBuf, IconConfig.EffectIconConfig> ICON_EFFECT = PacketCodec.tuple(
            STATUS_EFFECT,
            IconConfig.EffectIconConfig::effect,
            IconConfig.EffectIconConfig::new
    );

    public static final PacketCodec<PacketByteBuf, IconConfig.ItemIconConfig> ICON_ITEM = PacketCodec.tuple(
            ITEM_STACK,
            IconConfig.ItemIconConfig::item,
            IconConfig.ItemIconConfig::new
    );

    public static final PacketCodec<PacketByteBuf, IconConfig.TextureIconConfig> ICON_TEXTURE = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            IconConfig.TextureIconConfig::texture,
            IconConfig.TextureIconConfig::new
    );

    public static final PacketCodec<PacketByteBuf, IconConfig> ICON = PacketCodec.of(
            (encoder, buf) -> {
                switch (encoder) {
                    case IconConfig.EffectIconConfig effect -> {
                        buf.writeByte(0);
                        ICON_EFFECT.encode(buf, effect);
                    }
                    case IconConfig.ItemIconConfig item -> {
                        buf.writeByte(1);
                        ICON_ITEM.encode(buf, item);
                    }
                    case IconConfig.TextureIconConfig texture -> {
                        buf.writeByte(2);
                        ICON_TEXTURE.encode(buf, texture);
                    }
                    default -> throw new IllegalStateException("Unknown IconConfig subclass: " + encoder.getClass());
                }
            },
            buf -> {
                int type = buf.readByte();
                return switch (type) {
                    case 0 -> ICON_EFFECT.decode(buf);
                    case 1 -> ICON_ITEM.decode(buf);
                    case 2 -> ICON_TEXTURE.decode(buf);
                    default -> throw new IllegalStateException("Unknown IconConfig type: " + type);
                };
            }
    );




    public static final PacketCodec<PacketByteBuf, ClassConfig> CLASS_CONFIG = PacketCodec.tuple(
                TextCodecs.PACKET_CODEC,
                ClassConfig::name,
                TextCodecs.PACKET_CODEC,
                ClassConfig::description,
                Identifier.PACKET_CODEC,
                ClassConfig::backTexture,
                Identifier.PACKET_CODEC,
                ClassConfig::nameTexture,
                (a, b,  d, e) -> new ClassConfig(Text.of(a), Text.of(b), null, d, e)
        );


    public static final PacketCodec<PacketByteBuf, Map<Identifier, ClassConfig>> CLASS_CONFIG_MAP = PacketCodecs.map(
            HashMap::new,
            Identifier.PACKET_CODEC,
            CLASS_CONFIG
    );




    public static PacketCodec<PacketByteBuf, ArrayList<Identifier>> IDENTIFIER_LIST = PacketCodecs.collection(
            ArrayList::new,
            Identifier.PACKET_CODEC
    );

    public static PacketCodec<PacketByteBuf, DevilFruitPathConfig> DEVIL_FRUIT_PATH_CONFIG = PacketCodec.of(
            (value, buf) -> IDENTIFIER_LIST.encode(buf, new ArrayList<>(value.skills())),
            buf -> new DevilFruitPathConfig(IDENTIFIER_LIST.decode(buf))
    );

    public static PacketCodec<PacketByteBuf, ArrayList<DevilFruitPathConfig>> DEVIL_FRUIT_PATHS_CONFIG = PacketCodecs.collection(
            ArrayList::new,
            DEVIL_FRUIT_PATH_CONFIG
    );


    public static PacketCodec<PacketByteBuf, DevilFruitConfig> DEVIL_FRUIT_CONFIG = PacketCodec.tuple(
            DEVIL_FRUIT_PATHS_CONFIG,
            e->new ArrayList<>(e.paths()),
            IDENTIFIER_LIST,
            e->new ArrayList<>(e.passives()),
            Identifier.PACKET_CODEC,
            DevilFruitConfig::modelId,
            DevilFruitConfig::new
    );
}
