package de.one_piece_api.util.interfaces;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.util.ChangeListener;

import java.util.Map;
import java.util.Optional;

public interface ICategoryAccessor {

    ChangeListener<Optional<Map<Identifier, CategoryConfig>>> getCategories();
}
