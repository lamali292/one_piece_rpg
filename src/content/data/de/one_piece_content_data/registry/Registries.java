package de.one_piece_content_data.registry;

import de.one_piece_api.ability.PassiveAbility;
import de.one_piece_api.config.*;
import de.one_piece_api.config.skill.ConnectionsConfig;
import de.one_piece_api.config.skill.SkillDefinitionConfig;
import de.one_piece_api.config.skill.SkillTreeEntryConfig;
import de.one_piece_api.config.spell.SpellConfig;


import java.util.List;

public class Registries {


    public static final DataRegistry<SpellConfig> SPELLS = new DataRegistry<>();
    public static final DataRegistry<StyleConfig> STYLES = new DataRegistry<>();
    public static final DataRegistry<ClassConfig> CLASSES = new DataRegistry<>();
    public static final DataRegistry<DevilFruitConfig> DEVIL_FRUITS = new DataRegistry<>();
    public static final DataRegistry<SkillDefinitionConfig> SKILL_DEFINITION = new DataRegistry<>();
    public static final DataRegistry<List<SkillTreeEntryConfig>> SKILL_TREE_ENTRIES = new DataRegistry<>();
    public static final DataRegistry<ConnectionsConfig> CONNECTIONS = new DataRegistry<>();
    public static final DataRegistry<PassiveAbility> PASSIVES = new DataRegistry<>();

}
