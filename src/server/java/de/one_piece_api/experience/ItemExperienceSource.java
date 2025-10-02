package de.one_piece_api.experience;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.registries.MyDataComponentTypes;
import net.minecraft.item.ItemStack;
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

public class ItemExperienceSource implements ExperienceSource {
    public static final Identifier ID = OnePieceRPG.id("item");
    private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

    static {
        PROTOTYPE.registerOperation(
                OnePieceRPG.id("get_player"),
                BuiltinPrototypes.PLAYER,
                OperationFactory.create(Data::player)
        );
        PROTOTYPE.registerOperation(
                OnePieceRPG.id("get_item_stack"),
                BuiltinPrototypes.ITEM_STACK,
                OperationFactory.create(Data::item)
        );
        PROTOTYPE.registerOperation(
                OnePieceRPG.id("get_count"),
                BuiltinPrototypes.NUMBER,
                OperationFactory.create(data -> (double) data.item.getCount())
        );
        PROTOTYPE.registerOperation(
                OnePieceRPG.id("get_xp"),
                BuiltinPrototypes.NUMBER,
                OperationFactory.create(data -> (double) data.item.getOrDefault(MyDataComponentTypes.XP, 0))
        );
    }

    private final Calculation<Data> calculation;

    public ItemExperienceSource(Calculation<Data> calculation) {
        this.calculation = calculation;
    }

    public record Data(ServerPlayerEntity player, ItemStack item) { }

    public int getValue(ServerPlayerEntity player, ItemStack item) {
        return (int) Math.round(calculation.evaluate(
                new Data(player, item)
        ));
    }

    @Override
    public void dispose(ExperienceSourceDisposeContext context) {
        // Nothing to do
    }

    private static Result<ItemExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
        return context.getData()
                .andThen(JsonElement::getAsObject)
                .andThen(rootObject -> rootObject.noUnused(o -> parse(o, context)));
    }

    private static Result<ItemExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
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
            return Result.success(new ItemExperienceSource(
                    optCalculation.orElseThrow()
            ));
        } else {
            return Result.failure(Problem.combine(problems));
        }
    }

    public static void register() {
        SkillsAPI.registerExperienceSource(
                ID,
                ItemExperienceSource::parse
        );
    }
}