package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.ICombatPlayer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class CombatPlayerMixin implements ICombatPlayer {

    @Unique
    private PlayerEntity onepiece$getCombatSelf() {
        return (PlayerEntity) (Object) this;
    }


    @Unique
    private static final TrackedData<Boolean> COMBAT_MODE =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);


    @Override
    public boolean onepiece$isCombatMode() {
        return onepiece$getCombatSelf().getDataTracker().get(COMBAT_MODE);
    }

    @Override
    public void onepiece$setCombatMode(boolean combatMode) {
        onepiece$getCombatSelf().getDataTracker().set(COMBAT_MODE, combatMode);
    }


    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedCombatData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(COMBAT_MODE, false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCombatData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("onepiece_combat_mode", onepiece$getCombatSelf().getDataTracker().get(COMBAT_MODE));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCombatData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("onepiece_combat_mode")) {
            onepiece$getCombatSelf().getDataTracker().set(COMBAT_MODE, nbt.getBoolean("onepiece_combat_mode"));
        }
    }

}
