package de.one_piece_content_data;

import de.one_piece_content_data.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleDatagen implements DataGeneratorEntrypoint {
    public static final String MOD_ID = "one_piece_content_datagen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        LOGGER.info("Example Content datagen initializing");
        //MySounds.register();

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(LangGen::new);
        pack.addProvider(SoundGen::new);

        pack.addProvider(SpellsGen::new);
        pack.addProvider(SkillGen::new);
        pack.addProvider(SkillConnectionsGen::new);
        pack.addProvider(SkillDefinitionGen::new);
        pack.addProvider(DevilFruitGen::new);
        pack.addProvider(ClassGen::new);
    }


}
