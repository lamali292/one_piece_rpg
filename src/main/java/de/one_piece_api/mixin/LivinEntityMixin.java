package de.one_piece_api.mixin;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivinEntityMixin {


    @Inject(method = "swimUpward", at = @At("HEAD"), cancellable = true)
    protected void swimUpward(TagKey<Fluid> fluid, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) {
            if (!isShallowWater(player, 3)) {
                ((LivingEntity)(Object)this).setPose(EntityPose.SWIMMING);
                ci.cancel();
            }
        }
    }


    private boolean isShallowWater(PlayerEntity player, int maxDepth) {
        World world = player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // Find the bottom of the water column by going down
        BlockPos bottomPos = playerPos;
        int depthCheck = 0;
        while (world.getFluidState(bottomPos.down()).isIn(FluidTags.WATER)) {
            bottomPos = bottomPos.down();
            depthCheck++;
            // Early exit if we already know it's too deep
            if (depthCheck > maxDepth) {
                return false;
            }
        }

        // Count total water depth from bottom to surface
        int totalWaterDepth = 0;
        BlockPos checkPos = bottomPos;

        while (world.getFluidState(checkPos).isIn(FluidTags.WATER)) {
            totalWaterDepth++;
            checkPos = checkPos.up();

            // If total depth exceeds max, it's too deep
            if (totalWaterDepth > maxDepth) {
                return false;
            }
        }

        // Check if there's air at the top
        if (!world.getBlockState(checkPos).isAir()) {
            return false; // No air above the water column
        }

        // Allow swimming ONLY if total water depth is 1-3 blocks with air above
        return totalWaterDepth > 0 && totalWaterDepth <= maxDepth;
    }





}

