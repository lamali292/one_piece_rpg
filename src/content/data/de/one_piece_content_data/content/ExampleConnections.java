package de.one_piece_content_data.content;

import com.google.gson.JsonObject;
import de.one_piece_content_data.datagen.ConnectionsBuilder;

public class ExampleConnections {

    public static JsonObject createClassConnections() {
        return new ConnectionsBuilder()
                .normal(e->e
                        .addUnidirectional("swordsmen_skill_1", "swordsmen_skill_2")
                        .addUnidirectional("swordsmen_skill_1", "swordsmen_skill_3")
                        .addUnidirectional("brawler_skill_1", "brawler_skill_2")
                        .addUnidirectional("brawler_skill_1", "brawler_skill_3")
                        .addUnidirectional("sniper_skill_1", "sniper_skill_2")
                        .addUnidirectional("sniper_skill_1", "sniper_skill_3")
                ).toJson();
    }
}
