package de.one_piece_api.data.experience;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceDisposeContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.Map;

public class TimeExperienceSource implements ExperienceSource {
    public static final Identifier ID = OnePieceRPG.id("time");
    public static final Prototype<TimeExperienceSource.Data> PROTOTYPE = Prototype.create(ID);
    static {
         PROTOTYPE.registerOperation(
                OnePieceRPG.id("get_ticks"),
                BuiltinPrototypes.NUMBER,
                OperationFactory.create(s->(double) s.ticks)
        );
    }

    private final Calculation<TimeExperienceSource.Data> calculation;
    public TimeExperienceSource(Calculation<TimeExperienceSource.Data> calculation) {
        this.calculation = calculation;
    }

    public record Data(ServerPlayerEntity player, int ticks) { }

    public int getValue(ServerPlayerEntity player, int ticks) {
        return (int) Math.round(calculation.evaluate(
                new TimeExperienceSource.Data(player, ticks)
        ));
    }

    @Override
    public void dispose(ExperienceSourceDisposeContext context) {

    }


    private static Result<TimeExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
        return context.getData()
                .andThen(JsonElement::getAsObject)
                .andThen(rootObject -> rootObject.noUnused(o -> parse(o, context)));
    }

    private static Result<TimeExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
        var problems = new ArrayList<Problem>();

        Variables<Data, Double> variables = rootObject.get("variables")
                .getSuccess()
                .flatMap(variablesElement -> Variables.parse(variablesElement, PROTOTYPE, context)
                        .ifFailure(problems::add)
                        .getSuccess()
                )
                .orElseGet(() -> Variables.create(Map.of()));

        var optCalculation = rootObject.get("experience")
                .andThen(experienceElement -> Calculation.parse(
                        experienceElement,
                        variables,
                        context
                ))
                .ifFailure(problems::add)
                .getSuccess();

        if (problems.isEmpty()) {
            return Result.success(new TimeExperienceSource(
                    optCalculation.orElseThrow()
            ));
        } else {
            return Result.failure(Problem.combine(problems));
        }
    }


    public static void register() {
        SkillsAPI.registerExperienceSource(
                ID,
                TimeExperienceSource::parse
        );
    }
}
