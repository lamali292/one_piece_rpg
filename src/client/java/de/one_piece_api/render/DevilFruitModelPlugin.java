package de.one_piece_api.render;

import de.one_piece_api.OnePieceRPG;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

public class DevilFruitModelPlugin implements ModelLoadingPlugin {

    @Override
    public void onInitializeModelLoader(Context context) {
        // Register model override
        context.modifyModelAfterBake().register((model, mutableContext) -> {
            ModelIdentifier id = mutableContext.topLevelId();

            // Check if this is our devil fruit item
            if (model != null && id instanceof ModelIdentifier(Identifier id1, String variant)
                    && id1.equals(OnePieceRPG.id("item/devil_fruit"))
                    && variant.equals(ModelIdentifier.INVENTORY_VARIANT)) {

                // Wrap the model with a custom one that checks components
                return new DevilFruitBakedModel(model, mutableContext.baker());
            }

            return model;
        });
    }
}