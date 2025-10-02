package de.one_piece_api.gui.widgets;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.registries.MyShaders;
import de.one_piece_api.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.util.SpellRender;
import org.joml.Vector2i;

import java.util.function.Consumer;

/**
 * Widget representing a single spell slot in the skill tree UI.
 * Handles rendering, hover detection, and click interactions for spell slots.
 */
public class SpellSlotWidget implements Drawable, Element {
    private static final Identifier SKILL_SMALL_RECT = OnePieceRPG.id("textures/gui/skill/small_rect.png");
    private static final int ICON_PADDING = 3;

    private final int x;
    private final int y;
    private final int size;
    private final int iconSize;
    private final int slotIndex;

    private RegistryEntry<Spell> spell;
    private boolean learned;
    private Consumer<SpellSlotWidget> onLeftClick;
    private Consumer<SpellSlotWidget> onRightClick;
    private Consumer<HoverInfo> onHover;

    public SpellSlotWidget(int x, int y, int size, int slotIndex) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.iconSize = size - ICON_PADDING * 2;
        this.slotIndex = slotIndex;
    }

    public void setSpell(RegistryEntry<Spell> spell, boolean learned) {
        this.spell = spell;
        this.learned = learned;
    }

    public void setOnLeftClick(Consumer<SpellSlotWidget> handler) {
        this.onLeftClick = handler;
    }

    public void setOnRightClick(Consumer<SpellSlotWidget> handler) {
        this.onRightClick = handler;
    }

    public void setOnHover(Consumer<HoverInfo> handler) {
        this.onHover = handler;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw slot background
        context.drawTexture(SKILL_SMALL_RECT, x, y, 0, 0, size, size, size, size);

        // Draw spell icon if present
        if (spell != null) {
            Identifier spellId = Identifier.of(spell.getIdAsString());
            Identifier iconId = SpellRender.iconTexture(spellId);

            int iconX = x + ICON_PADDING;
            int iconY = y + ICON_PADDING;

            if (learned) {
                context.drawTexture(iconId, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            } else {
                RenderUtil.drawTexture(context, MyShaders::getGrayscaleShader,
                        iconId, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            }
        }

        // Handle hover
        if (isMouseOver(mouseX, mouseY) && onHover != null) {
            HoverInfo info = createHoverInfo();
            if (info != null) {
                onHover.accept(info);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        if (button == 0 && onLeftClick != null) {
            onLeftClick.accept(this);
            return true;
        } else if (button == 1 && onRightClick != null) {
            onRightClick.accept(this);
            return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + size &&
                mouseY >= y && mouseY < y + size;
    }

    private HoverInfo createHoverInfo() {
        if (spell == null) {
            return null;
        }

        String spellId = spell.getIdAsString();
        String nameKey = "spell." + spellId.replace(":", ".") + ".name";
        Text title = Text.translatable(nameKey).formatted(Formatting.WHITE);

        String descKey = "spell." + spellId.replace(":", ".") + ".description";
        Text description = Text.translatable(descKey).formatted(Formatting.GRAY);

        Text available = learned ? Text.empty() : Text.literal("Not Learned!");

        return new HoverInfo(title, description, available, Text.empty());
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public Vector2i getPosition() {
        return new Vector2i(x, y);
    }

    public RegistryEntry<Spell> getSpell() {
        return spell;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    public record HoverInfo(Text title, Text description, Text extraDescription, Text advanced) {}
}