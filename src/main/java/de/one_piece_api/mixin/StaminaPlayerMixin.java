package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IStaminaPlayer;
import de.one_piece_api.items.DevilFruitItem;
import de.one_piece_api.registries.MyAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class StaminaPlayerMixin implements IStaminaPlayer {

    @Unique
    private static final TrackedData<Float> STAMINA =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);


    @Unique
    private PlayerEntity onepiece$getStaminaSelf() {
        return (PlayerEntity) (Object) this;
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void addCustomStaminaAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(MyAttributes.MAX_STAMINA)
                .add(MyAttributes.STAMINA_REGEN);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedStaminaData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(STAMINA, 0F);
    }

    // --- NBT Save ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onepiece$saveStaminaData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putDouble("Stamina", onepiece$getStamina());
    }


    // --- NBT Load ---
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onepiece$loadStaminaData(NbtCompound nbt, CallbackInfo ci) {
        onepiece$setStamina(nbt.getDouble("Stamina"));
    }


    @Override
    public void onepiece$setStamina(double stamina) {
        var attribute = onepiece$getStaminaSelf().getAttributeInstance(MyAttributes.MAX_STAMINA);
        if (attribute != null) {
            var maxStamina = attribute.getValue();
            if (stamina > maxStamina) {
                stamina = maxStamina;
            } else if (stamina < 0) {
                stamina = 0;
            }
            onepiece$getStaminaSelf().getDataTracker().set(STAMINA, (float) stamina);
        }
    }

    @Override
    public void onepiece$addStamina(double stamina) {
        double currentStamina = onepiece$getStamina();
        onepiece$setStamina(currentStamina + stamina);
    }

    @Override
    public void onepiece$removeStamina(double stamina) {
        double currentStamina = onepiece$getStamina();
        onepiece$setStamina(currentStamina - stamina);
    }

    @Override
    public double onepiece$getStamina() {
        return onepiece$getStaminaSelf().getDataTracker().get(STAMINA);
    }

    @Unique
    private int stamina_tick = 0;

    @Override
    public void onepiece$updateStamina() {
        int STAMINA_REGEN_TICKS = 20;
        if (stamina_tick >= STAMINA_REGEN_TICKS) {
            var attribute = onepiece$getStaminaSelf().getAttributeInstance(MyAttributes.STAMINA_REGEN);
            if (attribute != null) {
                var staminaRegen = attribute.getValue();
                onepiece$addStamina(staminaRegen);
            }
            stamina_tick = 0;
        }
        stamina_tick++;
    }


}
