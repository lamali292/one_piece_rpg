package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IHidden;
import net.puffish.skillsmod.config.skill.SkillConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SkillConfig.class)
public class SkillConfigMixin implements IHidden {

    @Unique
    private boolean onePiece$isHidden = false;

    @Override
    public boolean onepiece$isHidden() {
        return onePiece$isHidden;
    }

    @Override
    public void onepiece$setHidden(boolean hidden) {
        this.onePiece$isHidden = hidden;
    }
}
