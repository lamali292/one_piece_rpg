package de.one_piece_api.gui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

public interface Tab extends Drawable, Element {

    @Override
    default void setFocused(boolean focused) {

    }

    @Override
    default boolean isFocused() {
        return false;
    }

    void resize(MinecraftClient client, int width, int height);
}
