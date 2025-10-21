package de.one_piece_api.event;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.init.MyDataComponentTypes;
import de.one_piece_api.network.payload.ClassConfigPayload;
import de.one_piece_api.render.TextureFramebufferCache;
import de.one_piece_api.util.ClientData;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Handles client-side event registration and callbacks.
 * <p>
 * This class manages various client events, including custom item tooltip rendering
 * for items with XP data components, data synchronization on resource reload, and
 * data management on player connection.
 *
 * @see ItemTooltipCallback
 * @see MyDataComponentTypes
 * @see ClientPlayConnectionEvents
 */
public class ClientEvents {

    /**
     * Registers all client-side event callbacks.
     * <p>
     * This method should be called during client initialization to set up
     * event listeners for tooltip rendering, resource reloading, and connection events.
     */
    public static void register() {
        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
        registerReloadListener();
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
                    xpString += " (x" + stack.getCount() + ")";
                }
                lines.add(Text.literal(xpString).formatted(Formatting.GOLD));
            }
        }
    }

    /**
     * Registers a resource reload listener to request fresh data after /reload.
     * <p>
     * When the server executes /reload and reloads data packs, this listener
     * requests updated configuration data from the server to ensure the client
     * stays synchronized.
     */
    private static void registerReloadListener() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return OnePieceRPG.id("texture_cache_clear");
                    }
                    @Override
                    public void reload(ResourceManager manager) {
                        TextureFramebufferCache.clearCache();
                    }
                }
        );
    }

}