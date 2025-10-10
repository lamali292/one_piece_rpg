package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.ICombatPlayer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public class CombatPlayerMixin implements ICombatPlayer {

    @Unique
    private boolean combatMode = false;

    @Override
    public boolean onepiece$isCombatMode() {
        return combatMode;
    }

    @Override
    public void onepiece$setCombatMode(boolean combatMode) {
        this.combatMode = combatMode;
    }


}
