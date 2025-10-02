package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class MySounds {


    public static SoundEvent AMBIENT = registerSound(OnePieceRPG.id("ambient"));
    public static SoundEvent registerSound(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {

    }
}
