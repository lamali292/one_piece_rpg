package de.one_piece_content_data.content;

import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.builder.DevilFruitBuilder;
import de.one_piece_content_data.registry.Registries;
import net.minecraft.util.Identifier;

import static de.one_piece_content_data.content.ExampleSkillDefinitions.*;

public class ExampleDevilFruits {


    public static final Entry<DevilFruitConfig> SUNA_SUNA_NO_MI = Registries.DEVIL_FRUITS.register(
            ExampleMod.id("suna_suna_no_mi"),
            new DevilFruitBuilder()
                    .newPath(builder -> builder
                            .add(
                                    SANDSTORM.id(),
                                    SANDSTORM_MODIFIER_1.id())
                    ).newPath(builder -> builder
                            .add(
                                    DUMMY_DEFINITIONS.get("sand_hand").id(),
                                    DUMMY_DEFINITIONS.get("sand_drain").id())
                    ).newPath(builder -> builder
                            .add(
                                    DUMMY_DEFINITIONS.get("sand_blade").id(),
                                    DUMMY_DEFINITIONS.get("sand_spikes").id())
                    ).newPath(builder -> builder
                            .add(
                                    DUMMY_DEFINITIONS.get("quicksand").id())
                    )
                    .modelId(Identifier.of(ExampleMod.MOD_ID, "devil_fruit/suna_suna_no_mi.json"))
                    .build()
    );

    public static final Entry<DevilFruitConfig> DEVIL_FRUIT_1 = Registries.DEVIL_FRUITS.register(
            ExampleMod.id("test_fruit_1"),
            new DevilFruitBuilder()
                    .newPath(builder -> builder
                            .add(
                                    ITEM_ENTRIES.subList(0,4).stream().map(Entry::id).toArray(Identifier[]::new)
                            )
                    ).newPath(builder -> builder
                            .add(
                                    ITEM_ENTRIES.subList(4,8).stream().map(Entry::id).toArray(Identifier[]::new)
                            )
                    ).newPath(builder -> builder
                            .add(
                                    ITEM_ENTRIES.subList(8,12).stream().map(Entry::id).toArray(Identifier[]::new)
                            )
                    ).newPath(builder -> builder
                            .add(
                                    ITEM_ENTRIES.subList(12,16).stream().map(Entry::id).toArray(Identifier[]::new)
                            )
                    ).newPath(builder -> builder
                            .add(
                                    ITEM_ENTRIES.subList(16,20).stream().map(Entry::id).toArray(Identifier[]::new)
                            )
                    )
                    .modelId(Identifier.of(ExampleMod.MOD_ID, "devil_fruit/suna_suna_no_mi.json"))
                    .build()
    );


    public static void init() {
    }


}
