package de.one_piece_api.config;

import net.minecraft.text.Text;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;

import java.util.ArrayList;
import java.util.Optional;

public record ClassConfig(Text name, Text description, String primary, String passive, ColorConfig primaryColor, ColorConfig secondaryColor,
                          IconConfig icon){


    public static Result<ClassConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
        return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
    }



    public record ColorPairConfig(ColorConfig primary, ColorConfig secondary) { }

    public static Result<ClassConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
        var problems = new ArrayList<Problem>();

        // Helper to parse required fields
        Optional<Text> name = rootObject.get("name")
                .andThen(titleElement -> BuiltinJson.parseText(titleElement, context.getServer().getRegistryManager()))
                .ifFailure(problems::add)
                .getSuccess(); // don't throw yet

        Text description = rootObject.get("description")
                .getSuccess()
                .flatMap(descElem -> BuiltinJson.parseText(descElem, context.getServer().getRegistryManager())
                        .ifFailure(problems::add)
                        .getSuccess())
                .orElse(Text.empty());

        Optional<String> primary = rootObject.getString("primary")
                .ifFailure(problems::add)
                .getSuccess();

        Optional<String> passive = rootObject.getString("passive")
                .ifFailure(problems::add)
                .getSuccess();

        Optional<ColorPairConfig> colors = rootObject.getObject("color")
                .ifFailure(problems::add)
                .andThen(colorObject -> {
                    var subProblems = new ArrayList<Problem>();
                    Optional<ColorConfig> primaryColor = colorObject.get("primary").getSuccess()
                            .map(ColorConfig::parse)
                            .flatMap(e -> e.ifFailure(subProblems::add)
                                    .getSuccess());
                    Optional<ColorConfig> secondaryColor = colorObject.get("secondary").getSuccess()
                            .map(ColorConfig::parse)
                            .flatMap(e -> e.ifFailure(subProblems::add)
                                    .getSuccess());
                    if (subProblems.isEmpty()) {
                        return Result.success(new ColorPairConfig(
                                primaryColor.orElseThrow(),
                                secondaryColor.orElseThrow()
                        ));
                    }
                    return Result.failure(Problem.combine(subProblems));
                }).ifFailure(problems::add)
                .getSuccess();
        Optional<IconConfig> icon = rootObject.get("icon")
                .getSuccess()
                .map(json -> IconConfig.parse(json, context))
                .flatMap(e -> e
                        .ifFailure(problems::add)
                        .getSuccess());

        // If there were any problems, return failure
        if (problems.isEmpty()) {
            return Result.success(new ClassConfig(
                    name.orElseThrow(),
                    description,
                    primary.orElseThrow(),
                    passive.orElseThrow(),
                    colors.orElseThrow().primary,
                    colors.orElseThrow().secondary,
                    icon.orElseThrow()
            ));
        }

        // Otherwise return success
        return Result.failure(Problem.combine(problems));
    }
}
