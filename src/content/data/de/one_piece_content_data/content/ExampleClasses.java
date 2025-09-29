package de.one_piece_content_data.content;

import de.one_piece_api.config.ClassConfig;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.datagen.OnePieceClassBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class ExampleClasses {

    public static final ArrayList<OnePieceClass> ENTRIES = new ArrayList<>();

    private static OnePieceClass add(OnePieceClass entry) {
        ENTRIES.add(entry);
        return entry;
    }
    public record OnePieceClass(Identifier id, ClassConfig config) { }

    static {
        add(new OnePieceClassBuilder(ExampleMod.id("fishman"))
                .primary("fishman_primary")
                .passive("fishman_passive")
                .color(0x2E86C1, 0x1B4F72)
                .itemIcon(new ItemStack(Items.TROPICAL_FISH))
                .build());

        add(new OnePieceClassBuilder(ExampleMod.id("human"))
                .primary("human_primary")
                .passive("human_passive")
                .color(0xE74C3C, 0xA93226)
                .itemIcon(new ItemStack(Items.IRON_SWORD))
                .build());

        add(new OnePieceClassBuilder(ExampleMod.id("mink"))
                .primary("mink_primary")
                .passive("mink_passive")
                .color(0xF39C12, 0xD68910)
                .itemIcon(new ItemStack(Items.RABBIT_FOOT))
                .build());
    }

}
