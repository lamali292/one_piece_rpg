package de.one_piece_api.mixin_interface;

import net.minecraft.util.Identifier;

import java.util.Optional;

public interface StyledConnection {
    Optional<Identifier> onepiece$getStyle();
    void onepiece$setStyle(Identifier style);
}
