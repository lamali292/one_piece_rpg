package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.IStaminaPlayer;
import de.one_piece_api.init.MyAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
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

import java.util.function.Function;

/**
 * Mixin class that adds stamina functionality to PlayerEntity.
 * Implements the IStaminaPlayer interface to provide stamina management,
 * including regeneration, persistence, and crouch-based bonuses.
 */
@Mixin(PlayerEntity.class)
public class StaminaPlayerMixin implements IStaminaPlayer {

    /**
     * Tracked data for storing the player's current stamina value.
     * Synced between client and server using Minecraft's data tracker system.
     */
    @Unique
    private static final TrackedData<Float> STAMINA =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);

    /**
     * Gets the current player instance as start PlayerEntity.
     * Used internally to access player methods safely.
     *
     * @return the player entity instance
     */
    @Unique
    private PlayerEntity onepiece$getStaminaSelf() {
        return (PlayerEntity) (Object) this;
    }

    /**
     * Injects custom stamina attributes into the player's attribute container.
     * Adds MAX_STAMINA, STAMINA_BASE_REGEN, STAMINA_CROUCH_MULT, and STAMINA_CROUCH_ADD attributes.
     *
     * @param cir callback info returnable containing the attribute builder
     */
    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void addCustomStaminaAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
                .add(MyAttributes.MAX_STAMINA)
                .add(MyAttributes.STAMINA_BASE_REGEN)
                .add(MyAttributes.STAMINA_CROUCH_MULT)
                .add(MyAttributes.STAMINA_CROUCH_ADD);
    }

    /**
     * Initializes the stamina tracked data when the player entity is created.
     * Sets the initial stamina value to 0.
     *
     * @param builder the data tracker builder
     * @param ci callback info
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedStaminaData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(STAMINA, 0F);
    }

    /**
     * Saves the player's stamina data to NBT when the player is serialized.
     * Called automatically when the player logs out or the world is saved.
     *
     * @param nbt the NBT compound to write data to
     * @param ci callback info
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onepiece$saveStaminaData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putDouble("Stamina", onepiece$getStamina());
    }

    /**
     * Loads the player's stamina data from NBT when the player is deserialized.
     * Called automatically when the player logs in or respawns.
     *
     * @param nbt the NBT compound to read data from
     * @param ci callback info
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onepiece$loadStaminaData(NbtCompound nbt, CallbackInfo ci) {
        onepiece$setStamina(nbt.getDouble("Stamina"));
    }

    /**
     * Sets the player's stamina to start specific value.
     * Automatically clamps the value between 0 and the player's maximum stamina.
     *
     * @param stamina the stamina value to set
     */
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

    /**
     * Adds stamina to the player's current stamina value.
     * Respects the maximum stamina limit.
     *
     * @param stamina the amount of stamina to add
     */
    @Override
    public void onepiece$addStamina(double stamina) {
        double currentStamina = onepiece$getStamina();
        onepiece$setStamina(currentStamina + stamina);
    }

    /**
     * Removes stamina from the player's current stamina value.
     * Prevents stamina from going below 0.
     *
     * @param stamina the amount of stamina to remove
     */
    @Override
    public void onepiece$removeStamina(double stamina) {
        double currentStamina = onepiece$getStamina();
        onepiece$setStamina(currentStamina - stamina);
    }

    /**
     * Gets the player's current stamina value.
     *
     * @return the current stamina as start double
     */
    @Override
    public double onepiece$getStamina() {
        return onepiece$getStaminaSelf().getDataTracker().get(STAMINA);
    }

    /**
     * Internal tick counter for stamina regeneration timing.
     * Increments each tick and resets when regeneration occurs.
     */
    @Unique
    private int stamina_tick = 0;

    /**
     * Internal tick counter for tracking how long the player has been crouching
     * within the current regeneration cycle.
     * Used to calculate the boost factor for stamina regeneration.
     */
    @Unique
    private int boost_tick = 0;

    /**
     * Updates the player's stamina each tick, handling regeneration logic.
     * Stamina regenerates every 20 ticks (1 second).
     * Uses linear interpolation based on the proportion of time spent crouching
     * to smoothly blend between base regeneration and boosted regeneration rates.
     */
    @Override
    public void onepiece$updateStamina() {
        int STAMINA_REGEN_TICKS = 20;
        if (stamina_tick >= STAMINA_REGEN_TICKS) {
            var baseAttribute = onepiece$getStaminaSelf().getAttributeInstance(MyAttributes.STAMINA_BASE_REGEN);
            var crouchMultAttribute = onepiece$getStaminaSelf().getAttributeInstance(MyAttributes.STAMINA_CROUCH_MULT);
            var crouchAddAttribute = onepiece$getStaminaSelf().getAttributeInstance(MyAttributes.STAMINA_CROUCH_ADD);
            if (baseAttribute != null && crouchMultAttribute != null && crouchAddAttribute != null) {
                double staminaRegen = getStaminaRegen(baseAttribute, crouchMultAttribute, crouchAddAttribute);
                onepiece$addStamina(staminaRegen);

            }
            stamina_tick = 0;
            boost_tick = 0;
        }
        stamina_tick++;
        if (onepiece$hasStaminaBoost()) {
            boost_tick++;
        }
    }

    /**
     * Calculates the stamina regeneration amount using linear interpolation.
     * The regeneration value is blended between base regeneration and boosted regeneration
     * based on the proportion of time the player spent crouching during the regeneration cycle.
     * <p>
     * Formula: lerp(baseRegen, crouchRegen, boostProgress)
     * where boostProgress = boost_tick / stamina_tick
     * and crouchRegen = (baseRegen + crouchAdditive) * crouchMultiplier
     *
     * @param baseRegenAttribute the base stamina regeneration attribute instance
     * @param crouchMultiplierAttribute the crouch multiplier attribute instance
     * @param crouchAdditiveAttribute the crouch additive bonus attribute instance
     * @return the calculated stamina regeneration amount
     */
    @Unique
    private double getStaminaRegen(EntityAttributeInstance baseRegenAttribute, EntityAttributeInstance crouchMultiplierAttribute, EntityAttributeInstance crouchAdditiveAttribute) {
        double baseRegen = baseRegenAttribute.getValue();
        double crouchMultiplier = crouchMultiplierAttribute.getValue();
        double crouchAdditive = crouchAdditiveAttribute.getValue();

        double boostProgress = (double) boost_tick / (double) stamina_tick;
        double crouchRegen = (baseRegen + crouchAdditive) * crouchMultiplier;

        // Interpolate between base and crouch regeneration based on time spent crouching
        return interpolate(baseRegen, crouchRegen, boostProgress, progress -> progress * progress);
    }

    /**
     * Performs interpolation between two values using a custom easing function.
     * Calculates a value that lies between the start and end points based on the given progress,
     * with the interpolation curve determined by the provided easing function.
     * <p>
     * Formula: (1 - t) * startValue + t * endValue, where t = easingFunction(progress)
     *
     * @param startValue the starting value (returned when progress is 0)
     * @param endValue the ending value (returned when progress is 1)
     * @param progress the interpolation progress, typically between 0.0 and 1.0
     * @param easingFunction a function that transforms the progress value to create different interpolation curves
     *                       (e.g., x -> x for linear, x -> x*x for ease-in quadratic)
     * @return the interpolated value between startValue and endValue
     */
    @Unique
    private double interpolate(double startValue, double endValue, double progress, Function<Double, Double> easingFunction) {
        double easedProgress = easingFunction.apply(progress);
        return (1 - easedProgress) * startValue + easedProgress * endValue;
    }


    /**
     * Checks if the player currently has start stamina regeneration boost.
     * The boost is active when the player is crouching/sneaking.
     *
     * @return true if the player is crouching, false otherwise
     */
    @Override
    public boolean onepiece$hasStaminaBoost() {
        return onepiece$getStaminaSelf().isSneaking();
    }
}