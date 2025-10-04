package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.gui.ClassScreen;
import de.one_piece_api.interfaces.IOnePiecePlayer;
import de.one_piece_api.network.SetCombatModePayload;
import de.one_piece_api.gui.OnePieceScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.function.Consumer;

public class MyKeys {

    public static final String OPEN_SKILL_TREE_KEY = "open_skill_tree";
    public static final String TOGGLE_COMBAT_MODE = "combat_mode";
    public static final String OPEN_CLASS = "class";


    private static final HashMap<String, KeyBinding> keyBindings = new HashMap<>();
    private static final HashMap<String, Consumer<MinecraftClient>> actions = new HashMap<>();

    public static void register() {
        registerHotkeys(GLFW.GLFW_KEY_G, TOGGLE_COMBAT_MODE, MyKeys::toggleCombatMode);
        registerHotkeys(GLFW.GLFW_KEY_H, OPEN_SKILL_TREE_KEY, client -> OnePieceScreen.getInstance().open(client));
        registerHotkeys(GLFW.GLFW_KEY_J, OPEN_CLASS, client -> ClassScreen.getInstance().open(client));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            keyBindings.forEach((id, key) -> {
                while (key.wasPressed()) {
                    actions.get(id).accept(client);
                }
            });
        });
    }

    public static KeyBinding getKeyBinding(String id) {
        return keyBindings.get(id);
    }

    public static void toggleCombatMode(MinecraftClient client) {
        if (client.player instanceof IOnePiecePlayer player) {
            boolean newState = !player.onepiece$isCombatMode(); // toggle locally
            player.onepiece$setCombatMode(newState);
            client.player.sendMessage(
                    net.minecraft.text.Text.of("Combat Mode: " + (newState ? "ON" : "OFF")),
                    true
            );
            ClientPlayNetworking.send(new SetCombatModePayload(newState));
        }
    }




    public static void registerHotkeys(int code, String id, Consumer<MinecraftClient> action) {
        KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key."+ OnePieceRPG.MOD_ID +"." + id,
                InputUtil.Type.KEYSYM,
                code,
                "category."+ OnePieceRPG.MOD_ID+".controls"
        ));
        keyBindings.put(id, key);
        actions.put(id, action);

    }
}
