package de.one_piece_content_data.builder;

import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.config.DevilFruitPathConfig;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DevilFruitBuilder {
    private Identifier modelId;
    private final List<DevilFruitPathConfig> paths = new ArrayList<>();
    private final List<Identifier> instantPassives = new ArrayList<>();

    public DevilFruitBuilder() {
    }

    public DevilFruitBuilder addPassive(Identifier skillId) {
        instantPassives.add(skillId);
        return this;
    }

    public static class PathBuilder {
        private final List<Identifier> path = new ArrayList<>();

        public PathBuilder add(Identifier... skillIds) {
            path.addAll(Arrays.asList(skillIds));
            return this;
        }

        public DevilFruitPathConfig build() {
            return new DevilFruitPathConfig(path);
        }
    }

    public DevilFruitBuilder modelId(Identifier modelId) {
        this.modelId = modelId;
        return this;
    }

    public DevilFruitBuilder newPath(Function<PathBuilder, PathBuilder> builder) {
        paths.add(builder.apply(new PathBuilder()).build());
        return this;
    }

    public DevilFruitConfig build() {
        return new DevilFruitConfig(paths, instantPassives, modelId);
    }
}