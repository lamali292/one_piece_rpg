package de.one_piece_api.screens;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.network.ClassConfigPayload;
import de.one_piece_api.network.SetClassPayload;
import de.one_piece_api.util.Data;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.puffish.skillsmod.config.IconConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassScreen extends Screen {
    // Grid Configuration
    private static final int GRID_COLUMNS = 4;
    private static final int CELL_SIZE = 48;
    private static final int CELL_PADDING = 8;

    // Colors (wie in Ihrem Original Code)
    private static final int COLOR_WHITE = ColorHelper.Argb.getArgb(255, 255, 255, 255);
    private static final int COLOR_FRAME = ColorHelper.Argb.getArgb(255, 200, 200, 200);

    private final ClientPlayerEntity player;
    public ClassScreen(ClientPlayerEntity player) {
        super(ScreenTexts.EMPTY);
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        // Cache classes for consistent ordering
        ClientPlayNetworking.send(new ClassConfigPayload.Request());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        super.render(context, mouseX, mouseY, delta);
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.of("Select Your Class"),
                this.width / 2, 20, COLOR_WHITE);

        // Render Class Grid
        renderClassGrid(context, mouseX, mouseY);

    }

    private void renderClassGrid(DrawContext context, int mouseX, int mouseY) {
        var classesList = Data.classConfigMap;
        if (classesList.isEmpty()) return;
        
        int totalClasses = classesList.size();
        int totalRows = (totalClasses + GRID_COLUMNS - 1) / GRID_COLUMNS; // Ceiling division
        
        // Berechne Grid-Dimensionen
        int totalGridWidth = GRID_COLUMNS * CELL_SIZE + (GRID_COLUMNS - 1) * CELL_PADDING;
        int totalGridHeight = totalRows * CELL_SIZE + (totalRows - 1) * CELL_PADDING;
        
        // Zentriere das Grid auf dem Bildschirm
        int gridStartX = (this.width - totalGridWidth) / 2;
        int gridStartY = (this.height - totalGridHeight) / 2;
        
        int index = 0;
        for (Map.Entry<Identifier, ClassConfig> entry : classesList.entrySet()) {
            Identifier className = entry.getKey();
            var classData = entry.getValue();
            
            // Grid Position berechnen
            int col = index % GRID_COLUMNS;
            int row = index / GRID_COLUMNS;
            
            // Für die letzte Reihe: Zentriere die verbleibenden Items
            int colsInThisRow = (row == totalRows - 1) 
                ? totalClasses - (row * GRID_COLUMNS)  // Anzahl Items in letzter Reihe
                : GRID_COLUMNS;                        // Volle Reihe
            
            // Offset für Zentrierung der letzten Reihe
            int rowOffset = 0;
            if (row == totalRows - 1 && colsInThisRow < GRID_COLUMNS) {
                int remainingWidth = colsInThisRow * CELL_SIZE + (colsInThisRow - 1) * CELL_PADDING;
                rowOffset = (totalGridWidth - remainingWidth) / 2;
            }
            
            // Finale Position berechnen
            int x = gridStartX + rowOffset + col * (CELL_SIZE + CELL_PADDING) + CELL_SIZE / 2;
            int y = gridStartY + row * (CELL_SIZE + CELL_PADDING) + CELL_SIZE / 2;
            
            // Check if mouse is hovering
            boolean isHovered = isPointInCell(mouseX, mouseY, x, y);
            
            // Draw Frame
            drawClassFrame(context, x, y, isHovered, classData);
            
            // Draw Icon
            if (classData.icon() != null) {
                drawClassIcon(context, classData.icon(), x, y);
            }

            Text displayName = classData.name();

            int textX = x - this.textRenderer.getWidth(displayName) / 2;
            int textY = y + CELL_SIZE / 2 + 10;
            context.drawText(this.textRenderer, displayName, textX, textY, COLOR_WHITE, false);
            
            // Tooltip on hover
            if (isHovered) {
                renderClassTooltip(context, mouseX, mouseY, classData);
            }
            
            index++;
        }
    }

    private void drawClassFrame(DrawContext context, int x, int y, boolean hovered, ClassConfig classData) {
        int halfSize = CELL_SIZE / 2;

        // Use class colors if available
        int primaryColor = COLOR_WHITE;
        int secondaryColor = COLOR_FRAME;

        if (classData.primaryColor() != null && classData.secondaryColor() != null) {
            primaryColor = hovered ? classData.primaryColor().argb() : classData.secondaryColor().argb();
            secondaryColor = classData.secondaryColor().argb();
        }

        // Frame mit class-spezifischen Farben
        context.fill(x - halfSize - 2, y - halfSize - 2,
                x + halfSize + 2, y + halfSize + 2,
                ColorHelper.Argb.getArgb(100, 0, 0, 0)); // Dark border

        context.fill(x - halfSize, y - halfSize,
                x + halfSize, y + halfSize,
                ColorHelper.Argb.getArgb(80,
                        ColorHelper.Argb.getRed(secondaryColor),
                        ColorHelper.Argb.getGreen(secondaryColor),
                        ColorHelper.Argb.getBlue(secondaryColor))); // Class color fill
    }


    private void drawClassIcon(DrawContext context, IconConfig iconData, int x, int y) {
        if (iconData == null) return;

        if (iconData instanceof IconConfig.EffectIconConfig effectIconConfig) {
            drawFallbackIcon(context, x, y, "?");
        } else if (iconData instanceof IconConfig.ItemIconConfig(ItemStack item)) {
            drawItemIcon(context, item, x, y);
        } else if (iconData instanceof IconConfig.TextureIconConfig textureIconConfig) {
            drawFallbackIcon(context, x, y, "?");
        }
    }

    private void drawItemIcon(DrawContext context, ItemStack stack, int x, int y) {
        try {
            if (stack != null) {
                int iconX = x - 8;
                int iconY = y - 8;
                context.drawItem(stack, iconX, iconY);
            } else {
                drawFallbackIcon(context, x, y, "I");
            }
        } catch (Exception e) {
            drawFallbackIcon(context, x, y, "!");
        }
    }

    private void drawTextureIcon(DrawContext context, String textureId, int x, int y) {
        try {
            Identifier texture = Identifier.tryParse(textureId);
            if (texture != null) {
                int size = 16;
                context.drawTexture(texture, x - size/2, y - size/2, 0, 0, size, size, size, size);
            } else {
                drawFallbackIcon(context, x, y, "T");
            }
        } catch (Exception e) {
            drawFallbackIcon(context, x, y, "!");
        }
    }

    private void drawFallbackIcon(DrawContext context, int x, int y, String letter) {
        // Simple text fallback
        int textX = x - this.textRenderer.getWidth(letter) / 2;
        int textY = y - this.textRenderer.fontHeight / 2;
        context.drawText(this.textRenderer, letter, textX, textY, COLOR_WHITE, false);
    }

    private boolean isPointInCell(int mouseX, int mouseY, int cellCenterX, int cellCenterY) {
        int halfSize = CELL_SIZE / 2;
        return mouseX >= cellCenterX - halfSize &&
                mouseX <= cellCenterX + halfSize &&
                mouseY >= cellCenterY - halfSize &&
                mouseY <= cellCenterY + halfSize;
    }

    private void renderClassTooltip(DrawContext context, int mouseX, int mouseY, ClassConfig classData) {
        List<Text> tooltip = new ArrayList<>();

        // Use translated name if available
        Text displayName = classData.name();
        Text description = classData.description();

        tooltip.add(Text.of("§6" + displayName.getString()));
        if (description != null) {
            tooltip.add(Text.of("§7" + description.getString()));
            tooltip.add(Text.of("")); // Empty line
        }

        // Use translatable labels for Primary and Passive
        String primaryLabel = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".class.primary").getString();
        String passiveLabel = Text.translatable("gui." + OnePieceRPG.MOD_ID + ".class.passive").getString();

        tooltip.add(Text.of("§7" + primaryLabel + ": §f" + classData.primary()));
        tooltip.add(Text.of("§7" + passiveLabel + ": §f" + classData.passive()));

        context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            var classesList = Data.classConfigMap;
            if (classesList.isEmpty()) return false;
        
            int totalClasses = classesList.size();
            int totalRows = (totalClasses + GRID_COLUMNS - 1) / GRID_COLUMNS;
        
            // Grid-Dimensionen (gleiche Berechnung wie in renderClassGrid)
            int totalGridWidth = GRID_COLUMNS * CELL_SIZE + (GRID_COLUMNS - 1) * CELL_PADDING;
            int totalGridHeight = totalRows * CELL_SIZE + (totalRows - 1) * CELL_PADDING;
            int gridStartX = (this.width - totalGridWidth) / 2;
            int gridStartY = (this.height - totalGridHeight) / 2;
        
            int index = 0;
            for (Map.Entry<Identifier, ClassConfig> entry : classesList.entrySet()) {
                Identifier className = entry.getKey();
            
                int col = index % GRID_COLUMNS;
                int row = index / GRID_COLUMNS;
            
                // Gleiche Zentrierung für letzte Reihe
                int colsInThisRow = (row == totalRows - 1) 
                    ? totalClasses - (row * GRID_COLUMNS)
                    : GRID_COLUMNS;
            
                int rowOffset = 0;
                if (row == totalRows - 1 && colsInThisRow < GRID_COLUMNS) {
                    int remainingWidth = colsInThisRow * CELL_SIZE + (colsInThisRow - 1) * CELL_PADDING;
                    rowOffset = (totalGridWidth - remainingWidth) / 2;
                }
            
                int x = gridStartX + rowOffset + col * (CELL_SIZE + CELL_PADDING) + CELL_SIZE / 2;
                int y = gridStartY + row * (CELL_SIZE + CELL_PADDING) + CELL_SIZE / 2;
            
                if (isPointInCell((int)mouseX, (int)mouseY, x, y)) {
                    onClassSelected(className);
                    return true;
                }
            
                index++;
            }
        }
    
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onClassSelected(Identifier className) {
        ClientPlayNetworking.send(new SetClassPayload(className));
        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}