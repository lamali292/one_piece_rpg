package de.one_piece_content_data.builder;

import de.one_piece_api.config.ClassConfig;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating character class configurations with level-based rewards.
 */
public class OnePieceClassBuilder {
    private final Text name;
    private final Text description;
    private final List<ClassConfig.LevelReward> rewards = new ArrayList<>();
    private Identifier backTexture;
    private Identifier nameTexture;

    public OnePieceClassBuilder(Identifier id) {
        name = Text.translatable("class." + id.getNamespace() + "." + id.getPath() + ".name");
        description = Text.translatable("class." + id.getNamespace() + "." + id.getPath() + ".description");
    }

    /**
     * Adds a reward that will be granted at the specified level.
     *
     * @param level the level at which to grant this reward (must be >= 1)
     * @param rewardType the reward type identifier
     * @param rewardInstance the reward instance
     * @return this builder for method chaining
     */
    public OnePieceClassBuilder addReward(int level, Identifier rewardType, Reward rewardInstance) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be >= 1, got: " + level);
        }
        if (rewardInstance == null) {
            throw new IllegalArgumentException("Reward cannot be null");
        }

        SkillRewardConfig rewardConfig = new SkillRewardConfig(rewardType, rewardInstance);
        rewards.add(new ClassConfig.LevelReward(level, rewardConfig));
        return this;
    }

    /**
     * Adds a SkillRewardConfig directly.
     */
    public OnePieceClassBuilder addReward(int level, SkillRewardConfig rewardConfig) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be >= 1, got: " + level);
        }
        rewards.add(new ClassConfig.LevelReward(level, rewardConfig));
        return this;
    }

    public OnePieceClassBuilder background(Identifier backTexture, Identifier nameTexture) {
        this.backTexture = backTexture;
        this.nameTexture = nameTexture;
        return this;
    }

    public ClassConfig build() {
        if (backTexture == null || nameTexture == null) {
            throw new IllegalStateException("Background and name textures must be set");
        }

        return new ClassConfig(
                name,
                description,
                List.copyOf(rewards),
                backTexture,
                nameTexture
        );
    }
}