package de.one_piece_api.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.registries.MyFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for creating tabbed GUI screens with consistent navigation and rendering.
 * <p>
 * This abstract screen provides a complete tabbed interface implementation with:
 * <ul>
 *     <li>Tab management (add, switch, get current)</li>
 *     <li>Mouse click and keyboard navigation</li>
 *     <li>Hover and selection visual feedback</li>
 *     <li>Sound effects for interactions</li>
 *     <li>Automatic input delegation to active tab</li>
 *     <li>Responsive layout calculation</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * public class MyScreen extends GenericTabbedScreen {
 *     public MyScreen(ClientPlayerEntity player) {
 *         super(Text.literal("My Screen"), player);
 *         addTab(Text.literal("Tab 1"), new MyTab1(), 100);
 *         addTab(Text.literal("Tab 2"), new MyTab2(), 100);
 *     }
 * }
 * }</pre>
 *
 * @see Tab
 * @see Screen
 */
public abstract class GenericTabbedScreen extends Screen {

    // ==================== Constants ====================

    /** Horizontal spacing between tabs in pixels */
    private static final int TAB_MARGIN = 20;

    /** Height of tab buttons in pixels */
    private static final int TAB_HEIGHT_GETTER = getTabHeight();

    /** Text color for the selected tab (white) */
    private static final int COLOR_SELECTED = 0xFFFFFFFF;

    /** Text color for hovered tabs (light yellow) */
    private static final int COLOR_HOVERED = 0xFFFFFFA0;

    /** Text color for normal tabs (white) */
    private static final int COLOR_NORMAL = 0xFFFFFFFF;

    /** Sound pitch for tab switching */
    private static final float SOUND_PITCH_TAB_SWITCH = 1.0F;

    /** Sound volume for tab clicks */
    private static final float SOUND_VOLUME_TAB_CLICK = 0.8F;

    /** Sound volume for arrow key navigation */
    private static final float SOUND_VOLUME_TAB_ARROW = 0.9F;

    // Key codes for navigation
    /** GLFW key code for right arrow key */
    private static final int KEY_RIGHT_ARROW = 262;

    /** GLFW key code for left arrow key */
    private static final int KEY_LEFT_ARROW = 263;

    // ==================== Fields ====================

    /** The client player viewing this screen */
    protected final ClientPlayerEntity player;

    /** List of registered tabs */
    protected final List<TabData> tabs = new ArrayList<>();

    /** Index of the currently selected tab */
    protected int currentTab = 0;

    /** Counter incremented each tick for animations */
    protected int tickCounter = 0;

    // ==================== Constructor ====================

    /**
     * Creates a new tabbed screen.
     *
     * @param title the screen title
     * @param player the client player entity
     * @throws NullPointerException if player is null
     */
    public GenericTabbedScreen(Text title, ClientPlayerEntity player) {
        super(title);
        this.player = Objects.requireNonNull(player, "Player cannot be null");
    }

    // ==================== Tab Management ====================

    /**
     * Adds a new tab to the screen.
     * <p>
     * Tabs are displayed in the order they are added. The first tab added
     * becomes the initially selected tab.
     *
     * @param name the display name for the tab
     * @param tab the tab content to render when selected
     * @param tabWidth the width of the tab button in pixels
     * @throws NullPointerException if name or tab is null
     * @throws IllegalArgumentException if tabWidth is not positive
     */
    public void addTab(MutableText name, Tab tab, int tabWidth) {
        Objects.requireNonNull(name, "Tab name cannot be null");
        Objects.requireNonNull(tab, "Tab content cannot be null");

        if (tabWidth <= 0) {
            throw new IllegalArgumentException("Tab width must be positive");
        }

        tabs.add(new TabData(name, tab, tabWidth));
    }

    /**
     * Gets the currently active tab.
     *
     * @return the current tab, or {@code null} if no tabs exist
     */
    public Tab getCurrentTab() {
        if (isValidTabIndex(currentTab)) {
            return tabs.get(currentTab).tab();
        }
        return null;
    }

