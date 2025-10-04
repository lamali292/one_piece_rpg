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
 * Provides tab management, click handling, and keyboard shortcuts.
 */
public abstract class GenericTabbedScreen extends Screen {

    // ==================== Constants ====================

    private static final int TAB_MARGIN = 20;
    private static final int TAB_HEIGHT_GETTER = getTabHeight();

    private static final int COLOR_SELECTED = 0xFFFFFFFF;
    private static final int COLOR_HOVERED = 0xFFFFFFA0;
    private static final int COLOR_NORMAL = 0xFFFFFFFF;

    private static final float SOUND_PITCH_TAB_SWITCH = 1.0F;
    private static final float SOUND_VOLUME_TAB_CLICK = 0.8F;
    private static final float SOUND_VOLUME_TAB_ARROW = 0.9F;

    // Key codes for navigation
    private static final int KEY_RIGHT_ARROW = 262;
    private static final int KEY_LEFT_ARROW = 263;

    // ==================== Fields ====================

    protected final ClientPlayerEntity player;
    protected final List<TabData> tabs = new ArrayList<>();
    protected int currentTab = 0;
    protected int tickCounter = 0;

    // ==================== Constructor ====================

    public GenericTabbedScreen(Text title, ClientPlayerEntity player) {
        super(title);
        this.player = Objects.requireNonNull(player, "Player cannot be null");
    }

    // ==================== Tab Management ====================

    /**
     * Adds a new tab to the screen.
     *
     * @param name     The display name for the tab
     * @param tab      The tab content to render
     * @param tabWidth The width of the tab button
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
     * Gets the currently active tab, or null if no tabs exist.
     */
    public Tab getCurrentTab() {
        if (isValidTabIndex(currentTab)) {
            return tabs.get(currentTab).tab();
        }
        return null;
    }

    /**
     * Gets an immutable view of all tabs.
     */
    public List<TabData> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

    /**
     * Gets the index of the currently selected tab.
     */
    public int getCurrentTabIndex() {
        return currentTab;
    }

    /**
     * Switches to the specified tab index if valid.
     *
     * @param index The tab index to switch to
     * @return true if the tab was changed, false otherwise
     */
    public boolean switchToTab(int index) {
        if (isValidTabIndex(index) && index != currentTab) {
            currentTab = index;
            onTabChanged(index);
            return true;
        }
        return false;
    }

    /**
     * Called when the active tab changes. Override to add custom behavior.
     */
    protected void onTabChanged(int newTabIndex) {
        // Override in subclasses if needed
    }

    private boolean isValidTabIndex(int index) {
        return index >= 0 && index < tabs.size();
    }

    // ==================== Lifecycle ====================

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCounter++;
    }

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

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderTabs(context, mouseX, mouseY);

        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            currentTab.render(context, mouseX, mouseY, delta);
        }
    }

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

    private void renderTab(DrawContext context, int x, int y, TabData tab, boolean selected, boolean hovered) {
        RenderSystem.enableBlend();

        int textColor = getTabTextColor(selected, hovered);
        Text styledText = tab.name().getWithStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT).withUnderline(selected)).getFirst();
        int textX = x + (tab.width() - textRenderer.getWidth(styledText)) / 2;
        int textY = y + (getTabHeight() - 8) / 2;
        context.drawText(textRenderer, styledText, textX, textY, textColor, false);

        RenderSystem.disableBlend();
    }

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

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);

        Tab currentTab = getCurrentTab();
        if (currentTab != null) {
            currentTab.mouseMoved(mouseX, mouseY);
        }
    }

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

    private TabLayoutInfo calculateTabLayout() {
        int guiX = (width - OnePieceScreen.Layout.BACKGROUND_WIDTH) / 2;
        int guiY = (height - OnePieceScreen.Layout.BACKGROUND_HEIGHT) / 2 + OnePieceScreen.Layout.TOP_MARGIN;
        int totalTabWidth = calculateTotalTabWidth();
        int startX = guiX + (OnePieceScreen.Layout.BACKGROUND_WIDTH - totalTabWidth) / 2;

        return new TabLayoutInfo(startX, guiY, totalTabWidth);
    }

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

    private boolean isMouseOverTab(double mouseX, double mouseY, int tabX, int tabY, int tabWidth) {
        return mouseX >= tabX && mouseX < tabX + tabWidth &&
                mouseY >= tabY && mouseY < tabY + getTabHeight();
    }

    // ==================== Sound Effects ====================

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

    private static int getTabHeight() {
        try {
            return OnePieceScreen.Layout.RECT_DIM;
        } catch (Exception e) {
            return 20; // Fallback value
        }
    }

    // ==================== Inner Classes ====================

    /**
     * Immutable data class representing a tab's configuration.
     */
    public record TabData(MutableText name, Tab tab, int width) {
        public TabData {
            Objects.requireNonNull(name, "Tab name cannot be null");
            Objects.requireNonNull(tab, "Tab content cannot be null");
            if (width <= 0) {
                throw new IllegalArgumentException("Tab width must be positive, got: " + width);
            }
        }
    }

    /**
     * Layout information for tab rendering.
     */
    private record TabLayoutInfo(int startX, int guiY, int totalWidth) {}
}