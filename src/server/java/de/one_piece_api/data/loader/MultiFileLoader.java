package de.one_piece_api.data.loader;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class MultiFileLoader<T> extends BaseLoader<T> {
    private final Map<Identifier, T> items = new HashMap<>();
    private final String folderName;

    public MultiFileLoader(String folderName, BiFunction<JsonElement, ConfigContext, Result<T, Problem>> parser) {
        super(parser);
        this.folderName = folderName;
    }

    @Override
    public Identifier getFabricId() {
        return OnePieceRPG.id(folderName + "_loader");
    }

    public Map<Identifier, T> getItems() {
        return items;
    }

    @Override
    public void reload(ResourceManager manager) {
        if (server == null) {
            return;
        }

        items.clear();
        HashMap<String, Integer> loaded = new HashMap<>();
        HashMap<String, Integer> failed = new HashMap<>();

        manager.findResources(folderName, path -> path.getPath().endsWith(".json")).forEach((fileId, resource) -> {
            String path = fileId.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (fileName.endsWith(".json")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            }
            Identifier id = Identifier.of(fileId.getNamespace(), fileName);
            ConfigContext context = createContext();

            try (var reader = resource.getReader()) {
                JsonElement root = JsonElement.parseReader(reader, JsonPath.create(fileId.toString()))
                        .getSuccess()
                        .orElseThrow();
                Result<T, Problem> result = parser.apply(root, context);

                result.getSuccess().ifPresent(item -> {
                    items.put(id, item);
                    loaded.merge(fileId.getNamespace(), 1, Integer::sum);
                });
            } catch (Exception e) {
                OnePieceRPG.LOGGER.error("[{}] Failed to load {}", folderName, id);
                failed.merge(fileId.getNamespace(), 1, Integer::sum);
            }
        });

        loaded.forEach((ns, e) -> {
            if (failed.containsKey(ns)) {
                OnePieceRPG.LOGGER.warn("[{}] Data pack `{}` loaded! ({}/{})", folderName, ns, e, e + failed.get(ns));
            } else {
                OnePieceRPG.LOGGER.info("[{}] Data pack `{}` loaded successfully!", folderName, ns);
            }
        });
    }
}