    /**
     * Gets an immutable view of all tabs.
     *
     * @return unmodifiable list of tab data
     */
    public List<TabData> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

    /**
     * Gets the index of the currently selected tab.
     *
     * @return the current tab index (0-based)
     */
    public int getCurrentTabIndex() {
        return currentTab;
    }

    /**
     * Switches to the specified tab index if valid.
     * <p>
     * If the index is the same as the current tab, no action is taken.
     * Calls {@link #onTabChanged} when the tab successfully changes.
     *
     * @param index the tab index to switch to (0-based)
     */
    public void switchToTab(int index) {
        if (isValidTabIndex(index) && index != currentTab) {
            currentTab = index;
            onTabChanged(index);
        }
    }

    /**
     * Called when the active tab changes.
     * <p>
     * Override this method in subclasses to add custom behavior when
     * switching tabs (e.g., saving state, updating UI elements).
     *
     * @param newTabIndex the index of the newly selected tab
     */
    protected void onTabChanged(int newTabIndex) {
        // Override in subclasses if needed
    }

    /**
     * Checks if a tab index is valid.
     *
     * @param index the index to validate
     * @return {@code true} if index is within bounds, {@code false} otherwise
     */
    private boolean isValidTabIndex(int index) {
        return index >= 0 && index < tabs.size();
    }

    // ==================== Lifecycle ====================

    /**
     * Initializes the screen.
     * <p>
     * Called when the screen is first displayed or after a resize.
     */
    @Override
    protected void init() {
        super.init();
    }

    /**
     * Updates the screen state each game tick.
     * <p>
     * Increments the tick counter for animations and delegates to parent.
     */
    @Override
    public void tick() {
        super.tick();
        this.tickCounter++;
    }

