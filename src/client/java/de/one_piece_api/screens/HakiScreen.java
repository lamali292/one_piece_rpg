// HakiScreen.java
package de.one_piece_api.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class HakiScreen extends Screen {
    private final ClientPlayerEntity player;
    private final Screen parent;
    public HakiScreen(Screen parent, ClientPlayerEntity player) {
        super(Text.literal("Haki"));
        init(MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
        this.player = player;
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {


  }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}