package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.ISkillTypeProvider;
import de.one_piece_api.mixin_interface.SkillType;
import net.puffish.skillsmod.config.skill.SkillConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = SkillConfig.class, remap = false)
public class SkillConfigMixin implements ISkillTypeProvider {

    @Unique
    private SkillType onePiece$skillType = SkillType.SKILL_TREE;

    @Override
    public SkillType onepiece$getSkillType() {
        return onePiece$skillType;
    }

    @Override
    public void onepiece$setSkillType(SkillType hidden) {
        this.onePiece$skillType = hidden;
    }
}
