package de.one_piece_api.screen.manager;

import de.one_piece_api.util.ClientData;

/**
 * Coordinates updates across all screen components.
 * Provides a single source of truth for what needs updating.
 */
public class ScreenUpdateCoordinator {

    private final UpdateFlags flags = new UpdateFlags();

    public ScreenUpdateCoordinator() {
        // Listen to central invalidation events
        ClientData.DATA_INVALIDATION.addListener(this::onDataInvalidated);
    }

    /**
     * Central handler for all data invalidation events
     */
    private void onDataInvalidated(ClientData.DataInvalidationEvent event) {
        switch (event.type()) {
            case DEVIL_FRUIT_CONFIG -> {
                flags.devilFruitConfig = true;
                flags.tabs = true;
            }
            case CLASS_CONFIG -> {
                flags.classConfig = true;
                flags.viewport = true;
            }
            case CATEGORY_DATA -> {
                flags.categoryData = true;
                flags.viewport = true;
            }
            case ALL -> flags.setAll();
        }
    }

    /**
     * Check if any updates are pending
     */
    public boolean hasUpdates() {
        return flags.hasAny();
    }

    /**
     * Get current update flags
     */
    public UpdateFlags getFlags() {
        return flags;
    }

    /**
     * Clear a specific flag
     */
    public void clearFlag(UpdateFlag flag) {
        flags.clear(flag);
    }

    /**
     * Clear all flags
     */
    public void clearAll() {
        flags.clearAll();
    }

    /**
     * Update flag enum
     */
    public enum UpdateFlag {
        DEVIL_FRUIT_CONFIG,
        CLASS_CONFIG,
        CATEGORY_DATA,
        VIEWPORT,
        SPELLS,
        TABS
    }

    /**
     * Update flags container
     */
    public static class UpdateFlags {
        public boolean devilFruitConfig = false;
        public boolean classConfig = false;
        public boolean categoryData = false;
        public boolean viewport = false;
        public boolean tabs = false;

        public void setAll() {
            devilFruitConfig = true;
            classConfig = true;
            categoryData = true;
            viewport = true;
            tabs = true;
        }

        public boolean hasAny() {
            return devilFruitConfig || classConfig || categoryData ||
                    viewport || tabs;
        }

        public void clear(UpdateFlag flag) {
            switch (flag) {
                case DEVIL_FRUIT_CONFIG -> devilFruitConfig = false;
                case CLASS_CONFIG -> classConfig = false;
                case CATEGORY_DATA -> categoryData = false;
                case VIEWPORT -> viewport = false;
                case TABS -> tabs = false;
            }
        }

        public void clearAll() {
            devilFruitConfig = false;
            classConfig = false;
            categoryData = false;
            viewport = false;
            tabs = false;
        }
    }
}