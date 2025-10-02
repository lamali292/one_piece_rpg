package de.one_piece_api;

import de.one_piece_api.content.CategoryLoader;
import de.one_piece_api.content.DataLoader;
import de.one_piece_api.experience.ItemExperienceSource;
import de.one_piece_api.experience.TimeExperienceSource;
import de.one_piece_api.network.SyncStylesPayload;
import de.one_piece_api.registries.MyCommands;
import de.one_piece_api.registries.MyDataComponentTypes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.config.CategoryConfig;

import java.util.*;

public class ServerEvents {

    // Track which screen handlers already have our listener
    private static final WeakHashMap<ScreenHandler, Boolean> trackedHandlers = new WeakHashMap<>();
    private static final Map<UUID, Map<Integer, ItemStack>> processedStacks = new HashMap<>();

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(ServerEvents::onServerTick);
        ServerPlayConnectionEvents.JOIN.register(ServerEvents::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            processedStacks.remove(handler.getPlayer().getUuid());
        });

        ServerLifecycleEvents.SERVER_STARTING.register(ServerEvents::onServerStarted);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(ServerEvents::onEndDataPackReload);
    }

    private static void onServerStarted(MinecraftServer minecraftServer) {
        reloadCategoryData();
    }



    private static void onEndDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        if (success) {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                var packet = new SyncStylesPayload(DataLoader.STYLE_LOADER.getItems());
                ServerPlayNetworking.send(player, packet);
            });

            reloadCategoryData();
        }
    }

    public static void reloadCategoryData() {
        CategoryConfig categoryConfig = CategoryLoader.buildCategory(
                DataLoader.CONNECTIONS_LOADER.getItems(),
                DataLoader.SKILL_DEFINITION_LOADER.getItems(),
                DataLoader.DEVIL_FRUIT_LOADER.getItems(),
                DataLoader.SKILL_LOADER.getItems()
        );
        CategoryLoader.addCategory(categoryConfig.id(), categoryConfig);

    }




    private static void onServerTick(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(ServerEvents::onServerPlayerTick);
    }

    private static final Map<UUID, Integer> playerTickCounters = new HashMap<>();
    private static final int TICKS_PER_HOUR= 20 * 60 * 60; // 20 ticks/sec * 60 sec * 10 min = 12000 ticks
    private static void onServerPlayerTick(ServerPlayerEntity player) {
       UUID playerId = player.getUuid();
        int currentTicks = playerTickCounters.getOrDefault(playerId, 0) + 1;

        if (currentTicks >= TICKS_PER_HOUR) {
            // Give XP every hour
            player.sendMessage(Text.of("You received 50 XP because you were 1h on server"), false);
            SkillsAPI.updateExperienceSources(
                    player,
                    TimeExperienceSource.class,
                    experienceSource -> experienceSource.getValue(player, 50)
            );
            playerTickCounters.put(playerId, 0);
        } else {
            playerTickCounters.put(playerId, currentTicks);
        }


        ScreenHandler currentHandler = player.currentScreenHandler;
        if (currentHandler != null && !trackedHandlers.containsKey(currentHandler)) {
            addListenerToHandler(player, currentHandler);
            trackedHandlers.put(currentHandler, true);
        }
    }

    private static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        processedStacks.put(player.getUuid(), new HashMap<>());

        // Add listener to player's inventory screen
        addListenerToHandler(player, player.playerScreenHandler);
        trackedHandlers.put(player.playerScreenHandler, true);

        var packet = new SyncStylesPayload(DataLoader.STYLE_LOADER.getItems());
        ServerPlayNetworking.send(player, packet);
    }

    private static void addListenerToHandler(ServerPlayerEntity player, ScreenHandler handler) {
        handler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler screenHandler, int slotId, ItemStack stack) {
                if (stack.contains(MyDataComponentTypes.XP) && stack.getCount() > 0) {
                    UUID playerId = player.getUuid();
                    Map<Integer, ItemStack> playerStacks = processedStacks.get(playerId);

                    if (playerStacks != null) {
                        ItemStack previousStack = playerStacks.get(slotId);

                        // Only process if it's a new item or stack count increased
                        boolean shouldProcess = previousStack == null ||
                                previousStack.isEmpty() ||
                                !ItemStack.areEqual(stack, previousStack) ||
                                stack.getCount() > previousStack.getCount();

                        if (shouldProcess) {
                            onItemWithComponentAdded(player, screenHandler, stack.copy(), slotId);
                            playerStacks.put(slotId, stack.copy());
                        }
                    }
                } else {
                    // Remove from tracking if slot is empty or no component
                    Map<Integer, ItemStack> playerStacks = processedStacks.get(player.getUuid());
                    if (playerStacks != null) {
                        playerStacks.remove(slotId);
                    }
                }
            }

            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
                // Not needed
            }
        });
    }

    private static void onItemWithComponentAdded(ServerPlayerEntity player, ScreenHandler handler, ItemStack stack, int slotId) {
        if (MyCommands.isPlayerLocked(player)) {
            return;
        }
        Integer data = stack.get(MyDataComponentTypes.XP);
        if (data == null) {
            return;
        }
        SkillsAPI.updateExperienceSources(
                player,
                ItemExperienceSource.class,
                experienceSource -> experienceSource.getValue(player, stack)
        );
        stack.remove(MyDataComponentTypes.XP);
        handler.setStackInSlot(slotId, handler.nextRevision(), stack);
    }
}