package de.one_piece_api.content;

import de.one_piece_api.OnePieceRPG;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.impl.config.ConfigContextImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class GenericLoader<T> implements SimpleSynchronousResourceReloadListener {
    private final Map<Identifier, T> items = new HashMap<>();
    private final BiFunction<JsonElement, ConfigContext, Result<T, Problem>> parser;
    private final String folderName;
    private MinecraftServer server;

    public GenericLoader(String folderName, BiFunction<JsonElement, ConfigContext, Result<T, Problem>> parser) {
        this.folderName = folderName;
        this.parser = parser;
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
        ConfigContext context = new ConfigContextImpl(server);
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

    public void setServer(MinecraftServer server) {
        this.server = server;
    }
}
