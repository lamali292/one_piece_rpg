package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.util.Identifier;

/**
 * Registry for custom font identifiers used in the One Piece RPG mod.
 * <p>
 * This class provides identifiers for custom fonts that can be used throughout
 * the mod's UI elements. The fonts must be defined in the mod's assets.
 *
 * @see Identifier
 */
public class MyFonts {

    /**
     * Identifier for the Montserrat font.
     * <p>
     * A modern, geometric sans-serif font suitable for body text and UI elements.
     */
    public static final Identifier MONTSERRAT = OnePieceRPG.id("montserrat");

    /**
     * Identifier for the Press Start 2P font.
     * <p>
     * A pixel-style font reminiscent of classic video games, suitable for
     * retro-styled UI elements and headings.
     */
    public static final Identifier PRESS_START = OnePieceRPG.id("press_start");

    /**
     * Registers custom fonts with the game.
     * <p>
     * This method is called during mod initialization to set up custom fonts.
     */
    public static void register() {

    }
}