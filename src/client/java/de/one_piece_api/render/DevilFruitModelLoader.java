package de.one_piece_api.render;

import de.one_piece_api.OnePieceRPG;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DevilFruitModelLoader implements PreparableModelLoadingPlugin<Set<Identifier>> {

    // Only scan these namespaces for devil fruit models
    private static final Set<String> DEVIL_FRUIT_NAMESPACES = Set.of(
            "one_piece_api",
            "one_piece_content"
            // Add any other modpack/datapack namespaces here
    );

    public static final PreparableModelLoadingPlugin.DataLoader<Set<Identifier>> DATA_LOADER =
            (resourceManager, executor) -> CompletableFuture.supplyAsync(() -> {
                Set<Identifier> devilFruitIds = new HashSet<>();
                OnePieceRPG.LOGGER.info("=== LOAD DEVIL FRUIT MODELS ===");
                // Find all models in models/item/
                var resources = resourceManager.findResources("models/devil_fruit", path -> path.getPath().endsWith(".json"));

                resources.forEach((id, resource) -> {

                    String path = id.getPath();

                    if (path.startsWith("models/devil_fruit/") && path.endsWith(".json")) {
                        String modelPath = path.substring("models/devil_fruit/".length(), path.length() - ".json".length());
                        Identifier modelId = Identifier.of(id.getNamespace(), "devil_fruit/" + modelPath);
                        devilFruitIds.add(modelId);
                        OnePieceRPG.LOGGER.info("Discovered devil fruit model: {}", modelId);
                    }
                });

                OnePieceRPG.LOGGER.info("=== TOTAL DEVIL FRUIT MODELS: {} ===", devilFruitIds.size());
                return devilFruitIds;
            }, executor);

    @Override
    public void onInitializeModelLoader(Set<Identifier> data, ModelLoadingPlugin.Context context) {
        OnePieceRPG.LOGGER.info("Registering {} devil fruit models", data.size());
        context.addModels(data);
    }
}