package de.one_piece_api.datagen.generator;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class RecipeGen extends FabricRecipeProvider {
    public RecipeGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter recipeExporter) {
        /*ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, MyItems.ORB_OF_OBLIVION.item())
                .pattern(" X ")
                .pattern("XCX")
                .pattern(" X ")
                .input('X', Items.EXPERIENCE_BOTTLE)
                .input('C', Items.DIAMOND)
                .criterion(FabricRecipeProvider.hasItem(Items.EXPERIENCE_BOTTLE), FabricRecipeProvider.conditionsFromItem(Items.EXPERIENCE_BOTTLE))
                .offerTo(recipeExporter);*/
    }
}
