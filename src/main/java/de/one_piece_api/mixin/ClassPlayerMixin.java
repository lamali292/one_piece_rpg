package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.IClassPlayer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ClassPlayerMixin implements IClassPlayer {

    @Unique
    private static final TrackedData<String> ONE_PIECE_CLASS =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);

    @Unique
    private PlayerEntity onepiece$getClassSelf() {
        return (PlayerEntity) (Object) this;
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedClassData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(ONE_PIECE_CLASS, "");
    }

    @Override
    public Identifier onepiece$getOnePieceClass() {
        String value = onepiece$getClassSelf().getDataTracker().get(ONE_PIECE_CLASS);
        if (value == null || value.isEmpty()) {
            return Identifier.ofVanilla("empty");
        }
        return Identifier.of(value);
    }

    @Override
    public void onepiece$setOnePieceClass(Identifier className) {
        onepiece$getClassSelf().getDataTracker().set(ONE_PIECE_CLASS, className.toString());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onepiece$saveClassData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("OnePieceClass", onepiece$getOnePieceClass().toString());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onepiece$loadClassData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("OnePieceClass")) {
            onepiece$setOnePieceClass(Identifier.of(nbt.getString("OnePieceClass")));
        }
    }
}