package de.one_piece_api.config;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.impl.util.ProblemImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DevilFruitConfig(
        List<DevilFruitPathConfig> paths,
        List<SkillDefinitionReferenceConfig> instantPassives,
        Identifier modelId
) {

    public static Result<DevilFruitConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
        return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
    }

    public static Result<DevilFruitConfig, Problem> parse(
            JsonObject obj, ConfigContext context
    ) {
        List<Problem> problems = new ArrayList<>();


        // Parse spells
        Result<List<DevilFruitPathConfig>, Problem> spellsResult = obj.getObject("spells").getSuccess()
                .map(pathObject -> pathObject.getAsMap((key, element) ->
                                DevilFruitPathConfig.parse(element))
                        .mapFailure(problem -> Problem.combine(problem.values()))
                        .mapSuccess(map -> map.values().stream().toList())
                )
                .orElse(Result.failure(new ProblemImpl("Missing 'path' object in DevilFruitPathsConfig")));
        Optional<List<DevilFruitPathConfig>> optSpells = spellsResult
                .ifFailure(problems::add)
                .getSuccess();

        // Parse passives
        List<SkillDefinitionReferenceConfig> passives = obj.getArray("passives")
                .andThen(array -> array.getAsList((index, el) -> SkillDefinitionReferenceConfig.parse(el))
                        .mapFailure(Problem::combine)
                ).getSuccess().orElse(List.of());
        // Parse modelId
        Optional<String> modelIdStr = obj.getString("modelId").getSuccess();
        Optional<Identifier> optModelId = modelIdStr.map(Identifier::of);

        if (problems.isEmpty() && optSpells.isPresent() && optModelId.isPresent()) {
            return Result.success(new DevilFruitConfig(
                    optSpells.get(),
                    passives,
                    optModelId.get()
            ));
        } else {
            if (optModelId.isEmpty()) problems.add(new ProblemImpl("Missing 'modelId' in DevilFruitConfig"));
            return Result.failure(problems.isEmpty() ? new ProblemImpl("Unknown parsing error in DevilFruitConfig") : Problem.combine(problems));
        }
    }


}
