package de.one_piece_api.init;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.config.XpTimeConfig;
import de.one_piece_api.data.loader.DataLoaders;
import de.one_piece_api.item.DevilFruitItem;
import de.one_piece_api.mixin_interface.IDevilFruitPlayer;
import de.one_piece_api.mixin_interface.IXpPlayer;
import de.one_piece_api.network.payload.DevilFruitPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class MyCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(MyCommands::registerCommands);
    }

    // ==================== GENERIC HELPER ====================

    /**
     * Gets players from context - either from selector or defaults to command source
     */
    private static Collection<ServerPlayerEntity> getPlayers(CommandContext<ServerCommandSource> context) {
        try {
            return EntityArgumentType.getPlayers(context, "players");
        } catch (IllegalArgumentException e) {
            try {
                return Collections.singleton(context.getSource().getPlayerOrThrow());
            } catch (CommandSyntaxException ex) {
                return Collections.emptyList();
            }
        } catch (CommandSyntaxException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Executes an action on players (self or selected)
     */
    private static int executeOnPlayers(CommandContext<ServerCommandSource> context,
                                        BiFunction<ServerCommandSource, ServerPlayerEntity, Integer> action) {
        Collection<ServerPlayerEntity> players = getPlayers(context);
        ServerCommandSource source = context.getSource();

        if (players.isEmpty()) {
            source.sendError(Text.literal("No player found!"));
            return 0;
        }

        int successCount = 0;
        for (ServerPlayerEntity player : players) {
            if (action.apply(source, player) == Command.SINGLE_SUCCESS) {
                successCount++;
            }
        }

        return successCount > 0 ? Command.SINGLE_SUCCESS : 0;
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {

        // ==================== DEVIL FRUIT COMMANDS ====================

        LiteralArgumentBuilder<ServerCommandSource> fruitBuilder = CommandManager.literal("devilfruit")
                .requires(source -> source.hasPermissionLevel(2))
                // /devilfruit give <fruit> [@players]
                .then(CommandManager.literal("give")
                        .then(CommandManager.argument("fruit", StringArgumentType.string())
                                .suggests(MyCommands::suggestFruits)
                                .executes(MyCommands::giveDevilFruit)
                                .then(CommandManager.argument("players", EntityArgumentType.players())
                                        .executes(MyCommands::giveDevilFruit)
                                )
                        )
                )
                // /devilfruit allow_second [@players]
                .then(CommandManager.literal("allow_second")
                        .executes(MyCommands::allowSecondFruit)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::allowSecondFruit)
                        )
                )
                // /devilfruit disallow_second [@players]
                .then(CommandManager.literal("disallow_second")
                        .executes(MyCommands::disallowSecondFruit)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::disallowSecondFruit)
                        )
                )
                // /devilfruit query [@players]
                .then(CommandManager.literal("query")
                        .executes(MyCommands::queryDevilFruit)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::queryDevilFruit)
                        )
                )
                // /devilfruit clear [@players]
                .then(CommandManager.literal("clear")
                        .executes(MyCommands::clearDevilFruit)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::clearDevilFruit)
                        )
                );
        dispatcher.register(fruitBuilder);

        // ==================== XP TIME COMMAND ====================

        LiteralArgumentBuilder<ServerCommandSource> xpTimeBuilder = CommandManager.literal("xp_time")
                .requires(source -> source.hasPermissionLevel(2))
                // /xp_time set <amount> <minutes> [@players]
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                            .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                    .executes(MyCommands::setPlayerXpTime)
                                    .then(CommandManager.argument("players", EntityArgumentType.players())
                                            .executes(MyCommands::setPlayerXpTime)
                                    )
                            )
                    )
                )
                // /xp_time query [@players]
                .then(CommandManager.literal("query")
                        .executes(MyCommands::queryPlayerXpTime)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::queryPlayerXpTime)
                        )
                )
                // /xp_time reset [@players]
                .then(CommandManager.literal("reset")
                        .executes(MyCommands::resetPlayerXpTime)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::resetPlayerXpTime)
                        )
                );
        dispatcher.register(xpTimeBuilder);

        // ==================== ITEM XP COMMANDS ====================

        LiteralArgumentBuilder<ServerCommandSource> itemXpBuilder = CommandManager.literal("itemxp")
                .requires(source -> source.hasPermissionLevel(2))
                // /itemxp <count> - add XP to held item (self only)
                .then(CommandManager.literal("count")
                        .then(CommandManager.argument("count", IntegerArgumentType.integer(0))
                            .executes(MyCommands::giveItemXP)
                ))
                // /itemxp lock [@players]
                .then(CommandManager.literal("lock")
                        .executes(MyCommands::lockPlayer)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::lockPlayer)
                        )
                )
                // /itemxp unlock [@players]
                .then(CommandManager.literal("unlock")
                        .executes(MyCommands::unlockPlayer)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::unlockPlayer)
                        )
                );
        dispatcher.register(itemXpBuilder);

        // ==================== XP & SP MANAGEMENT ====================

        LiteralArgumentBuilder<ServerCommandSource> onepieceBuilder = CommandManager.literal("onepiece")
                .requires(source -> source.hasPermissionLevel(2))
                // /onepiece xp add <amount> [@players]
                .then(CommandManager.literal("xp")
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(MyCommands::addPlayerXp)
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .executes(MyCommands::addPlayerXp)
                                        )
                                )
                        )
                        // /onepiece xp remove <amount> [@players]
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(MyCommands::removePlayerXp)
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .executes(MyCommands::removePlayerXp)
                                        )
                                )
                        )
                        // /onepiece xp set <amount> [@players]
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(MyCommands::setPlayerXp)
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .executes(MyCommands::setPlayerXp)
                                        )
                                )
                        )
                )
                // /onepiece points add <amount> [@players]
                .then(CommandManager.literal("points")
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(MyCommands::addPlayerSp)
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .executes(MyCommands::addPlayerSp)
                                        )
                                )
                        )
                        // /onepiece points remove <amount> [@players]
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(MyCommands::removePlayerSp)
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .executes(MyCommands::removePlayerSp)
                                        )
                                )
                        )
                        // /onepiece points set <amount> [@players]
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(MyCommands::setPlayerSp)
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .executes(MyCommands::setPlayerSp)
                                        )
                                )
                        )
                )
                // /onepiece query [@players]
                .then(CommandManager.literal("query")
                        .executes(MyCommands::queryPlayerXp)
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(MyCommands::queryPlayerXp)
                        )
                );
        dispatcher.register(onepieceBuilder);
    }

    // ==================== DEVIL FRUIT IMPLEMENTATIONS ====================

    private static final Set<ServerPlayerEntity> allowedSecondFruit = new HashSet<>();

    public static boolean canEatSecondFruit(ServerPlayerEntity player) {
        return allowedSecondFruit.contains(player);
    }

    private static int giveDevilFruit(CommandContext<ServerCommandSource> context) {
        String fruitId = StringArgumentType.getString(context, "fruit");
        Identifier fruitIdentifier = Identifier.tryParse(fruitId);

        if (fruitIdentifier == null || !DataLoaders.DEVIL_FRUIT_LOADER.getItems().containsKey(fruitIdentifier)) {
            context.getSource().sendError(Text.literal("Invalid devil fruit ID!"));
            return 0;
        }

        return executeOnPlayers(context, (source, player) -> {
            Item item = MyItems.DEVIL_FRUIT.item();
            ItemStack stack = new ItemStack(item);
            stack.set(MyDataComponentTypes.DEVIL_FRUIT, fruitIdentifier);
            player.getInventory().insertStack(stack);
            return Command.SINGLE_SUCCESS;
        });
    }

    private static int allowSecondFruit(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            allowedSecondFruit.add(player);
            player.sendMessage(Text.literal("§6[Special Permission] §aYou can now eat a second Devil Fruit!"), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    private static int disallowSecondFruit(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            allowedSecondFruit.remove(player);
            return Command.SINGLE_SUCCESS;
        });
    }

    private static int queryDevilFruit(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IDevilFruitPlayer dfPlayer) {
                String currentFruit = dfPlayer.onepiece$getDevilFruit();
                boolean canEatSecond = canEatSecondFruit(player);

                String fruitStatus = currentFruit != null && !currentFruit.isEmpty() ? "§e" + currentFruit : "§7None";
                String secondFruitStatus = canEatSecond ? "§aYes" : "§cNo";

                source.sendFeedback(() -> Text.literal(
                        player.getName().getString() + "'s Devil Fruit Status:\n" +
                                "  Current Fruit: " + fruitStatus + "\n" +
                                "  Can Eat Second: " + secondFruitStatus
                ), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int clearDevilFruit(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IDevilFruitPlayer dfPlayer) {
                dfPlayer.onepiece$setDevilFruit(DevilFruitItem.DEFAULT_DEVIL_FRUIT.toString());
                ServerPlayNetworking.send(player, new DevilFruitPayload(DevilFruitItem.DEFAULT_DEVIL_FRUIT, DevilFruitConfig.DEFAULT));
                allowedSecondFruit.remove(player);
                player.sendMessage(Text.literal("§cYour Devil Fruit powers have been removed!"), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    // ==================== XP TIME IMPLEMENTATIONS ====================

    private static int setPlayerXpTime(CommandContext<ServerCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        int minutes = IntegerArgumentType.getInteger(context, "minutes");

        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                XpTimeConfig config = new XpTimeConfig(amount, minutes);
                xpPlayer.onepiece$setXpTimeConfig(config);
                xpPlayer.onepiece$resetXpTimer();
                player.sendMessage(Text.literal("§6[XP Rate] §aYour XP gain rate has been set to: §e" + config.toString()), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int queryPlayerXpTime(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                XpTimeConfig config = xpPlayer.onepiece$getXpTimeConfig();
                int ticksRemaining = config.getIntervalTicks() - xpPlayer.onepiece$getTicksSinceLastXp();
                int minutesRemaining = ticksRemaining / (60 * 20);
                int secondsRemaining = (ticksRemaining / 20) % 60;

                source.sendFeedback(() -> Text.literal(
                        player.getName().getString() + "'s XP config: §e" + config.toString() + "\n" +
                                "§7Next gain in: §e" + minutesRemaining + "m " + secondsRemaining + "s"
                ), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int resetPlayerXpTime(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                xpPlayer.onepiece$setXpTimeConfig(XpTimeConfig.DEFAULT);
                xpPlayer.onepiece$resetXpTimer();
                player.sendMessage(Text.literal("§aYour XP time has been reset to default: §e" + XpTimeConfig.DEFAULT.toString()), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    // ==================== ITEM XP LOCK SYSTEM ====================

    private static final Set<ServerPlayerEntity> xpLockedPlayers = new HashSet<>();

    public static boolean isPlayerLocked(ServerPlayerEntity player) {
        return xpLockedPlayers.contains(player);
    }

    private static int lockPlayer(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            xpLockedPlayers.add(player);
            source.sendFeedback(() -> Text.literal("§aLocked Item XP for §e" + player.getName()), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    private static int unlockPlayer(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            xpLockedPlayers.remove(player);
            source.sendFeedback(() -> Text.literal("§aUnlocked Item XP for §e" + player.getName()), false);
            return Command.SINGLE_SUCCESS;
        });
    }


    private static int giveItemXP(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            ItemStack itemStack = player.getMainHandStack();

            if (itemStack.isEmpty()) {
                source.sendError(Text.literal("No Item in Hand!"));
                return 0;
            }

            int xpAmount = IntegerArgumentType.getInteger(context, "count");
            itemStack.set(MyDataComponentTypes.XP, xpAmount);
            source.sendFeedback(() -> Text.literal("§aAdded §e" + xpAmount + " XP §ato held item"), false);
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }

    // ==================== XP MANAGEMENT ====================

    private static int addPlayerXp(CommandContext<ServerCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");

        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                int oldSp = xpPlayer.onepiece$getTotalSkillPoints();
                xpPlayer.onepiece$addXp(amount);
                int newXp = xpPlayer.onepiece$getXp();
                int newSp = xpPlayer.onepiece$getTotalSkillPoints();
                int spGained = newSp - oldSp;

                if (spGained > 0) {
                    player.sendMessage(Text.literal("§6✦ §aYou gained §6" + spGained + " Skill Point(s)§a!"), false);
                }
                player.sendMessage(Text.literal("§a+§e" + amount + " XP §7(Total: §e" + newXp + "§7)"), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int removePlayerXp(CommandContext<ServerCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");

        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                int oldXp = xpPlayer.onepiece$getXp();
                int newXp = Math.max(0, oldXp - amount);
                int actualRemoved = oldXp - newXp;

                xpPlayer.onepiece$setXp(newXp);
                player.sendMessage(Text.literal("§c-§e" + actualRemoved + " XP §7(Total: §e" + newXp + "§7)"), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int setPlayerXp(CommandContext<ServerCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");

        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                xpPlayer.onepiece$setXp(amount);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int queryPlayerXp(CommandContext<ServerCommandSource> context) {
        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                int xp = xpPlayer.onepiece$getXp();
                Map<Identifier, Integer> sp = xpPlayer.onepiece$getSkillPointsWithSources();
                boolean isAfk = xpPlayer.onepiece$isAfk();

                // Calculate total SP
                int totalSp = sp.values().stream().mapToInt(Integer::intValue).sum();

                // Build sources breakdown
                StringBuilder sourcesBreakdown = new StringBuilder();
                if (!sp.isEmpty()) {
                    sourcesBreakdown.append("\n  §7Sources:");
                    sp.forEach((id, amount) -> {
                        sourcesBreakdown.append("\n    §6")
                                .append(id.toString())
                                .append(": §f")
                                .append(amount);
                    });
                }

                String finalBreakdown = sourcesBreakdown.toString();
                source.sendFeedback(() -> Text.literal(
                        player.getName().getString() + "'s Stats:\n" +
                                "  §eXP: §f" + xp +
                                "\n  §6SP Available: §f" + totalSp +
                                finalBreakdown +
                                (isAfk ? "\n  §cStatus: AFK" : "")
                ), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    // ==================== SP MANAGEMENT ====================

    private static int addPlayerSp(CommandContext<ServerCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");

        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                xpPlayer.onepiece$addSkillPoints(amount, OnePieceRPG.id("commands"));
                player.sendMessage(Text.literal("§6✦ §aYou received §6" + amount + " Skill Point(s)§a!"), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int removePlayerSp(CommandContext<ServerCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");

        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                int oldSp = xpPlayer.onepiece$getSkillPoints(OnePieceRPG.id("commands"));
                int newSp = Math.max(0, oldSp - amount);
                int actualRemoved = oldSp - newSp;

                xpPlayer.onepiece$setSkillPoints(newSp, OnePieceRPG.id("commands"));
                player.sendMessage(Text.literal("§c-§6" + actualRemoved + " SP §7(Total: §6" + newSp + "§7)"), false);
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    private static int setPlayerSp(CommandContext<ServerCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");

        return executeOnPlayers(context, (source, player) -> {
            if (player instanceof IXpPlayer xpPlayer) {
                xpPlayer.onepiece$setSkillPoints(amount, OnePieceRPG.id("commands"));
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });
    }

    // ==================== UTILITY ====================

    private static CompletableFuture<Suggestions> suggestFruits(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        Set<Identifier> fruits = DataLoaders.DEVIL_FRUIT_LOADER.getItems().keySet();
        for (Identifier id : fruits) {
            builder.suggest("\"" + id.toString() + "\"");
        }
        return builder.buildFuture();
    }
}