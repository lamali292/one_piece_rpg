package de.one_piece_api.init;

import de.one_piece_api.reward.PassiveAbilityReward;
import de.one_piece_api.reward.SpellContainerReward;

public class MyRewards {

    public static void register() {
        SpellContainerReward.register();
        PassiveAbilityReward.register();
    }
}
