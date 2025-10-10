package de.one_piece_api.init;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.screen.ClassScreen;
import de.one_piece_api.mixin_interface.ICombatPlayer;
import de.one_piece_api.network.payload.SetCombatModePayload;
import de.one_piece_api.screen.OnePieceScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Registry for custom keybindings and their associated actions.
 * <p>
 * This class manages all custom keyboard controls for the One Piece RPG mod,
 * including skill tree navigation, combat mode toggling, and class screen access.
 * It handles keybinding registration, action mapping, and tick-based input processing.
 *
 * @see KeyBinding
 * @see ClientTickEvents
 */
public class MyKeys {

    /** Identifier for the skill tree opening keybinding */
    public static final String OPEN_SKILL_TREE_KEY = "open_skill_tree";

    /** Identifier for the combat mode toggle keybinding */
    public static final String TOGGLE_COMBAT_MODE = "combat_mode";

    /** Identifier for the class screen opening keybinding */
    public static final String OPEN_CLASS = "class";

    /**
     * Internal storage mapping keybinding identifiers to their {@link KeyBinding} instances.
     */
    private static final HashMap<String, KeyBinding> keyBindings = new HashMap<>();

    /**
     * Internal storage mapping keybinding identifiers to their action consumers.
     */
    private static final HashMap<String, Consumer<MinecraftClient>> actions = new HashMap<>();

    /**
     * Registers all custom keybindings and sets up their tick handlers.
     * <p>
     * This method should be called during client initialization. It registers:
     * <ul>
     *     <li>G key - Toggle combat mode</li>
     *     <li>H key - Open skill tree screen</li>
     *     <li>J key - Open class screen</li>
     * </ul>
     * It also sets up a client tick event listener to process key presses.
     */
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

    /**
     * Retrieves a keybinding by its identifier.
     *
     * @param id the identifier of the keybinding to retrieve
     * @return the {@link KeyBinding} associated with the identifier, or {@code null} if not found
     */
    public static KeyBinding getKeyBinding(String id) {
        return keyBindings.get(id);
    }

    /**
     * Toggles the player's combat mode on and off.
     * <p>
     * This method switches the player's combat mode state locally, displays an
     * action bar message indicating the new state, and sends the state change
     * to the server for synchronization.
     *
     * @param client the Minecraft client instance
     */
    public static void toggleCombatMode(MinecraftClient client) {
        if (client.player instanceof ICombatPlayer player) {
            boolean newState = !player.onepiece$isCombatMode();
            player.onepiece$setCombatMode(newState);
            client.player.sendMessage(
                    net.minecraft.text.Text.of("Combat Mode: " + (newState ? "ON" : "OFF")),
                    true
            );
            ClientPlayNetworking.send(new SetCombatModePayload(newState));
        }
    }

    /**
     * Registers a new keybinding with an associated action.
     * <p>
     * This method creates a new keybinding with the specified key code and identifier,
     * then maps it to the provided action consumer. The keybinding will appear in the
     * game's controls menu under the mod's category.
     *
     * @param code the GLFW key code for the keybinding
     * @param id the unique identifier for this keybinding
     * @param action the action to execute when the key is pressed
     */
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