package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.ISkillTypeProvider;
import de.one_piece_api.mixin_interface.SkillType;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin that adds skill type classification functionality to {@link ClientSkillConfig}.
 * <p>
 * This mixin implements the {@link ISkillTypeProvider} interface, allowing skill configurations
 * to be categorized by their type (Devil Fruit, Class, or Skill Tree). The skill type determines
 * how the skill is displayed, its requirements, and its behavior in the skill system.
 * <p>
 * The mixin injects a {@link SkillType} field into the client skill configuration, enabling
 * skills to be filtered and organized by type in the UI and game logic.
 *
 * @see ISkillTypeProvider
 * @see SkillType
 * @see ClientSkillConfig
 */
@Mixin(value = ClientSkillConfig.class, remap = false)
public class ClientSkillConfigMixin implements ISkillTypeProvider {

    /**
     * Stores the type classification of this skill configuration.
     * <p>
     * This field is injected into {@link ClientSkillConfig} and determines whether
     * the skill is a Devil Fruit ability, Class-specific skill, or general Skill Tree skill.
     * <p>
     * The {@code @Unique} annotation ensures this field doesn't conflict with existing
     * fields in the target class.
     *
     * @see SkillType
     */
    @Unique
    private SkillType skillType;

    /**
     * Retrieves the skill type of this configuration.
     * <p>
     * This method is part of the {@link ISkillTypeProvider} interface and allows
     * external code to query the skill's type classification. The type determines
     * how the skill is categorized in the UI and what requirements it has.
     *
     * @return the {@link SkillType} of this skill, or {@code null} if not set
     */
    @Override
    public SkillType onepiece$getSkillType() {
        return skillType;
    }

    /**
     * Sets the skill type classification for this configuration.
     * <p>
     * This method is part of the {@link ISkillTypeProvider} interface and allows
     * the skill system to assign a type to each skill. The type should be set
     * during skill initialization and determines the skill's category and behavior.
     *
     * @param skillType the {@link SkillType} to assign to this skill
     *                  (DEVIL_FRUIT, CLASS, or SKILL_TREE)
     */
    @Override
    public void onepiece$setSkillType(SkillType skillType) {
        this.skillType = skillType;
    }
}