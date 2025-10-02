package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IHidden;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientSkillConfig.class)
public class ClientSkillConfigMixin implements IHidden {

    @Unique
    private boolean hidden;


    @Override
    public boolean onepiece$isHidden() {
        return hidden;
    }

    @Override
    public void onepiece$setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
