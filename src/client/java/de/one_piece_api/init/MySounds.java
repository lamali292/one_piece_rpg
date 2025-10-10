package de.one_piece_api.init;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Registry for custom sound events in the One Piece RPG mod.
 * <p>
 * This class manages the registration of custom sound events that can be played
 * throughout the mod. Sound files must be defined in the mod's assets under the
 * sounds directory.
 *
 * @see SoundEvent
 * @see Registries#SOUND_EVENT
 */
public class MySounds {

    /**
     * Ambient sound event for atmospheric background audio.
     * <p>
     * This sound can be used for environmental ambience or mood setting.
     */
    public static SoundEvent AMBIENT = registerSound(OnePieceRPG.id("ambient"));

    /**
     * Registers a sound event with the given identifier.
     * <p>
     * This method creates and registers a new {@link SoundEvent} in the game's
     * sound registry. The sound file must exist in the mod's assets at
     * {@code assets/<namespace>/sounds/<path>.ogg}.
     *
     * @param id the identifier for the sound event
     * @return the registered {@link SoundEvent}
     */
    public static SoundEvent registerSound(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    /**
     * Registers all custom sound events.
     * <p>
     * This method should be called during mod initialization to ensure all sounds
     * are registered.
     */
    public static void register() {

    }
}