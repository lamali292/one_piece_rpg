package de.one_piece_content_data.content;

import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.builder.DevilFruitBuilder;
import de.one_piece_content_data.registry.Registries;
import net.minecraft.util.Identifier;

public class ExampleDevilFruits {


    public static final Entry<DevilFruitConfig> SUNA_SUNA_NO_MI = Registries.DEVIL_FRUITS.register(
            ExampleMod.id("suna_suna_no_mi"),
            addSunaSunaNoMi()
    );

    public static DevilFruitConfig addSunaSunaNoMi() {
        return new DevilFruitBuilder()
                .newPath(builder -> builder
                        .add(ExampleSkillDefinitions.SANDSTORM.id(), ExampleSkillDefinitions.SANDSTORM_MODIFIER_1.id())
                ).newPath(builder -> builder
                        .add(ExampleSkillDefinitions.DUMMY_DEFINITIONS.get("sand_hand").id(), ExampleSkillDefinitions.DUMMY_DEFINITIONS.get("sand_drain").id())
                ).newPath(builder -> builder
                        .add(ExampleSkillDefinitions.DUMMY_DEFINITIONS.get("sand_blade").id(), ExampleSkillDefinitions.DUMMY_DEFINITIONS.get("sand_spikes").id())
                ).newPath(builder -> builder
                        .add(ExampleSkillDefinitions.DUMMY_DEFINITIONS.get("quicksand").id())
                )
                .modelId(Identifier.of(ExampleMod.MOD_ID, "devil_fruit/suna_suna_no_mi.json"))
                .build();
    }


    public static void init() {
    }


}
