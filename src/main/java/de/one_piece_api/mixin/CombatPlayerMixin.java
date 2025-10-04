package de.one_piece_api.mixin;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.interfaces.IClassPlayer;
import de.one_piece_api.interfaces.ICombatPlayer;
import de.one_piece_api.interfaces.IDevilFruitPlayer;
import de.one_piece_api.interfaces.ISpellPlayer;
import de.one_piece_api.items.DevilFruitItem;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

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
