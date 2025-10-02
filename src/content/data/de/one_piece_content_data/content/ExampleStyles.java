package de.one_piece_content_data.content;

import de.one_piece_api.config.StyleConfig;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.registry.Registries;

public class ExampleStyles {
    public static Entry<StyleConfig> SWORDSMEN_STYLE = Registries.STYLES.register(
            ExampleMod.id("swordsmen_style"), new StyleConfig(0x009900)
    );
    public static Entry<StyleConfig> BRAWLER_STYLE = Registries.STYLES.register(
            ExampleMod.id("brawler_style"), new StyleConfig(0x990000)
    );
    public static Entry<StyleConfig> SNIPER_STYLE = Registries.STYLES.register(
            ExampleMod.id("sniper_style"), new StyleConfig(0x000099)
    );


    public static void init() {
    }



}