    /**
     * Handles screen resize events.
     * <p>
     * Propagates the resize event to all tabs so they can recalculate
     * their layout and positions.
     *
     * @param client the Minecraft client instance
     * @param width the new screen width
     * @param height the new screen height
     */
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        // Propagate resize to all tabs
        for (TabData tabData : tabs) {
            if (tabData.tab() != null) {
                tabData.tab().resize(client, width, height);
            }
        }
    }

    // ==================== Rendering ====================

    /**
     * Renders the screen background.
     * <p>
     * Override this method to customize background rendering.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    /**
     * Renders the entire screen including tabs and active tab content.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param delta the frame delta time
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderTabs(context, mouseX, mouseY);

        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            currentTab.render(context, mouseX, mouseY, delta);
        }
    }

    /**
     * Renders all tab buttons with appropriate styling.
     * <p>
     * Tabs are rendered horizontally centered at the top of the screen.
     * The selected tab is underlined, and hovered tabs change color.
     *
     * @param context the drawing context
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     */
    private void renderTabs(DrawContext context, int mouseX, int mouseY) {
        if (tabs.isEmpty()) {
            return;
        }

        TabLayoutInfo layout = calculateTabLayout();
        int currentX = layout.startX();

        for (int i = 0; i < tabs.size(); i++) {
            TabData tab = tabs.get(i);
            boolean isSelected = i == currentTab;
            boolean isHovered = isMouseOverTab(mouseX, mouseY, currentX, layout.guiY(), tab.width());

            renderTab(context, currentX, layout.guiY(), tab, isSelected, isHovered);
            currentX += tab.width() + TAB_MARGIN;
        }
    }

    /**
     * Renders a single tab button.
     *
     * @param context the drawing context
     * @param x the tab x-coordinate
     * @param y the tab y-coordinate
     * @param tab the tab data to render
     * @param selected whether this tab is currently selected
     * @param hovered whether the mouse is hovering over this tab
     */
    private void renderTab(DrawContext context, int x, int y, TabData tab, boolean selected, boolean hovered) {
        RenderSystem.enableBlend();

        int textColor = getTabTextColor(selected, hovered);
        Text styledText = tab.name().getWithStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withUnderline(selected)).getFirst();
        int textX = x + (tab.width() - textRenderer.getWidth(styledText)) / 2;
        int textY = y + (getTabHeight() - 8) / 2;
        context.drawText(textRenderer, styledText, textX, textY, textColor, false);

        RenderSystem.disableBlend();
    }

    /**
     * Gets the appropriate text color for a tab based on its state.
     *
     * @param selected whether the tab is selected
     * @param hovered whether the tab is hovered
     * @return the ARGB color value
     */
    private int getTabTextColor(boolean selected, boolean hovered) {
        if (selected) {
            return COLOR_SELECTED;
        } else if (hovered) {
            return COLOR_HOVERED;
        } else {
            return COLOR_NORMAL;
        }
    }

    // ==================== Input Handling ====================

    /**
     * Handles mouse movement events.
     * <p>
     * Delegates to the current tab for hover state updates.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     */
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);

        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            currentTab.mouseMoved(mouseX, mouseY);
        }
    }

    /**
     * Handles mouse scroll wheel events.
     * <p>
     * Delegates to the current tab, allowing it to handle scrolling.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param horizontalAmount the horizontal scroll amount
     * @param verticalAmount the vertical scroll amount
     * @return {@code true} if the scroll was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            if (currentTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    /**
     * Handles mouse drag events.
     * <p>
     * Delegates to the current tab for drag operations.
     *
     * @param mouseX the current mouse x-coordinate
     * @param mouseY the current mouse y-coordinate
     * @param button the mouse button being dragged
     * @param deltaX the horizontal movement since last frame
     * @param deltaY the vertical movement since last frame
     * @return {@code true} if the drag was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            if (currentTab.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    /**
     * Handles mouse button release events.
     * <p>
     * Delegates to the current tab for release handling.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the release was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            if (currentTab.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Handles mouse click events.
     * <p>
     * Checks for tab button clicks first, then delegates to the current
     * tab for content interaction.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (0=left, 1=right, 2=middle)
     * @return {@code true} if the click was handled, {@code false} otherwise
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check tab clicks first
        if (handleTabClick(mouseX, mouseY, button)) {
            return true;
        }

        // Delegate to current tab
        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            if (currentTab.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Handles clicks on tab buttons.
     * <p>
     * Detects which tab was clicked and switches to it, playing a click sound.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param button the mouse button (only left clicks are handled)
     * @return {@code true} if a tab was clicked, {@code false} otherwise
     */
    private boolean handleTabClick(double mouseX, double mouseY, int button) {
        if (tabs.isEmpty() || button != 0) {
            return false; // Only handle left clicks
        }

        TabLayoutInfo layout = calculateTabLayout();
        int currentX = layout.startX();

        for (int i = 0; i < tabs.size(); i++) {
            TabData tab = tabs.get(i);

            if (isMouseOverTab(mouseX, mouseY, currentX, layout.guiY(), tab.width())) {
                if (i != currentTab) {
                    playTabClickSound();
                    switchToTab(i);
                }
                return true;
            }

            currentX += tab.width() + TAB_MARGIN;
        }

        return false;
    }

    /**
     * Handles keyboard input for tab navigation.
     * <p>
     * Supports arrow key navigation:
     * <ul>
     *     <li>Right arrow: Next tab</li>
     *     <li>Left arrow: Previous tab</li>
     * </ul>
     *
     * @param keyCode the key code
     * @param scanCode the scan code
     * @param modifiers the modifier keys
     * @return {@code true} if the key was handled, {@code false} otherwise
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Arrow key navigation
        if (keyCode == KEY_RIGHT_ARROW && currentTab < tabs.size() - 1) {
            playTabArrowSound();
            switchToTab(currentTab + 1);
            return true;
        } else if (keyCode == KEY_LEFT_ARROW && currentTab > 0) {
            playTabArrowSound();
            switchToTab(currentTab - 1);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ==================== Layout Calculations ====================

    /**
     * Calculates the layout information for tab rendering.
     * <p>
     * Determines the starting x-position to center all tabs horizontally
     * within the screen background.
     *
     * @return layout information containing start position and dimensions
     */
    private TabLayoutInfo calculateTabLayout() {
        int guiX = (width - OnePieceScreen.Layout.BACKGROUND_WIDTH) / 2;
        int guiY = (height - OnePieceScreen.Layout.BACKGROUND_HEIGHT) / 2 + OnePieceScreen.Layout.TOP_MARGIN;
        int totalTabWidth = calculateTotalTabWidth();
        int startX = guiX + (OnePieceScreen.Layout.BACKGROUND_WIDTH - totalTabWidth) / 2;

        return new TabLayoutInfo(startX, guiY, totalTabWidth);
    }

    /**
     * Calculates the total width occupied by all tabs including margins.
     *
     * @return the total width in pixels
     */
    private int calculateTotalTabWidth() {
        if (tabs.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (TabData tab : tabs) {
            total += tab.width() + TAB_MARGIN;
        }
        return total - TAB_MARGIN; // Remove last margin
    }

    /**
     * Checks if the mouse is over a specific tab.
     *
     * @param mouseX the mouse x-coordinate
     * @param mouseY the mouse y-coordinate
     * @param tabX the tab x-coordinate
     * @param tabY the tab y-coordinate
     * @param tabWidth the tab width
     * @return {@code true} if mouse is over the tab, {@code false} otherwise
     */
    private boolean isMouseOverTab(double mouseX, double mouseY, int tabX, int tabY, int tabWidth) {
        return mouseX >= tabX && mouseX < tabX + tabWidth &&
                mouseY >= tabY && mouseY < tabY + getTabHeight();
    }

    // ==================== Sound Effects ====================

    /**
     * Plays a click sound when a tab is clicked.
     */
    private void playTabClickSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getSoundManager() != null) {
            client.getSoundManager().play(
                    PositionedSoundInstance.master(
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            SOUND_PITCH_TAB_SWITCH,
                            SOUND_VOLUME_TAB_CLICK
                    )
            );
        }
    }

    /**
     * Plays a softer sound when navigating tabs with arrow keys.
     */
    private void playTabArrowSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getSoundManager() != null) {
            client.getSoundManager().play(
                    PositionedSoundInstance.master(
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            SOUND_PITCH_TAB_SWITCH,
                            SOUND_VOLUME_TAB_ARROW
                    )
            );
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the height of tab buttons.
     * <p>
     * Attempts to use the layout constant from {@link OnePieceScreen},
     * falling back to a default value if unavailable.
     *
     * @return the tab height in pixels
     */
    private static int getTabHeight() {
        try {
            return OnePieceScreen.Layout.RECT_DIM;
        } catch (Exception e) {
            return 20; // Fallback value
        }
    }

    // ==================== Inner Classes ====================

    /**
     * Immutable record representing a tab's configuration.
     * <p>
     * Contains all data needed to render and manage a tab button
     * and its associated content.
     *
     * @param name the display name for the tab
     * @param tab the tab content implementation
     * @param width the tab button width in pixels
     */
    public record TabData(MutableText name, Tab tab, int width) {
        /**
         * Compact constructor validating tab data.
         */
        public TabData {
            Objects.requireNonNull(name, "Tab name cannot be null");
            Objects.requireNonNull(tab, "Tab content cannot be null");
            if (width <= 0) {
                throw new IllegalArgumentException("Tab width must be positive, got: " + width);
            }
        }
    }

    /**
     * Immutable record containing layout information for tab rendering.
     *
     * @param startX the x-coordinate where the first tab begins
     * @param guiY the y-coordinate for all tabs
     * @param totalWidth the total width occupied by all tabs
     */
    private record TabLayoutInfo(int startX, int guiY, int totalWidth) {}
}