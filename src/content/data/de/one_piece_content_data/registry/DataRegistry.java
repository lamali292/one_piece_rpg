package de.one_piece_content_data.registry;

import net.minecraft.util.Identifier;

import java.util.*;

public record DataRegistry<T>(
        Map<Identifier, T> entries
) {

    public DataRegistry() {
        this(new LinkedHashMap<>());
    }

    public Entry<T> register(Identifier id, T entry) {
        entries.put(id, entry);
        return new Entry<>(id, entry);
    }
}