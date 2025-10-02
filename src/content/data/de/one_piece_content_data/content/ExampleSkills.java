package de.one_piece_content_data.content;

import de.one_piece_content.ExampleMod;
import de.one_piece_api.config.SkillTreeEntryConfig;
import de.one_piece_content_data.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ExampleSkills {


    public static List<SkillTreeEntryConfig> SKILL_TREE_ENTRIES = new ArrayList<>();

    static {
        register(ExampleSkillDefinitions.SWORDSMEN_SKILL_1.id(), true, -70, -40);
        register(ExampleSkillDefinitions.SWORDSMEN_SKILL_2.id(), false, -138, 0);
        register(ExampleSkillDefinitions.SWORDSMEN_SKILL_3.id(), false, -70, -120);
        register(ExampleSkillDefinitions.BRAWLER_SKILL_1.id(), true,  70, -40);
        register(ExampleSkillDefinitions.BRAWLER_SKILL_2.id(), false, 130, 0);
        register(ExampleSkillDefinitions.BRAWLER_SKILL_3.id(), false, 70, -120);
        register(ExampleSkillDefinitions.SNIPER_SKILL_1.id(), true, 0, 80);
        register(ExampleSkillDefinitions.SNIPER_SKILL_2.id(), false, 70, 120);
        register(ExampleSkillDefinitions.SNIPER_SKILL_3.id(), false, -70, 120);
        register(ExampleSkillDefinitions.REQUIRES_BOTH.id(), false,  0, -70);

        Registries.SKILL_TREE_ENTRIES.register(ExampleMod.id("skills"), SKILL_TREE_ENTRIES);
    }

    public static void register(Identifier skillId, boolean isRoot, int x, int y) {
        SKILL_TREE_ENTRIES.add(new SkillTreeEntryConfig(skillId, isRoot, x, y));
    }


    public static void init() {

    }
}
