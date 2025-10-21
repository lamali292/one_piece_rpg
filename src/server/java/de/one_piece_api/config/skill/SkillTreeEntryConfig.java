package de.one_piece_api.config.skill;


import de.one_piece_api.mixin_interface.ISkillTypeProvider;
import de.one_piece_api.mixin_interface.SkillType;
import de.one_piece_api.util.DataGenUtil;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonArray;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;

import java.util.*;

public record SkillTreeEntryConfig(Identifier definition, boolean isRoot, int x, int y, SkillType type)  {


    public static Result<SkillsConfig, Problem> parse(JsonElement jsonElement, ConfigContext configContext) {
        return parseArray(jsonElement).mapSuccess(c->{
            Map<String, SkillConfig> map = new HashMap<>();
            c.forEach(e->{
                String id = DataGenUtil.generateDeterministicId(e.definition());
                SkillConfig skill = new SkillConfig(id, e.x(), e.y(), e.definition().toString(), e.isRoot());
                ((ISkillTypeProvider) (Object) skill).onepiece$setSkillType(e.type());
                map.put(id, skill);
            });
            return new SkillsConfig(map);
        });
    }

    public static Result<List<SkillTreeEntryConfig>, Problem> parseArray(JsonElement rootElement) {
        return rootElement.getAsArray().andThen(SkillTreeEntryConfig::parseArray);
    }

    private static Result<List<SkillTreeEntryConfig>, Problem> parseArray(JsonArray array) {
        return array.getAsList((i, element) -> parseEntry(element))
                .mapFailure(Problem::combine);
    }


    public static Result<SkillTreeEntryConfig, Problem> parseEntry(JsonElement rootElement) {
        return rootElement.getAsObject().andThen(SkillTreeEntryConfig::parseEntry);
    }

    private static Result<SkillTreeEntryConfig, Problem> parseEntry(JsonObject rootObject) {
        ArrayList<Problem> problems = new ArrayList<>();

        // Parse definition (required)
        var definitionResult = rootObject.get("definition")
                .andThen(e->e.getAsString().mapSuccess(Identifier::of))
                .ifFailure(problems::add);

        // Parse isRoot (optional, defaults to false)
        boolean isRoot = rootObject.get("isRoot")
                .andThen(JsonElement::getAsBoolean)
                .ifFailure(problems::add)
                .getSuccess()
                .orElse(false);

        // Parse x (required)
        var xResult = rootObject.get("x")
                .andThen(JsonElement::getAsInt)
                .ifFailure(problems::add);

        // Parse y (required)
        var yResult = rootObject.get("y")
                .andThen(JsonElement::getAsInt)
                .ifFailure(problems::add);

        var typeResult = rootObject.get("type")
                .andThen(JsonElement::getAsString)
                .ifFailure(problems::add)
                .mapSuccess(SkillType::getType);

        if (!problems.isEmpty()) {
            return Result.failure(Problem.combine(problems));
        }

        return Result.success(new SkillTreeEntryConfig(
                definitionResult.getSuccess().orElseThrow(),
                isRoot,
                xResult.getSuccess().orElseThrow(),
                yResult.getSuccess().orElseThrow(),
                typeResult.getSuccess().orElseThrow()
        ));
    }


}