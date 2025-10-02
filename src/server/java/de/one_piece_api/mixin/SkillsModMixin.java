package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.ICategoryAccessor;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.util.ChangeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Optional;

@Mixin(value = SkillsMod.class, remap = false)
public abstract class SkillsModMixin implements ICategoryAccessor {


    @Accessor("categories")
    public abstract ChangeListener<Optional<Map<Identifier, CategoryConfig>>> getCategories();
}