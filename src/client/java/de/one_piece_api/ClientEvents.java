package de.one_piece_api;

import de.one_piece_api.registries.MyDataComponentTypes;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ClientEvents {

    public static void register() {
        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
    }

    private static void onItemTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        if (stack.contains(MyDataComponentTypes.XP)) {
            Integer data = stack.get(MyDataComponentTypes.XP);
            if (data != null) {
                String xpString = String.format("%d XP", data);
                if (stack.getCount() > 1) {
                    xpString += " (x" + stack.getCount()+")";
                }
                lines.add(Text.literal(xpString).formatted(Formatting.GOLD));
            }
        }

    }

}
