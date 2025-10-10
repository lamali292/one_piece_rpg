package de.one_piece_api.reward;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.ability.handler.PassiveAbilityHandler;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardConfigContext;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.List;

/**
 * Reward that unlocks passive abilities through the SkillsMod system.
 * <p>
 * This reward can unlock multiple tiers of a passive ability as the player
 * levels up the skill node. Each tier can have a different passive ability.
 *
 * <h2>JSON Configuration Example:</h2>
 * <pre>{@code
 * {
 *   "type": "one_piece_api:passive_ability",
 *   "data": {
 *     "abilities": [
 *       "one_piece_content:enhanced_swimming",
 *       "one_piece_content:aquatic_mastery"
 *     ]
 *   }
 * }
 * }</pre>
 */
public record PassiveAbilityReward(List<Identifier> abilities) implements Reward {

    public static final Identifier ID = OnePieceRPG.id("passive_ability");

    /**
     * Registers this reward type with SkillsMod API.
     */
    public static void register() {
        SkillsAPI.registerReward(ID, PassiveAbilityReward::parse);
    }

    private static final Gson gson = new GsonBuilder().create();

    /**
     * Data structure for JSON parsing.
     */
    public record DataStructure(List<String> abilities) {
    }

    /**
     * Parses the reward configuration from JSON.
     */
    private static Result<PassiveAbilityReward, Problem> parse(RewardConfigContext context) {
        var dataResult = context.getData();
        if (dataResult.getFailure().isPresent()) {
            return Result.failure(dataResult.getFailure().get());
        }

        var data = dataResult.getSuccess();
        PassiveAbilityReward reward;

        try {
            var json = data.get().getJson();
            var parsed = gson.fromJson(json, DataStructure.class);

            if (parsed.abilities() == null || parsed.abilities().isEmpty()) {
                return Result.failure(Problem.message(
                        "Passive ability reward must have at least one ability"
                ));
            }

            // Convert string IDs to Identifier objects
            var abilityIds = parsed.abilities().stream()
                    .map(Identifier::of)
                    .toList();

            reward = new PassiveAbilityReward(abilityIds);

        } catch (Exception e) {
            return Result.failure(Problem.message(
                    "Failed to parse passive ability reward: " + e.getMessage()
            ));
        }

        return Result.success(reward);
    }

    /**
     * Converts this reward back to JSON format.
     */
    public JsonElement toJson() {
        var abilityStrings = this.abilities.stream()
                .map(Identifier::toString)
                .toList();
        DataStructure data = new DataStructure(abilityStrings);
        return gson.toJsonTree(data);
    }

    /**
     * Called when the reward is updated (player levels up the node).
     *
     * @param context contains player and count (current level of the node)
     */
    @Override
    public void update(RewardUpdateContext context) {
        int count = context.getCount();
        var player = context.getPlayer();
        var playerAbilities = PassiveAbilityHandler.getPlayerAbilities(player);
        for (Identifier abilityId : abilities) {
            playerAbilities.deactivate(abilityId);
        }
        if (count > 0) {
            int index = Math.min(count - 1, abilities.size() - 1);
            Identifier abilityToActivate = abilities.get(index);
            playerAbilities.activate(abilityToActivate);
        }
    }

    /**
     * Called when the reward is disposed (node is reset/removed).
     *
     * @param context contains player information
     */
    @Override
    public void dispose(RewardDisposeContext context) {
    }
}