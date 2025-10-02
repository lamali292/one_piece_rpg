
package de.one_piece_api.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import de.one_piece_api.registries.MyFonts;
import de.one_piece_api.gui.OnePieceScreen;
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
import java.util.List;

public class GenericTabbedScreen extends Screen {
    // Tab-Texturen

    protected final ClientPlayerEntity player;
    protected final int tabMargin = 20;
    protected final int tabHeight = OnePieceScreen.rectDim;

    protected int currentTab = 0;
    protected final List<TabData> tabs = new ArrayList<>();
    protected int tickCounter = 0;

    public GenericTabbedScreen(Text title, ClientPlayerEntity player) {
        super(title);
        this.player = player;
    }


    public record TabData(MutableText name, Tab tab, int width) {
    }

    public void addTab(MutableText name, Tab screen, int tabWidth) {
        tabs.add(new TabData(name, screen, tabWidth));
    }

    public Tab getCurrentTab() {
        if (currentTab < tabs.size()) {
            return tabs.get(currentTab).tab();
        }
        return null;
    }

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
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderTabs(context, mouseX, mouseY);
        if (currentTab < tabs.size()) {
            tabs.get(currentTab).tab.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        if (currentTab < tabs.size()) {
            tabs.forEach(tab -> tab.tab.resize(client, width, height));
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        if (currentTab < tabs.size()) {
            tabs.get(currentTab).tab.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentTab < tabs.size()) {
            return tabs.get(currentTab).tab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (currentTab < tabs.size()) {
            return tabs.get(currentTab).tab.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (currentTab < tabs.size()) {
            return tabs.get(currentTab).tab.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderTabs(DrawContext context, int mouseX, int mouseY) {
        int guiX = (width - OnePieceScreen.backgroundWidth) / 2;
        int guiY = (height - OnePieceScreen.backgroundHeight) / 2 + OnePieceScreen.topMargin;
        int totalTabWidth = calculateTotalTabWidth();
        int startX = guiX + (OnePieceScreen.backgroundWidth - totalTabWidth) / 2;


        for (int i = 0; i < tabs.size(); i++) {
            TabData tab = tabs.get(i);
            boolean isSelected = i == currentTab;
            boolean isHovered = isMouseOverTab(mouseX, mouseY, startX, guiY, tab.width);
            renderTab(context, startX, guiY, tab, isSelected, isHovered);
            startX += tab.width + tabMargin;
        }
    }

    private void renderTab(DrawContext context, int x, int y,
                          TabData tab, boolean selected, boolean hovered) {
        RenderSystem.enableBlend();
        int textColor = selected ? 0xFFFFFFFF : (hovered ? 0xFFFFFFA0 : 0xFFFFFFFF);

        var text = tab.name.setStyle(Style.EMPTY.withFont(MyFonts.MONTSERRAT));
        int textX = x + (tab.width - textRenderer.getWidth(text)) / 2;
        int textY = y + (tabHeight - 8) / 2;
        context.drawText(textRenderer, text, textX, textY, textColor, false);
        RenderSystem.disableBlend();
    }


    private boolean isMouseOverTab(double mouseX, double mouseY, int tabX, int tabY, int tabWidth) {
        return mouseX >= tabX && mouseX < tabX + tabWidth &&
                mouseY >= tabY && mouseY < tabY + tabHeight;
    }

    private int calculateTotalTabWidth() {
        return tabs.stream().mapToInt(TabData::width).map(t->t+tabMargin).sum() - tabMargin;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Tab-Clicks prüfen
        int guiX = (width - OnePieceScreen.backgroundWidth) / 2;
        int guiY = (height - OnePieceScreen.backgroundHeight) / 2 + OnePieceScreen.topMargin;
        int totalTabWidth = calculateTotalTabWidth();
        int startX = guiX + (OnePieceScreen.backgroundWidth - totalTabWidth) / 2;

        for (int i = 0; i < tabs.size(); i++) {
            TabData tab = tabs.get(i);
            if (isMouseOverTab(mouseX, mouseY, startX, guiY, tab.width)) {
                if (i != currentTab) {
                    MinecraftClient.getInstance().getSoundManager().play(
                            PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.8F)
                    );
                    currentTab = i;
                }
                return true;
            }
            startX += tab.width + tabMargin;
        }
        if (currentTab < tabs.size()) {
            Tab currentScreen = tabs.get(currentTab).tab();
            return currentScreen.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 262 && currentTab < tabs.size() - 1) { // Right Arrow
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.9F)
            );
            currentTab++;
            return true;
        } else if (keyCode == 263 && currentTab > 0) { // Left Arrow
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.9F)
            );
            currentTab--;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}