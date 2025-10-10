package de.one_piece_api.mixin_interface;

import net.minecraft.util.Identifier;

public interface IClassPlayer {
    void onepiece$setOnePieceClass(Identifier className);
    Identifier onepiece$getOnePieceClass();
}
