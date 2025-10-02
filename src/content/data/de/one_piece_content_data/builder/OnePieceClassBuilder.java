package de.one_piece_content_data.builder;

import de.one_piece_api.config.ClassConfig;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.colors.ColorConfig;

public class OnePieceClassBuilder {
    private final Text name, description;
    private String primary, passive;
    private ColorConfig primaryColor, secondaryColor;
    private Identifier backTexture;
    private Identifier nameTexture;

    public OnePieceClassBuilder(Identifier id) {
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

    public OnePieceClassBuilder background(Identifier backTexture, Identifier nameTexture) {
        this.backTexture = backTexture;
        this.nameTexture = nameTexture;
        return this;
    }



    public ClassConfig build() {
        return new ClassConfig(
                name, description,primary,passive, primaryColor, secondaryColor, backTexture, nameTexture
        );
    }
}
