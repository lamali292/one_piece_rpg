package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.IHidden;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin that adds hidden state functionality to {@link ClientSkillConfig}.
 * <p>
 * This mixin implements the {@link IHidden} interface, allowing skill configurations
 * to be marked as hidden in the UI.
 */
@Mixin(ClientSkillConfig.class)
public class ClientSkillConfigMixin implements IHidden {

    /**
     * Stores whether this skill configuration is currently hidden.
     */
    @Unique
    private boolean hidden;

    /**
     * Checks if this skill configuration is hidden.
     *
     * @return true if hidden, false otherwise
     */
    @Override
    public boolean onepiece$isHidden() {
        return hidden;
    }

    /**
     * Sets the hidden state of this skill configuration.
     *
     * @param hidden true to hide the skill, false to show it
     */
    @Override
    public void onepiece$setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}