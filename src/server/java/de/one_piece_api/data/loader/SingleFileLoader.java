package de.one_piece_api.data.loader;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.Optional;
import java.util.function.BiFunction;

public class SingleFileLoader<T> extends BaseLoader<T> {
    private T data;
    private final Identifier fileId;

    public SingleFileLoader(Identifier fileId, BiFunction<JsonElement, ConfigContext, Result<T, Problem>> parser) {
        super(parser);
        this.fileId = fileId;
    }

    @Override
    public Identifier getFabricId() {
        return OnePieceRPG.id(fileId.getPath().replace("/", "_") + "_loader");
    }

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    @Override
    public void reload(ResourceManager manager) {
        if (server == null) {
            return;
        }

        data = null;
        manager.getResource(fileId).ifPresent(resource -> {
            ConfigContext context = createContext();
            try (var reader = resource.getReader()) {
                JsonElement root = JsonElement.parseReader(reader, JsonPath.create(fileId.toString()))
                        .getSuccess()
                        .orElseThrow();

                Result<T, Problem> result = parser.apply(root, context);

                result.ifSuccess(item -> {
                    data = item;
                    OnePieceRPG.LOGGER.info("Successfully loaded {}", fileId);
                }).ifFailure(problem -> {
                    OnePieceRPG.LOGGER.error("Failed to parse {}: {}", fileId, problem);
                });

            } catch (Exception e) {
                OnePieceRPG.LOGGER.error("Failed to load {}", fileId, e);
            }
        });

        if (data == null) {
            OnePieceRPG.LOGGER.warn("File not found or failed to load: {}", fileId);
        }
    }
}