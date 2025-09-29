package de.one_piece_content_data.content;

import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.datagen.DevilFruitBuilder;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class ExampleDevilFruits {


    public static final ArrayList<DevilFruit> ENTRIES = new ArrayList<>();

    private static DevilFruit add(DevilFruit entry) {
        ENTRIES.add(entry);
        return entry;
    }
    public record DevilFruit(Identifier id, DevilFruitConfig config) { }

    public static final DevilFruit SUNA_SUNA_NO_MI = add(addSunaSunaNoMi());
    public static DevilFruit addSunaSunaNoMi() {
        return new DevilFruitBuilder(Identifier.of(ExampleMod.MOD_ID, "suna_suna_no_mi"))
                .newPath(builder -> builder
                        .add("sandstorm", "sandstorm_modifier_1")
                ).newPath(builder -> builder
                        .add("sand_hand", "sand_drain")
                ).newPath(builder -> builder
                        .add("sand_blade", "sand_spikes")
                ).newPath(builder -> builder
                        .add("quicksand")
                ).addPassive(ExampleSkills.BRAWLER_SKILL_1.config().id())
                .modelId(Identifier.of(ExampleMod.MOD_ID, "devil_fruit/suna_suna_no_mi.json"))
                .build();
    }


}
