package de.one_piece_api.config.attribute;

import de.one_piece_api.init.MyAttributes;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.expression.DefaultParser;
import net.puffish.skillsmod.expression.Expression;

import java.util.List;
import java.util.Set;

/**
 * Configuration for attribute scaling curves that define how player attributes scale with level.
 * <p>
 * This record holds mathematical expressions that are evaluated based on the player's level
 * to determine various attribute values. Each expression can use the variable "level" and
 * supports standard mathematical operations and functions.
 * </p>
 *
 * <h3>JSON Format:</h3>
 * <pre>{@code
 * {
 *   "max_stamina": "100 + (level ^ 1.5) * 5",
 *   "stamina_base_regen": "1 + level * 0.5",
 *   "stamina_crouch_multiplier": "1.5 + level * 0.1",
 *   "stamina_crouch_additive": "2 + level * 0.25"
 * }
 * }</pre>
 *
 * <h3>Expression Support:</h3>
 * Expressions support:
 * <ul>
 *   <li>Basic operators: +, -, *, /, ^ (power)</li>
 *   <li>Functions: sqrt, abs, floor, ceil, round, max, min, clamp, etc.</li>
 *   <li>Constants: e, pi, tau</li>
 *   <li>Variable: level (player's current level)</li>
 * </ul>
 *
 * @param maxStamina Expression for calculating maximum stamina at a given level
 * @param staminaBaseRegen Expression for calculating base stamina regeneration rate at a given level
 * @param staminaCrouchMultiplier Expression for calculating the stamina regeneration multiplier when crouching at a given level
 * @param staminaCrouchAdditive Expression for calculating the additional stamina regeneration when crouching at a given level
 */
public record AttributeScalingConfig(
        Expression<Double> maxStamina,
        Expression<Double> staminaBaseRegen,
        Expression<Double> staminaCrouchMultiplier,
        Expression<Double> staminaCrouchAdditive
) {

    /**
     * Parses an {@link AttributeScalingConfig} from a JSON element.
     *
     * @param jsonElement the JSON element containing the configuration
     * @param context the configuration context for parsing
     * @return a {@link Result} containing either the parsed config or a {@link Problem} describing the parsing error
     */
    public static Result<AttributeScalingConfig, Problem> parse(JsonElement jsonElement, ConfigContext context) {
        return jsonElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
    }

    /**
     * Parses an {@link AttributeScalingConfig} from a JSON object.
     * <p>
     * This method validates and parses all four required expression fields. If any field
     * is missing or contains an invalid expression, all errors are collected and returned
     * as a combined problem.
     * </p>
     *
     * @param rootObject the JSON object containing the attribute scaling configuration
     * @param context the configuration context for parsing
     * @return a {@link Result} containing either the parsed config or a combined {@link Problem} with all parsing errors
     */
    public static Result<AttributeScalingConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
        List<Problem> problems = new java.util.ArrayList<>();

        var maxStamina = parseExpression(rootObject, "max_stamina").ifFailure(problems::add).getSuccess();
        var staminaBaseRegen = parseExpression(rootObject, "stamina_base_regen").ifFailure(problems::add).getSuccess();
        var staminaCrouchMult = parseExpression(rootObject, "stamina_crouch_multiplier").ifFailure(problems::add).getSuccess();
        var staminaCrouchAdd = parseExpression(rootObject, "stamina_crouch_additive").ifFailure(problems::add).getSuccess();

        if (problems.isEmpty()) {
            return Result.success(new AttributeScalingConfig(
                    maxStamina.orElseThrow(),
                    staminaBaseRegen.orElseThrow(),
                    staminaCrouchMult.orElseThrow(),
                    staminaCrouchAdd.orElseThrow()
            ));
        } else {
            return Result.failure(Problem.combine(problems));
        }

    }

    /**
     * Parses a mathematical expression from a JSON object field.
     * <p>
     * The expression string is parsed using {@link DefaultParser} and must use "level" as the variable name.
     * </p>
     *
     * @param obj the JSON object containing the field
     * @param key the field name to extract
     * @return a {@link Result} containing either the parsed expression or a {@link Problem} if parsing failed
     */
    private static Result<Expression<Double>, Problem> parseExpression(JsonObject obj, String key) {
        return obj.get(key)
                .andThen(JsonElement::getAsString)
                .andThen(expr -> DefaultParser.parse(expr, Set.of("level")));
    }

    /**
     * Evaluates the maximum stamina expression at the specified level.
     * Corresponds to {@link MyAttributes#MAX_STAMINA}.
     *
     * @param level the player's level to evaluate at
     * @return the calculated maximum stamina value
     */
    public double evaluateMaxStamina(int level) {
        return maxStamina.eval(java.util.Map.of("level", (double) level));
    }

    /**
     * Evaluates the base stamina regeneration expression at the specified level.
     * Corresponds to {@link MyAttributes#STAMINA_BASE_REGEN}.
     *
     * @param level the player's level to evaluate at
     * @return the calculated base stamina regeneration rate per second
     */
    public double evaluateStaminaBaseRegen(int level) {
        return staminaBaseRegen.eval(java.util.Map.of("level", (double) level));
    }

    /**
     * Evaluates the stamina regeneration crouch multiplier expression at the specified level.
     * This multiplier is applied to the stamina regeneration rate when the player is crouching.
     * Corresponds to {@link MyAttributes#STAMINA_CROUCH_MULT}.
     *
     * @param level the player's level to evaluate at
     * @return the calculated stamina regeneration multiplier for crouching
     */
    public double evaluateStaminaCrouchMultiplier(int level) {
        return staminaCrouchMultiplier.eval(java.util.Map.of("level", (double) level));
    }

    /**
     * Evaluates the additional stamina regeneration when crouching expression at the specified level.
     * This value is added to the base stamina regeneration before applying the crouch multiplier.
     * Corresponds to {@link MyAttributes#STAMINA_CROUCH_ADD}.
     *
     * @param level the player's level to evaluate at
     * @return the calculated additional stamina regeneration for crouching
     */
    public double evaluateStaminaCrouchAdditive(int level) {
        return staminaCrouchAdditive.eval(java.util.Map.of("level", (double) level));
    }
}