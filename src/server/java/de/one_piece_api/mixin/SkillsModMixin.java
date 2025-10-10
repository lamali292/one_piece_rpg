package de.one_piece_api.mixin;

import de.one_piece_api.event.EventRegistry;
import de.one_piece_api.util.interfaces.ICategoryAccessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.util.ChangeListener;
import net.puffish.skillsmod.util.PointSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(value = SkillsMod.class, remap = false)
public abstract class SkillsModMixin implements ICategoryAccessor {


    @Accessor("categories")
    public abstract ChangeListener<Optional<Map<Identifier, CategoryConfig>>> getCategories();

    @Inject(method = "setPoints(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/puffish/skillsmod/config/CategoryConfig;Lnet/puffish/skillsmod/server/data/CategoryData;Lnet/minecraft/util/Identifier;IZ)V",
            at = @At("HEAD"))
    private void onSetPoints(ServerPlayerEntity player, CategoryConfig category,
                             CategoryData categoryData, Identifier source,
                             int count, boolean isSilent, CallbackInfo ci) {
        // Check if this is an experience-based point gain (level up)
        if (source.equals(PointSources.EXPERIENCE)) {
            int oldLevel = categoryData.getPoints(PointSources.EXPERIENCE);
            if (count > oldLevel) {
                EventRegistry.LEVEL_UP.invoker().onLevelUp(player, category.id(), oldLevel, count);
            }
        }
    }



}