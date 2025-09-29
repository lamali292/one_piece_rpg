package de.one_piece_content_data.datagen;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_content_data.content.ExampleClasses;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;

public class OnePieceClassBuilder {

    private final Identifier id;
    private final Text name, description;
    private String primary, passive;
    private ColorConfig primaryColor, secondaryColor;
    private IconConfig icon;
    public OnePieceClassBuilder(Identifier id) {
        this.id = id;
        name = Text.translatable("class."+id.getNamespace()+"."+id.getPath()+".name");
        description = Text.translatable("class."+id.getNamespace()+"."+id.getPath()+".description");
    }

    public OnePieceClassBuilder primary(String skillId) {
        primary = skillId;
        return this;
    }

    public OnePieceClassBuilder passive(String skillId) {
        passive = skillId;
        return this;
    }

    public OnePieceClassBuilder color(int primary, int secondary) {
        primaryColor = new ColorConfig(primary);
        secondaryColor = new ColorConfig(secondary);
        return this;
    }

    public OnePieceClassBuilder textureIcon(Identifier icon) {
        this.icon = new IconConfig.TextureIconConfig(icon);
        return this;
    }

    public OnePieceClassBuilder effectIcon(StatusEffect effect) {
        this.icon = new IconConfig.EffectIconConfig(effect);
        return this;
    }

    public OnePieceClassBuilder itemIcon(ItemStack item) {
        this.icon = new IconConfig.ItemIconConfig(item);
        return this;
    }

    public ExampleClasses.OnePieceClass build() {
        return new ExampleClasses.OnePieceClass(id, new ClassConfig(
                name, description,primary,passive, primaryColor, secondaryColor, icon
        ));
    }
}
