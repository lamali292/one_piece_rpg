package de.one_piece_api.config;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.impl.util.ProblemImpl;

import java.util.List;

public record DevilFruitPathConfig(
        List<Identifier> skills
) {

    public static final Problem NO_SKILLS = new ProblemImpl("Missing 'skillDefinitions' array in DevilFruitPathConfig");

    public static Result<DevilFruitPathConfig, Problem> parse(JsonElement pathElement) {
        return pathElement.getAsObject()
                .andThen(DevilFruitPathConfig::parse);
    }

    public static Result<DevilFruitPathConfig, Problem> parse(JsonObject obj) {
        // Parse skillDefinitions array
        return obj.getArray("skills")
                .mapFailure(f -> NO_SKILLS)
                .andThen(array -> array.getAsList((index, el) -> el.getAsString().mapSuccess(Identifier::of))
                        .mapFailure(Problem::combine)
                        .mapSuccess(DevilFruitPathConfig::new)
                );
    }
}
