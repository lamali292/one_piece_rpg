package de.one_piece_content_data;

import com.google.gson.Gson;
import de.one_piece_content_data.content.*;
import de.one_piece_content_data.data.JsonParser;
import de.one_piece_content_data.datagen.*;
import de.one_piece_content_data.registry.DataRegistry;
import de.one_piece_content_data.registry.Registries;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleDatagen implements DataGeneratorEntrypoint {
    public static final String MOD_ID = "one_piece_content_datagen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        LOGGER.info("Example Content datagen initializing");
        //MySounds.register();

        ExampleClasses.init();
        ExampleStyles.init();
        ExampleSpells.init();
        ExampleDevilFruits.init();
        ExampleSkillDefinitions.init();
        ExampleSkills.init();
        ExampleConnections.init();
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(LangGen::new);
        pack.addProvider(SoundGen::new);


        addProvider(pack, Registries.SKILL_DEFINITION, "skill_definition", JsonParser.GSON);
        addProvider(pack, Registries.SKILL_TREE_ENTRIES, "skill_tree", JsonParser.GSON);
        addProvider(pack, Registries.STYLES, "styles", JsonParser.GSON);
        addProvider(pack, Registries.CLASSES, "one_piece_class", JsonParser.GSON);
        addProvider(pack, Registries.DEVIL_FRUITS, "devil_fruit", JsonParser.GSON);
        addProvider(pack, Registries.CONNECTIONS, "connections", JsonParser.GSON);
        addProvider(pack, Registries.SPELLS, "spell", JsonParser.SPELL_GSON);
    }

    public static <T> void addProvider(FabricDataGenerator.Pack pack, DataRegistry<T> provider, String folder, Gson gson) {
        pack.addProvider((FabricDataOutput out) -> new RegistryDataProvider<>(out, provider, folder, gson));
    }


}
