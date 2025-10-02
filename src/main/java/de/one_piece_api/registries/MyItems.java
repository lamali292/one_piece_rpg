package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.items.DevilFruitItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MyItems {
    public static class Container { Item item;}
    public record LoreLine(String text, Formatting formatting) {
        public record Translatable(String translationKey, LoreLine line) { }
    }
    public record Entry(Identifier id, String title, List<LoreLine> lore,
                        Function<Item.Settings, Item> factory, Item.Settings settings,
                        Container container) {
        public Entry(Identifier id, String title, List<LoreLine> lore, Item.Settings settings) {
            this(id, title, lore, Item::new, settings);
        }
        public Entry(Identifier id, String title, List<LoreLine> lore,
                     Function<Item.Settings, Item> factory, Item.Settings settings) {
            this(id, title, lore, factory, settings, new Container());
        }
        public Item item() {
            return container.item;
        }
        public List<LoreLine.Translatable> loreTranslation() {
            var keys = new ArrayList<LoreLine.Translatable>();
            int index = 0;
            for (var line : lore) {
                String key = "item." + id.getNamespace() + "." + id.getPath() + ".lore." + index++;
                keys.add(new LoreLine.Translatable(key, line));
            }
            return keys;
        }
    }
    public static final ArrayList<Entry> ENTRIES = new ArrayList<>();
    private static Entry add(Entry entry) {
        ENTRIES.add(entry);
        return entry;
    }

    public static final Entry DEVIL_FRUIT = add(new Entry(
            OnePieceRPG.id("devil_fruit"),
            "Devil Fruit",
            List.of(),
            DevilFruitItem::new,
            new Item.Settings().rarity(Rarity.RARE)
    ));

    public static void register() {
        for (Entry entry : ENTRIES) {
            List<Text> lore = entry.loreTranslation().stream()
                    .map(line -> (Text) Text.translatable(line.translationKey())
                            .formatted(line.line().formatting()))
                    .toList();
            Item item = entry.factory().apply(entry.settings()
                    .component(DataComponentTypes.LORE, new LoreComponent(List.of(), lore) )
            );
            entry.container.item = item;
            Registry.register(Registries.ITEM, entry.id(), item);
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            for(var entry: ENTRIES) {
                content.add(entry.item());
            }
        });
    }
}
