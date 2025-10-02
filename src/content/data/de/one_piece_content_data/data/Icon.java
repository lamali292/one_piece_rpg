package de.one_piece_content_data.data;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.IconConfig;

public class Icon {
    public static IconConfig texture(Identifier texture) {
        return new IconConfig.TextureIconConfig(texture);
    }

    public static IconConfig item(Item item) {
        return new IconConfig.ItemIconConfig(new ItemStack(item));
    }

    public static IconConfig effect(StatusEffect effect) {
        return new IconConfig.EffectIconConfig(effect);
    }

    public static IconConfig spell(Identifier spellId) {
        return texture(Identifier.of(spellId.getNamespace(), "textures/spell/" + spellId.getPath() + ".png"));
    }
}