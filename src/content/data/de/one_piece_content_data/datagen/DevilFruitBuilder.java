package de.one_piece_content_data.datagen;

import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.config.DevilFruitPathConfig;
import de.one_piece_api.config.SkillDefinitionReferenceConfig;
import de.one_piece_content_data.content.ExampleDevilFruits;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DevilFruitBuilder {
    private Identifier modelId, identifier;
    private final List<DevilFruitPathConfig> paths = new ArrayList<>();
    private final List<SkillDefinitionReferenceConfig> instantPassives = new ArrayList<>();

    public DevilFruitBuilder(Identifier identifier) {
        this.identifier = identifier;
    }

    public DevilFruitBuilder addPassive(String skillId) {
        String id = DataGenUtil.generateDeterministicId(skillId);
        instantPassives.add(new SkillDefinitionReferenceConfig(id));
        return this;
    }

    public static class PathBuilder {
        private final List<SkillDefinitionReferenceConfig> path = new ArrayList<>();

        public PathBuilder add(String... skillIds) {
            for (String skillId : skillIds) {
                String id = DataGenUtil.generateDeterministicId(skillId);
                path.add(new SkillDefinitionReferenceConfig(id));
            }
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

    public ExampleDevilFruits.DevilFruit build() {
        return new ExampleDevilFruits.DevilFruit(identifier, new DevilFruitConfig(paths, instantPassives, modelId));
    }
}