package de.one_piece_api.event;

import de.one_piece_api.ClassRewardHandler;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.data.experience.ItemExperienceSource;
import de.one_piece_api.data.experience.TimeExperienceSource;
import de.one_piece_api.data.loader.CategoryLoader;
import de.one_piece_api.data.loader.DataLoaders;
import de.one_piece_api.init.MyAttributes;
import de.one_piece_api.init.MyCommands;
import de.one_piece_api.init.MyDataComponentTypes;
import de.one_piece_api.mixin_interface.IClassPlayer;
import de.one_piece_api.mixin_interface.IDevilFruitPlayer;
import de.one_piece_api.mixin_interface.IStaminaPlayer;
import de.one_piece_api.network.payload.DevilFruitPayload;
import de.one_piece_api.network.payload.SyncStylesPayload;
import de.one_piece_api.util.OnePieceCategory;
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
import net.minecraft.util.Identifier;
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
        ServerPlayConnectionEvents.DISCONNECT.register(ServerEvents::onPlayerDisconnect);

        ServerLifecycleEvents.SERVER_STARTING.register(ServerEvents::onServerStarted);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(ServerEvents::onEndDataPackReload);

        EventRegistry.DEVIL_FRUIT_EATEN.register(ServerEvents::onDevilFruitEaten);
        EventRegistry.LEVEL_UP.register(ServerEvents::onLevelUp);
        EventRegistry.CLASS_UPDATE.register(ClassRewardHandler::onClassUpdate);
    }


    /**
     * Checks for new class rewards when a player levels up.
     */
    public static void onLevelUp(ServerPlayerEntity player, Identifier categoryId,
                                 int oldLevel, int newLevel) {
        // Load attribute scaling configuration and update player attributes
        DataLoaders.ATTRIBUTE_SCALING.getData().ifPresentOrElse(attributeScalingConfig -> {
            // Update all stamina-related attributes based on the new level
            var maxStaminaAttr = player.getAttributeInstance(MyAttributes.MAX_STAMINA);
            var baseRegenAttr = player.getAttributeInstance(MyAttributes.STAMINA_BASE_REGEN);
            var crouchMultAttr = player.getAttributeInstance(MyAttributes.STAMINA_CROUCH_MULT);
            var crouchAddAttr = player.getAttributeInstance(MyAttributes.STAMINA_CROUCH_ADD);
            if (maxStaminaAttr != null) {
                maxStaminaAttr.setBaseValue(attributeScalingConfig.evaluateMaxStamina(newLevel));
            }
            if (baseRegenAttr != null) {
                baseRegenAttr.setBaseValue(attributeScalingConfig.evaluateStaminaBaseRegen(newLevel));
            }
            if (crouchMultAttr != null) {
                crouchMultAttr.setBaseValue(attributeScalingConfig.evaluateStaminaCrouchMultiplier(newLevel));
            }
            if (crouchAddAttr != null) {
                crouchAddAttr.setBaseValue(attributeScalingConfig.evaluateStaminaCrouchAdditive(newLevel));
            }
            OnePieceRPG.LOGGER.debug("Updated attributes for player {} at level {}",
                    player.getName().getString(), newLevel);
        }, () -> {
            OnePieceRPG.LOGGER.warn("Attribute scaling configuration not loaded for player {}",
                    player.getName().getString());
        });
        if (categoryId.equals(OnePieceCategory.ID)) {
            ClassRewardHandler.refreshRewards(player);
        }

    }


    private static void onDevilFruitEaten(ServerPlayerEntity serverPlayerEntity, Identifier identifier) {
        if (serverPlayerEntity instanceof IDevilFruitPlayer player) {
            player.onepiece$setDevilFruit(identifier.toString());
        }
        var config = DataLoaders.DEVIL_FRUIT_LOADER.getItems().get(identifier);
        if (config == null) {
            config = DevilFruitConfig.DEFAULT;
        }
        ServerPlayNetworking.send(serverPlayerEntity, new DevilFruitPayload(identifier, config));
    }

    private static void onServerStarted(MinecraftServer minecraftServer) {
        reloadCategoryData();
    }

    private static void onEndDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        if (success) {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                var packet = new SyncStylesPayload(DataLoaders.STYLE_LOADER.getItems());
                ServerPlayNetworking.send(player, packet);
            });

            reloadCategoryData();
        }
    }

    public static void reloadCategoryData() {
        CategoryConfig categoryConfig = CategoryLoader.buildCategory(
                DataLoaders.CONNECTIONS_LOADER.getItems(),
                DataLoaders.SKILL_DEFINITION_LOADER.getItems(),
                DataLoaders.DEVIL_FRUIT_LOADER.getItems(),
                DataLoaders.SKILL_LOADER.getItems()
        );
        CategoryLoader.addCategory(categoryConfig.id(), categoryConfig);
    }

    private static void onServerTick(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(ServerEvents::onServerPlayerTick);
    }

    private static final Map<UUID, Integer> playerTickCounters = new HashMap<>();
    private static final int TICKS_PER_HOUR = 20 * 60 * 60; // 20 ticks/sec * 60 sec * 60 min

    private static void onServerPlayerTick(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        int currentTicks = playerTickCounters.getOrDefault(playerId, 0) + 1;

        if (player instanceof IStaminaPlayer staminaPlayer) {
            staminaPlayer.onepiece$updateStamina();
        }

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

        var packet = new SyncStylesPayload(DataLoaders.STYLE_LOADER.getItems());
        ServerPlayNetworking.send(player, packet);

        ClassRewardHandler.refreshRewards(player);
    }

    private static void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        UUID playerId = handler.getPlayer().getUuid();
        processedStacks.remove(playerId);

        ClassRewardHandler.clearRewards(handler.getPlayer());
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