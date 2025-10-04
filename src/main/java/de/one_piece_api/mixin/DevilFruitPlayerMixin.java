package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IDevilFruitPlayer;
import de.one_piece_api.items.DevilFruitItem;
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
public class DevilFruitPlayerMixin implements IDevilFruitPlayer {

    @Unique
    private static final TrackedData<String> DEVIL_FRUIT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);


    @Unique
    private PlayerEntity onepiece$getDevilFruitSelf() {
        return (PlayerEntity) (Object) this;
    }

    // --- initDataTracker Injection ---
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedCombatData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(DEVIL_FRUIT, DevilFruitItem.DEFAULT_DEVIL_FRUIT.toString());
    }



    @Override
    public String onepiece$getDevilFruit() {
        return onepiece$getDevilFruitSelf().getDataTracker().get(DEVIL_FRUIT);
    }

    @Override
    public void onepiece$setDevilFruit(String fruit) {
        onepiece$getDevilFruitSelf().getDataTracker().set(DEVIL_FRUIT, fruit);
    }


    // --- NBT Save ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onepiece$saveDevilFruitData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("DevilFruit", onepiece$getDevilFruit());
    }


    // --- NBT Load ---
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onepiece$loadDevilFruitData(NbtCompound nbt, CallbackInfo ci) {
        onepiece$setDevilFruit(nbt.getString("DevilFruit"));
    }
}
