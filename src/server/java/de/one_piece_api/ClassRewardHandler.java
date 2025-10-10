package de.one_piece_api;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.data.loader.DataLoaders;
import de.one_piece_api.mixin_interface.IClassPlayer;
import de.one_piece_api.util.OnePieceCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.impl.rewards.RewardUpdateContextImpl;

import java.util.List;

public class ClassRewardHandler {

    /**
     * Called when a player selects or changes their class.
     * Handles clearing old class rewards and granting new class rewards.
     */
    public static void onClassUpdate(ServerPlayerEntity player, Identifier oldClassId, Identifier newClassId) {
        OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Player {} changed class: {} → {}",
                player.getName().getString(),
                oldClassId != null ? oldClassId : "none",
                newClassId != null ? newClassId : "none");

        // Remove rewards from old class
        if (oldClassId != null) {
            OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Clearing rewards for old class {}", oldClassId);
            clearRewards(player, oldClassId);
        }

        // Apply rewards for new class
        if (newClassId != null) {
            OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Applying rewards for new class {}", newClassId);
            applyRewards(player, newClassId);
        }
    }

    private static List<ClassConfig.LevelReward> getRewards(Identifier classID) {
        ClassConfig config = DataLoaders.CLASS_LOADER.getItems().get(classID);
        if (config == null) {
            OnePieceRPG.LOGGER.warn("[ClassRewardHandler] No class config found for {}", classID);
            return List.of();
        }
        return config.rewards();
    }

    /**
     * Remove all rewards for the player's current class.
     */
    public static void clearRewards(ServerPlayerEntity player) {
        if (!(player instanceof IClassPlayer classPlayer)) {
            return;
        }
        var classID = classPlayer.onepiece$getOnePieceClass();
        clearRewards(player, classID);
    }

    /**
     * Remove all rewards for the given class.
     */
    private static void clearRewards(ServerPlayerEntity player, Identifier classID) {
        if (classID == null) {
            OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Skipping clearRewards: null classID for player {}", player.getName().getString());
            return;
        }

        var rewards = getRewards(classID);
        OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Clearing {} rewards for player {} (class {})",
                rewards.size(), player.getName().getString(), classID);

        for (var reward : rewards) {
            var rewardInstance = reward.reward().instance();
            var context = new RewardUpdateContextImpl(player, 0, true); // amount 0 → remove
            rewardInstance.update(context);
            OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER,"Removed reward at level {} for player {}",
                    reward.level(), player.getName().getString());
        }
    }

    /**
     * Apply all rewards matching the player's class and level.
     */
    private static void applyRewards(ServerPlayerEntity player, Identifier classID) {
        if (classID == null) {
            OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Skipping applyRewards: null classID for player {}", player.getName().getString());
            return;
        }

        OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER,  "Applying class rewards for player {} with class {}",
                player.getName().getString(), classID);

        var optLevel = SkillsMod.getInstance().getCurrentLevel(player, OnePieceCategory.ID);
        if (optLevel.isEmpty()) {
            OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Player {} has no current level in category {}",
                    player.getName().getString(), OnePieceCategory.ID);
            return;
        }

        int level = optLevel.get();
        var rewards = getRewards(classID);

        OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER,"Found {} rewards for class {}. Player level = {}",
                rewards.size(), classID, level);

        for (var reward : rewards) {
            int requiredLevel = reward.level();
            if (level >= requiredLevel) {
                OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Applying reward (requiredLevel = {}, playerLevel = {}) for player {}",
                        requiredLevel, level, player.getName().getString());

                var rewardInstance = reward.reward().instance();
                var context = new RewardUpdateContextImpl(player, 1, false);
                rewardInstance.update(context);
            } else {
                OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Skipping reward (requiredLevel = {}, playerLevel = {}) for player {}",
                        requiredLevel, level, player.getName().getString());
            }
        }
    }

    /**
     * Reset all rewards for the player and reapply based on class and level.
     */
    public static void refreshRewards(ServerPlayerEntity player) {
        if (player instanceof IClassPlayer classPlayer) {
            var classId = classPlayer.onepiece$getOnePieceClass();
            if (classId != null) {
                OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER, "Refreshing rewards for player {} (class {})",
                        player.getName().getString(), classId);
                clearRewards(player, classId);
                applyRewards(player, classId);
            } else {
                OnePieceRPG.debug(OnePieceRPG.CLASS_REWARD_HANDLER,"No class found for player {}. Skipping refresh.",
                        player.getName().getString());
            }
        }
    }
}
