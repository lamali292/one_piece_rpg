package de.one_piece_api;

import de.one_piece_api.registries.MyDataComponentTypes;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Handles client-side event registration and callbacks.
 * <p>
 * This class manages various client events, including custom item tooltip rendering
 * for items with XP data components. It registers callbacks during client initialization
 * to enhance the user interface with mod-specific information.
 *
 * @see ItemTooltipCallback
 * @see MyDataComponentTypes
 */
public class ClientEvents {

    /**
     * Registers all client-side event callbacks.
     * <p>
     * This method should be called during client initialization to set up
     * event listeners for tooltip rendering and other client events.
     */
    public static void register() {
        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
    }

    /**
     * Handles item tooltip rendering for items with XP data.
     * <p>
     * When an item contains XP data via {@link MyDataComponentTypes#XP}, this method
     * adds a gold-colored line to the tooltip showing the XP value. If the stack
     * contains multiple items, it also displays the stack count multiplier.
     *
     * @param stack the item stack being examined
     * @param context the tooltip context providing additional information
     * @param type the type of tooltip being rendered
     * @param lines the list of tooltip lines to modify
     */
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