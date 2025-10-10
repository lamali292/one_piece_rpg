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
import de.one_piece_api.data.loader.DataLoaders;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MyCommands {


    public static void register() {
        CommandRegistrationCallback.EVENT.register(MyCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> fruitBuilder =  CommandManager.literal("devilfruit")
                .requires(source -> source.hasPermissionLevel(2))
                .then( CommandManager.argument("fruit", StringArgumentType.string())
                        .suggests(MyCommands::suggestFruits)
                        .executes(MyCommands::giveDevilFruit)
                );
        dispatcher.register(fruitBuilder);


        LiteralArgumentBuilder<ServerCommandSource> xpBuilder =  CommandManager.literal("itemxp")
                .requires(source -> source.hasPermissionLevel(2))
                .then( CommandManager.argument("count", IntegerArgumentType.integer(0))
                        .executes(MyCommands::giveItemXP)
                ).then(CommandManager.literal("lock")
                        .executes(MyCommands::lockSelf)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(MyCommands::lockPlayer)
                        )
                ).then(CommandManager.literal("unlock")
                        .executes(MyCommands::unlockSelf)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(MyCommands::unlockPlayer)
                        ).then(CommandManager.literal("all")
                                .executes(MyCommands::unlockAll)
                        )
                );
        dispatcher.register(xpBuilder);
    }

    private static final Set<ServerPlayerEntity> xpLockedPlayers = new HashSet<>();

    public static boolean isPlayerLocked(ServerPlayerEntity player) {
        return xpLockedPlayers.contains(player);
    }

    private static int lockSelf(CommandContext<ServerCommandSource> context) {
        try {
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity player = source.getPlayerOrThrow();

            xpLockedPlayers.add(player);

            source.sendFeedback(() -> Text.literal("Locked your XP"), false);
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }

    private static int unlockSelf(CommandContext<ServerCommandSource> context) {
        try {
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity player = source.getPlayerOrThrow();
            xpLockedPlayers.remove(player);
            source.sendFeedback(() -> Text.literal("Unlocked your XP"), false);
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }

    private static int lockPlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            ServerCommandSource source = context.getSource();
            xpLockedPlayers.add(player);
            source.sendFeedback(() -> Text.literal("Locked XP for " + player.getName().getString()), true);
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }

    private static int unlockPlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            ServerCommandSource source = context.getSource();
            xpLockedPlayers.remove(player);
            source.sendFeedback(() -> Text.literal("Unlocked XP for " + player.getName().getString()), true);
            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }

    private static int unlockAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        int count = xpLockedPlayers.size();
        xpLockedPlayers.clear();
        source.sendFeedback(() -> Text.literal("Unlocked XP for " + count + " players"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int giveItemXP(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("No Player!"));
            return 0;
        }
        var itemStack = player.getMainHandStack();
        if (itemStack.isEmpty()) {
            source.sendError(Text.literal("No Item in Hand!"));
            return 0;
        }
        itemStack.set(MyDataComponentTypes.XP, context.getArgument("count", Integer.class));
        return 1;
    }

    private static int giveDevilFruit(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String fruitId = StringArgumentType.getString(context, "fruit");

        Identifier fruitIdentifier = Identifier.tryParse(fruitId);
        if (fruitIdentifier == null || !DataLoaders.DEVIL_FRUIT_LOADER.getItems().containsKey(fruitIdentifier)) {
            source.sendError(Text.literal("Invalid devil fruit ID!"));
            return 0;
        }

        // Get the Devil Fruit item from your registry
        Item item = MyItems.DEVIL_FRUIT.item();
        ItemStack stack = new ItemStack(item);
        stack.set(MyDataComponentTypes.DEVIL_FRUIT, fruitIdentifier);

        // Give to player
        source.getPlayer().getInventory().insertStack(stack);
        source.sendFeedback(()->Text.literal("Given devil fruit: " + fruitIdentifier), false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestFruits(CommandContext<ServerCommandSource> serverCommandSourceCommandContext, SuggestionsBuilder builder) {
        Set<Identifier> fruits = DataLoaders.DEVIL_FRUIT_LOADER.getItems().keySet();
        for (Identifier id : fruits) {
            builder.suggest("\""+id.toString()+"\"");
        }
        return builder.buildFuture();
    }
}
