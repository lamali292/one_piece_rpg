package de.one_piece_api.screen.manager;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.mixin_interface.IClassPlayer;
import de.one_piece_api.mixin_interface.IDevilFruitPlayer;
import de.one_piece_api.network.payload.ClassConfigPayload;
import de.one_piece_api.network.payload.DevilFruitPayload;
import de.one_piece_api.util.ClientData;
import de.one_piece_api.util.OnePieceCategory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import org.slf4j.Marker;

import java.lang.reflect.Field;
import java.util.Map;

public class ScreenDataManager {

    private final ClientPlayerEntity player;
    private final ClientSkillScreenData skillScreenData;

    private ClientCategoryData categoryData;
    private ClassConfig classConfig;

    // Cached player data
    private String cachedDevilFruit = null;
    private Identifier cachedClass = null;
    private Identifier cachedFruitId = null;

    // Update flags
    private boolean categoryDataDirty = false;

    public ScreenDataManager(ClientPlayerEntity player) {
        this.player = player;
        this.skillScreenData = loadSkillScreenData();
        this.categoryData = loadCategoryData();

        // Listen to data invalidation events
        ClientData.DATA_INVALIDATION.addListener(this::onDataInvalidated);

        // Listen for CLASS_CONFIG updates to load class when it arrives
        ClientData.CLASS_CONFIG.addListener(this::onClassConfigLoaded);

        // Force initial data load
        forceInitialDataLoad();
    }

    /**
     * Called when CLASS_CONFIG is loaded from the server
     */
    private void onClassConfigLoaded(Map<Identifier, ClassConfig> classConfigs) {
        OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "CLASS_CONFIG received with {} entries", classConfigs.size());

