package de.one_piece_api.config;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for a character class with level-based rewards.
 * <p>
 * A class defines a character archetype with its name, description, visual assets,
 * and a progression system that grants rewards at specific levels.
 *
 * @param name the display name of the class
 * @param description detailed description of the class
 * @param rewards list of level-gated rewards for this class
 * @param backTexture background texture for class selection screen
 * @param nameTexture name badge texture overlay
 */
public record ClassConfig(
        Text name,
        Text description,
        List<LevelReward> rewards,
        Identifier backTexture,
        Identifier nameTexture
) {

    /**
     * Represents a reward that is granted at a specific level.
     * <p>
     * When the player reaches the specified level, the reward is automatically
     * granted. This allows for progressive unlocking of abilities and bonuses.
     *
     * @param level the level at which this reward is granted
     * @param reward the reward configuration to grant
     */
    public record LevelReward(int level, SkillRewardConfig reward) {
        /**
         * Creates a new level-gated reward.
         *
         * @param level the required level (must be >= 1)
         * @param reward the reward configuration to grant (must not be null)
         * @throws IllegalArgumentException if level < 1 or reward is null
         */
        public LevelReward {
            if (level < 1) {
                throw new IllegalArgumentException("Level condition must be >= 1, got: " + level);
            }
            if (reward == null) {
                throw new IllegalArgumentException("Reward cannot be null");
            }
        }
    }

    /**
     * Parses a ClassConfig from a JSON element.
     *
     * @param rootElement the JSON element to parse
     * @param context the configuration context
     * @return Result containing either the parsed ClassConfig or parsing problems
     */
    public static Result<ClassConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
        return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
    }

    /**
     * Parses a ClassConfig from a JSON object.
     * <p>
     * Expected JSON structure:
     * <pre>{@code
     * {
     *   "name": "Swordsman",
     *   "description": "Master of the blade",
     *   "rewards": [
     *     {
     *       "level": 5,
     *       "reward": {
     *         "type": "one_piece_api:passive_ability",
     *         "data": {
     *           "abilities": ["one_piece_content:sword_mastery"]
     *         }
     *       }
     *     },
     *     {
     *       "level": 10,
     *       "reward": {
     *         "type": "one_piece_api:passive_ability",
     *         "data": {
     *           "abilities": ["one_piece_content:advanced_sword_mastery"]
     *         }
     *       }
     *     }
     *   ],
     *   "backTexture": "one_piece:textures/class/swordsman_bg.png",
     *   "nameTexture": "one_piece:textures/class/swordsman_name.png"
     * }
     * }</pre>
     *
     * @param rootObject the JSON object to parse
     * @param context the configuration context
     * @return Result containing either the parsed ClassConfig or parsing problems
     */
    public static Result<ClassConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
        var problems = new ArrayList<Problem>();

        // Parse name (required)
        Optional<Text> optName = rootObject.get("name")
                .andThen(titleElement -> BuiltinJson.parseText(titleElement, context.getServer().getRegistryManager()))
                .ifFailure(problems::add)
                .getSuccess();

        // Parse description (optional, defaults to empty)
        Text description = rootObject.get("description")
                .getSuccess()
                .flatMap(descElem -> BuiltinJson.parseText(descElem, context.getServer().getRegistryManager())
                        .ifFailure(problems::add)
                        .getSuccess())
                .orElseGet(Text::empty);

        // Parse rewards array (optional, defaults to empty list)
        List<LevelReward> rewards = rootObject.getArray("rewards")
                .getSuccess()
                .flatMap(rewardsArray -> {
                    // Use getAsList to parse each reward entry
                    return rewardsArray.<LevelReward, Problem>getAsList((index, rewardElement) -> {
                                return rewardElement.getAsObject().andThen(rewardObj -> {
                                    var rewardProblems = new ArrayList<Problem>();

                                    // Parse level condition (required)
                                    Optional<Integer> optLevel = rewardObj.getInt("level")
                                            .ifFailure(rewardProblems::add)
                                            .getSuccess();

                                    // Parse reward using SkillRewardConfig parser (required)
                                    Optional<SkillRewardConfig> optReward = rewardObj.get("reward")
                                            .andThen(rewardElem -> SkillRewardConfig.parse(rewardElem, context))
                                            .ifFailure(rewardProblems::add)
                                            .getSuccess();

                                    if (!rewardProblems.isEmpty()) {
                                        return Result.failure(Problem.combine(rewardProblems));
                                    }

                                    try {
                                        return Result.success(new LevelReward(
                                                optLevel.orElseThrow(),
                                                optReward.orElseThrow()
                                        ));
                                    } catch (IllegalArgumentException e) {
                                        return Result.failure(Problem.message(
                                                "Invalid reward at index " + index + ": " + e.getMessage()
                                        ));
                                    }
                                });
                            })
                            .mapFailure(Problem::combine)
                            .ifFailure(problems::add)
                            .getSuccess();
                })
                .orElseGet(List::of);

        // Parse background texture (required)
        Optional<Identifier> optBackTexture = rootObject.getString("backTexture")
                .ifFailure(problems::add)
                .getSuccess()
                .map(Identifier::of);

        // Parse name texture (required)
        Optional<Identifier> optNameTexture = rootObject.getString("nameTexture")
                .ifFailure(problems::add)
                .getSuccess()
                .map(Identifier::of);

        // If there were any problems, return failure
        if (!problems.isEmpty()) {
            return Result.failure(Problem.combine(problems));
        }

        // Otherwise return success with all required fields
        return Result.success(new ClassConfig(
                optName.orElseThrow(),
                description,
                rewards,
                optBackTexture.orElseThrow(),
                optNameTexture.orElseThrow()
        ));
    }
}