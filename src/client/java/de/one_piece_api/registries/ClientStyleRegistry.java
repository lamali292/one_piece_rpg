package de.one_piece_api.registries;

import de.one_piece_api.config.StyleConfig;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientStyleRegistry {
    private static final Map<Identifier, StyleConfig> styles = new HashMap<>();

    public static void setStyles(Map<Identifier, StyleConfig> newStyles) {
        styles.clear();
        styles.putAll(newStyles);
    }

    public static Optional<StyleConfig> getStyle(Identifier id) {
        return Optional.ofNullable(styles.get(id));
    }
}