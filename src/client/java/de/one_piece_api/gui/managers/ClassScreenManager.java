package de.one_piece_api.gui.managers;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.gui.ClassScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

/**
 * Manages the lifecycle and state of the ClassScreen.
 * Provides a centralized point for external systems to interact with the tab
 * without tight coupling to tab instances.
 */
public class ClassScreenManager {

    private static ClassScreenManager instance;

    private ClassScreen currentScreen;
    private Map<Identifier, ClassConfig> cachedClassConfigs;

    private ClassScreenManager() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance of the tab manager.
     */
    public static ClassScreenManager getInstance() {
        if (instance == null) {
            instance = new ClassScreenManager();
        }
        return instance;
    }

    /**
     * Opens the class selection tab for the given player.
     * If a tab is already open, it will be replaced.
     */
    public void openScreen(ClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClassScreen screen = new ClassScreen(player);

        // If we have cached configs, apply them immediately
        if (cachedClassConfigs != null) {
            screen.updateAvailableClasses(cachedClassConfigs);
        }

        this.currentScreen = screen;
        client.setScreen(screen);
    }

    /**
     * Updates the available class configurations.
     * If a ClassScreen is currently open, it will be updated immediately.
     * Otherwise, the configurations are cached for the next tab opening.
     */
    public void updateClassConfigurations(Map<Identifier, ClassConfig> classConfigs) {
        this.cachedClassConfigs = classConfigs;

        if (currentScreen != null && isScreenActive()) {
            currentScreen.updateAvailableClasses(classConfigs);
        }
    }

    /**
     * Gets the currently active ClassScreen, if any.
     */
    public Optional<ClassScreen> getCurrentScreen() {
        if (currentScreen != null && isScreenActive()) {
            return Optional.of(currentScreen);
        }
        return Optional.empty();
    }

    /**
     * Notifies the manager that a tab has been closed.
     * Should be called from ClassScreen's removed() method.
     */
    void onScreenClosed(ClassScreen screen) {
        if (this.currentScreen == screen) {
            this.currentScreen = null;
        }
    }

    /**
     * Checks if the current tab is still the active tab in Minecraft.
     */
    private boolean isScreenActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.currentScreen == currentScreen;
    }

    /**
     * Clears all cached data. Useful for cleanup or resource management.
     */
    public void clear() {
        this.currentScreen = null;
        this.cachedClassConfigs = null;
    }
}