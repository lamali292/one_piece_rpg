package de.one_piece_content_data.registry;

import net.minecraft.util.Identifier;

public record Entry<T>(Identifier id, T entry) {

}
