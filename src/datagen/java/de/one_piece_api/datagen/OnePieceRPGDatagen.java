package de.one_piece_api.datagen;

import de.one_piece_api.datagen.generator.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class OnePieceRPGDatagen implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(LangGen::new);
		// pack.addProvider(ModelGen::new);
		pack.addProvider(RecipeGen::new);
		//pack.addProvider(SkillCategoryGen::new);
	}

}
