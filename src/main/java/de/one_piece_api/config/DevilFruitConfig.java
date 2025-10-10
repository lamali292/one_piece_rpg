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
        List<Identifier> passives,
        Identifier modelId
) {

    public static final DevilFruitConfig DEFAULT = new DevilFruitConfig(List.of(), List.of(), Identifier.of(""));


    public static Result<DevilFruitConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
        return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
    }

    public static Result<DevilFruitConfig, Problem> parse(
            JsonObject obj, ConfigContext context
    ) {
        List<Problem> problems = new ArrayList<>();


        // Parse spells
        Optional<List<DevilFruitPathConfig>> optSpells = obj.getArray("paths").getSuccess()
                .map(jsonArray -> jsonArray.stream().map(e->
                        DevilFruitPathConfig.parse(e)
                                .ifFailure(problems::add)
                                .getSuccess()
                ).filter(Optional::isPresent).map(Optional::get).toList());


        // Parse passives
        List<Identifier> passives = obj.getArray("passives")
                .andThen(array -> array.getAsList((index, el) -> el.getAsString().mapSuccess(Identifier::of))
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
