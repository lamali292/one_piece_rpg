package de.one_piece_api.config;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;


public record SkillDefinitionReferenceConfig(
        String id
) {
    public static Result<SkillDefinitionReferenceConfig, Problem> parse(JsonElement rootElement) {
        return rootElement.getAsObject().andThen(SkillDefinitionReferenceConfig::parse);
    }

    public static Result<SkillDefinitionReferenceConfig, Problem> parse(JsonObject obj) {
        return obj.getString("id").andThen(s-> Result.success(new SkillDefinitionReferenceConfig(s)));
    }
}