        // If we already cached the player's class, try to load it now
        if (cachedClass != null &&
                !cachedClass.toString().equals("minecraft:") &&
                !cachedClass.toString().equals("minecraft:empty")) {

            ClassConfig config = classConfigs.get(cachedClass);
            if (config != null) {
                this.classConfig = config;
                OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "Loaded class config for {}", cachedClass);

                // Trigger update to apply the new config
                ClientData.invalidate(ClientData.DataInvalidationType.CLASS_CONFIG);
            } else {
                OnePieceRPG.LOGGER.warn("Player class {} not found in CLASS_CONFIG", cachedClass);
            }
        }
    }

    /**
     * Forces initial data load from player data tracker and client config
     */
    private void forceInitialDataLoad() {
        if (!(player instanceof IDevilFruitPlayer devilFruitPlayer &&
                player instanceof IClassPlayer classPlayer)) {
            return;
        }

        // Force read current values from DataTracker
        String currentDevilFruit = devilFruitPlayer.onepiece$getDevilFruit();
        Identifier currentClass = classPlayer.onepiece$getOnePieceClass();

        OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "Initial data load - Devil Fruit: {}, Class: {}",
                currentDevilFruit, currentClass);

        // Process devil fruit
        if (currentDevilFruit != null && !currentDevilFruit.isEmpty()) {
            cachedDevilFruit = currentDevilFruit;
            Identifier fruitId = Identifier.of(currentDevilFruit);
            if (!fruitId.toString().equals("minecraft:")) {
                this.cachedFruitId = fruitId;
                ClientPlayNetworking.send(new DevilFruitPayload.Request(fruitId));
            }
        }

        // Process class
        if (currentClass != null &&
                !currentClass.toString().equals("minecraft:") &&
                !currentClass.toString().equals("minecraft:empty") &&
                !currentClass.getPath().isEmpty()) {

            cachedClass = currentClass;

            // Check if CLASS_CONFIG is already loaded
            ClientData.CLASS_CONFIG.get().ifPresentOrElse(
                    classConfigs -> {
                        if (!classConfigs.isEmpty()) {
                            ClassConfig config = classConfigs.get(currentClass);
                            if (config != null) {
                                this.classConfig = config;
                                OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "Loaded initial class config: {}", currentClass);
                            } else {
                                OnePieceRPG.LOGGER.warn("No class config found for: {}", currentClass);
                            }
                        } else {
                            // CLASS_CONFIG is empty, request it from server
                            OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "CLASS_CONFIG is empty, requesting from server...");
                            ClientPlayNetworking.send(new ClassConfigPayload.Request());
                        }
                    },
                    () -> {
                        // CLASS_CONFIG not loaded, request it from server
                        OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "CLASS_CONFIG not loaded, requesting from server...");
                        ClientPlayNetworking.send(new ClassConfigPayload.Request());
                    }
            );
        }
    }

    /**
     * Checks player data and updates if changed.
     * Should be called once per frame.
     */
    public void updatePlayerData() {
        if (!(player instanceof IDevilFruitPlayer devilFruitPlayer &&
                player instanceof IClassPlayer classPlayer)) {
            return;
        }

        String currentDevilFruit = devilFruitPlayer.onepiece$getDevilFruit();
        Identifier currentClass = classPlayer.onepiece$getOnePieceClass();

        // Check devil fruit change
        if (!currentDevilFruit.equals(cachedDevilFruit)) {
            handleDevilFruitChange(currentDevilFruit);
        }

        // Check class change
        if (currentClass != null && !currentClass.equals(cachedClass)) {
            handleClassChange(currentClass);
        }
    }

    private void handleDevilFruitChange(String newDevilFruit) {
        OnePieceRPG.debug(OnePieceRPG.UI_UPDATE, "Devil Fruit changed: {} -> {}",
                cachedDevilFruit, newDevilFruit);

        cachedDevilFruit = newDevilFruit;

        if (newDevilFruit == null || newDevilFruit.isEmpty()) {
            return;
        }

        Identifier newFruitId = Identifier.of(newDevilFruit);

        if (!newFruitId.equals(this.cachedFruitId)) {
            this.cachedFruitId = newFruitId;
            ClientPlayNetworking.send(new DevilFruitPayload.Request(newFruitId));
        }
    }

    private void handleClassChange(Identifier newClass) {
        OnePieceRPG.debug(OnePieceRPG.UI_UPDATE, "Class changed: {} -> {}",
                cachedClass, newClass);

        // Better validation
        if (newClass == null ||
                newClass.toString().equals("minecraft:") ||
                newClass.toString().equals("minecraft:empty") ||
                newClass.getPath().isEmpty()) {
            return;
        }

        cachedClass = newClass;

        ClientData.CLASS_CONFIG.get().ifPresentOrElse(
                classConfigs -> {
                    if (!classConfigs.isEmpty()) {
                        ClassConfig newConfig = classConfigs.get(newClass);
                        if (newConfig != null && newConfig != this.classConfig) {
                            this.classConfig = newConfig;
                            ClientData.invalidate(ClientData.DataInvalidationType.CLASS_CONFIG);
                        } else if (newConfig == null) {
                            OnePieceRPG.LOGGER.warn("Class config not found for: {}", newClass);
                        }
                    } else {
                        // Request class configs from server
                        OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "CLASS_CONFIG empty on class change, requesting from server...");
                        ClientPlayNetworking.send(new ClassConfigPayload.Request());
                    }
                },
                () -> {
                    // Request class configs from server
                    OnePieceRPG.debug(OnePieceRPG.LOADING_DATA, "CLASS_CONFIG not loaded on class change, requesting from server...");
                    ClientPlayNetworking.send(new ClassConfigPayload.Request());
                }
        );
    }

    private void onDataInvalidated(ClientData.DataInvalidationEvent event) {
        if (event.type() == ClientData.DataInvalidationType.CATEGORY_DATA) {
            categoryDataDirty = true;
        }
    }

    /**
     * Updates category data if marked dirty
     */
    public void updateCategoryDataIfNeeded() {
        if (categoryData == null) {
            categoryData = loadCategoryData();
            categoryDataDirty = true;
        }

        if (categoryDataDirty && categoryData != null) {
            categoryData.updateLastOpen();
            categoryData.updateUnseenPoints();
            categoryDataDirty = false;
        }
    }

    /**
     * Reloads category data while preserving view state
     */
    public void reloadCategoryData() {
        if (categoryData == null) {
            return;
        }

        ViewState viewState = ViewState.capture(categoryData);
        categoryData = loadCategoryData();

        if (categoryData != null) {
            viewState.applyTo(categoryData);
            ClientData.invalidate(ClientData.DataInvalidationType.CATEGORY_DATA);
        }
    }

    // Getters
    public ClientCategoryData getCategoryData() {
        return categoryData;
    }

    public ClassConfig getClassConfig() {
        return classConfig;
    }

    public boolean hasCategoryData() {
        return categoryData != null;
    }

    public boolean hasClassConfig() {
        return classConfig != null;
    }

    // Data loading
    private ClientCategoryData loadCategoryData() {
        return skillScreenData.getCategory(OnePieceCategory.ID).orElse(null);
    }

    private static ClientSkillScreenData loadSkillScreenData() {
        try {
            SkillsClientMod skillsClientMod = SkillsClientMod.getInstance();
            Field screenDataField = SkillsClientMod.class.getDeclaredField("screenData");
            screenDataField.setAccessible(true);

            Object data = screenDataField.get(skillsClientMod);
            if (data instanceof ClientSkillScreenData skillData) {
                return skillData;
            }

            OnePieceRPG.LOGGER.warn("Failed to cast screen data, using empty instance");
            return new ClientSkillScreenData();

        } catch (Exception e) {
            OnePieceRPG.LOGGER.error("Error loading skill screen data: {}", e.getMessage(), e);
            return new ClientSkillScreenData();
        }
    }

    /**
     * View state snapshot for preserving pan/zoom
     */
    private record ViewState(int x, int y, float scale) {
        static ViewState capture(ClientCategoryData categoryData) {
            return new ViewState(
                    categoryData.getX(),
                    categoryData.getY(),
                    categoryData.getScale()
            );
        }

        void applyTo(ClientCategoryData categoryData) {
            categoryData.setX(x);
            categoryData.setY(y);
            categoryData.setScale(scale);
        }
    }
}