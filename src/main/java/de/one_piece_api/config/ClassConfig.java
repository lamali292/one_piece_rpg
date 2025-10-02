package de.one_piece_api.config;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.colors.ColorConfig;

import java.util.ArrayList;
import java.util.Optional;

public record ClassConfig(Text name, Text description, String primary, String passive, ColorConfig primaryColor, ColorConfig secondaryColor,
                          Identifier backTexture, Identifier nameTexture){


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


        Optional<ColorConfig> primaryColor = rootObject.get("primaryColor").getSuccess()
                .map(ColorConfig::parse)
                .flatMap(e -> e.ifFailure(problems::add)
                        .getSuccess());
        Optional<ColorConfig> secondaryColor = rootObject.get("secondaryColor").getSuccess()
                .map(ColorConfig::parse)
                .flatMap(e -> e.ifFailure(problems::add)
                        .getSuccess());

        Optional<Identifier> backTexture = rootObject.getString("backTexture")
                .ifFailure(problems::add)
                .getSuccess().map(Identifier::of);
        Optional<Identifier> nameTexture = rootObject.getString("nameTexture")
                .ifFailure(problems::add)
                .getSuccess().map(Identifier::of);

        // If there were any problems, return failure
        if (problems.isEmpty()) {
            return Result.success(new ClassConfig(
                    name.orElseThrow(),
                    description,
                    primary.orElseThrow(),
                    passive.orElseThrow(),
                    primaryColor.orElseThrow(),
                    secondaryColor.orElseThrow(),
                    backTexture.orElseThrow(),
                    nameTexture.orElseThrow()
            ));
        }

        // Otherwise return success
        return Result.failure(Problem.combine(problems));
    }
}
