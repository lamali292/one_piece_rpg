package de.one_piece_api.config;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.colors.ColorConfig;

import java.util.ArrayList;
import java.util.Optional;

public record StyleConfig(ColorConfig color) {

    public StyleConfig(int argb) {
        this(new ColorConfig(argb));
    }

    public static Result<StyleConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
        return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
    }

    public static Result<StyleConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
        var problems = new ArrayList<Problem>();

        Optional<ColorConfig> color = rootObject.get("color")
                .getSuccess()
                .map(ColorConfig::parse)
                .flatMap(e -> e.ifFailure(problems::add)
                        .getSuccess());

        if (problems.isEmpty()) {
            return Result.success(new StyleConfig(
                    color.orElseThrow()
            ));
        }

        // Otherwise return success
        return Result.failure(Problem.combine(problems));
    }
}
