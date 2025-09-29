package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class MyFonts {
    public static final Identifier MONTSERRAT = OnePieceRPG.id("montserrat");
    public static final Identifier PRESS_START = OnePieceRPG.id("press_start");

    public static void register() {
        MinecraftClient client = MinecraftClient.getInstance();
    }
}
