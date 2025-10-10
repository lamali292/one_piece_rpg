package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.StyledConnection;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.common.SkillConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(value = SkillConnection.class, remap = false)
public class SkillConnectionMixin implements StyledConnection {

    @Unique
    private Identifier style;

    @Override
    public Optional<Identifier> onepiece$getStyle() {
        return Optional.ofNullable(style);
    }

    @Override
    public void onepiece$setStyle(Identifier style) {
        this.style = style;
    }
}