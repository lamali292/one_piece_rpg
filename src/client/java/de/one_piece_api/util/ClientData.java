package de.one_piece_api.util;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.config.DevilFruitConfig;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ClientData {

    public static final Observable<DevilFruitConfig> DEVIL_FRUIT_CONFIG = new Observable<>();
    public static final Observable<Map<Identifier, ClassConfig>> CLASS_CONFIG = new Observable<>();

    public static void init() {
    }
}
