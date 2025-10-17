package de.one_piece_api.mixin_interface;

import de.one_piece_api.config.XpTimeConfig;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Interface for managing player XP and Skill Points
 */
public interface IXpPlayer {


    /**
     * Get the player's time-based XP configuration
     */
    XpTimeConfig onepiece$getXpTimeConfig();

    /**
     * Set the player's time-based XP configuration
     */
    void onepiece$setXpTimeConfig(XpTimeConfig config);

    /**
     * Get ticks since last time-based XP gain
     */
    int onepiece$getTicksSinceLastXp();

    /**
     * Set ticks since last time-based XP gain
     */
    void onepiece$setTicksSinceLastXp(int ticks);

    /**
     * Reset the XP timer
     */
    void onepiece$resetXpTimer();

    /**
     * Get last activity tick (for AFK detection)
     */
    long onepiece$getLastActivityTick();

    /**
     * Update last activity tick
     */
    void onepiece$updateActivity();

    /**
     * Check if player is considered AFK
     */
    boolean onepiece$isAfk();


    int onepiece$getXp();

    void onepiece$addXp(int amount);

    void onepiece$setXp(int newXp);

    int onepiece$getTotalSkillPoints();

    Map<Identifier, Integer> onepiece$getSkillPointsWithSources();

    int onepiece$getSkillPoints(Identifier source);

    void onepiece$setSkillPoints(int i, Identifier source);

    void onepiece$addSkillPoints(int i, Identifier source);
}