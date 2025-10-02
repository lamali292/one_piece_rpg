package de.one_piece_content_data.content;

import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.builder.ConnectionsBuilder;
import de.one_piece_api.config.ConnectionsConfig;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.registry.Registries;

public class ExampleConnections {



    public static Entry<ConnectionsConfig> CLASS_CONNECTIONS = Registries.CONNECTIONS.register(ExampleMod.id("class_connections"), createClassConnections() );
    public static ConnectionsConfig createClassConnections() {
        return new ConnectionsBuilder()
                .normal(e->e
                        .addUnidirectional(ExampleSkillDefinitions.SWORDSMEN_SKILL_1.id(), ExampleSkillDefinitions.SWORDSMEN_SKILL_2.id(), ExampleStyles.SWORDSMEN_STYLE.id())
                        .addUnidirectional(ExampleSkillDefinitions.SWORDSMEN_SKILL_1.id(), ExampleSkillDefinitions.SWORDSMEN_SKILL_3.id(), ExampleStyles.SWORDSMEN_STYLE.id())
                        .addUnidirectional(ExampleSkillDefinitions.BRAWLER_SKILL_1.id(), ExampleSkillDefinitions.BRAWLER_SKILL_2.id(), ExampleStyles.BRAWLER_STYLE.id())
                        .addUnidirectional(ExampleSkillDefinitions.BRAWLER_SKILL_1.id(), ExampleSkillDefinitions.BRAWLER_SKILL_3.id(), ExampleStyles.BRAWLER_STYLE.id())
                        .addUnidirectional(ExampleSkillDefinitions.SNIPER_SKILL_1.id(), ExampleSkillDefinitions.SNIPER_SKILL_2.id(), ExampleStyles.SNIPER_STYLE.id())
                        .addUnidirectional(ExampleSkillDefinitions.SNIPER_SKILL_1.id(), ExampleSkillDefinitions.SNIPER_SKILL_3.id(), ExampleStyles.SNIPER_STYLE.id())
                        .addUnidirectional(ExampleSkillDefinitions.SWORDSMEN_SKILL_1.id(), ExampleSkillDefinitions.REQUIRES_BOTH.id(), ExampleStyles.SWORDSMEN_STYLE.id())
                        .addUnidirectional(ExampleSkillDefinitions.BRAWLER_SKILL_1.id(), ExampleSkillDefinitions.REQUIRES_BOTH.id(), ExampleStyles.BRAWLER_STYLE.id()))
                .build();
    }

    public static void init() {
    }
}
