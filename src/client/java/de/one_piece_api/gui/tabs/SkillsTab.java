package de.one_piece_api.gui.tabs;

import de.one_piece_api.gui.OnePieceScreen;
import de.one_piece_api.gui.util.Tab;
import de.one_piece_api.gui.widgets.SkillTreeViewportWidget;
import de.one_piece_api.network.SkillClickPacket;
import de.one_piece_api.util.OnePieceCategory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.network.packets.out.SkillClickOutPacket;
import org.lwjgl.glfw.GLFW;

/**
 * Refactored SkillsScreen using widget-based architecture.
 * Manages the skill tree viewport and interaction logic.
 */
public class SkillsTab implements Tab {
    private static final int BOX_WIDTH = OnePieceScreen.skilltreeWidth;
    private static final int BOX_HEIGHT = OnePieceScreen.contentHeight;
    private static final double DRAG_THRESHOLD = 2.0;

    private final OnePieceScreen parent;
    private final TextRenderer textRenderer;
    private int width;
    private int height;
    private SkillTreeViewportWidget viewportWidget;
    private final DragState dragState = new DragState();

    public SkillsTab(OnePieceScreen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;
        this.width = client.getWindow().getScaledWidth();
        this.height = client.getWindow().getScaledHeight();
        this.parent = parent;

        initializeWidget();
    }

    private void initializeWidget() {
        ViewportPosition pos = getViewportPosition();

        this.viewportWidget = new SkillTreeViewportWidget(pos.x(), pos.y(), BOX_WIDTH, BOX_HEIGHT);
        this.viewportWidget.setCategoryData(parent.getCategoryData());

        // Setup hover handlers to update parent's description panel
        this.viewportWidget.setOnSkillHover(info -> {
            parent.setHoveredSkillInfo(
                    info.title(),
                    info.description(),
                    info.extraDescription(),
                    Text.literal(info.skillId())
            );
        });

        //this.viewportWidget.setOnNoSkillHover(parent::clearHoveredSkillInfo);
    }


    @Override
    public void resize(MinecraftClient client, int width, int height) {
        this.width = width;
        this.height = height;
        initializeWidget();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!parent.hasCategoryData()) {
            renderEmptyState(context);
            return;
        }
        if (viewportWidget != null) {
            viewportWidget.setCategoryData(parent.getCategoryData());
            viewportWidget.render(context, mouseX, mouseY, delta);

        }
    }

    private void renderEmptyState(DrawContext context) {
        ViewportPosition viewport = getViewportPosition();
        int centerX = viewport.x() + BOX_WIDTH / 2;
        int centerY = viewport.y() + BOX_HEIGHT / 2;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("advancements.sad_label"),
                centerX, centerY - this.textRenderer.fontHeight,
                0xffffffff
        );
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("advancements.empty"),
                centerX, centerY + this.textRenderer.fontHeight,
                0xffffffff
        );
    }



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) return false;
        if (!parent.hasCategoryData()) return false;

        ViewportPosition viewport = getViewportPosition();

        if (viewportWidget.isMouseNotInViewport(mouseX, mouseY)) {
            return false;
        }

        double adjustedMouseX = mouseX - viewport.x();
        double adjustedMouseY = mouseY - viewport.y();

        dragState.startDrag(adjustedMouseX, adjustedMouseY, parent.getCategoryData());
        return handleSkillClick(mouseX, mouseY);
    }

    private boolean handleSkillClick(double mouseX, double mouseY) {
        var skillId = viewportWidget.getSkillAtPosition(mouseX, mouseY);

        if (skillId.isPresent()) {
            SkillsClientMod.getInstance()
                    .getPacketSender()
                    .send(new SkillClickOutPacket(parent.getCategoryData().getConfig().id(), skillId.get()));
            //ClientPlayNetworking.send(new SkillClickPacket(parent.getCategoryData().getConfig().id(), skillId.get()));
            parent.updateLearned();
            dragState.preventDrag();
            return true;
        }

        return false;
    }

    public void refreshCategoryData() {
        if (viewportWidget != null && parent.hasCategoryData()) {
            viewportWidget.setCategoryData(parent.getCategoryData());
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1 || !dragState.canDrag()) return false;

        dragState.updateDrag(deltaX, deltaY);

        if (dragState.shouldStartDragging() && parent.hasCategoryData()) {
            ViewportPosition viewport = getViewportPosition();
            double adjustedMouseX = mouseX - viewport.x();
            double adjustedMouseY = mouseY - viewport.y();

            viewportWidget.applyPan(adjustedMouseX, adjustedMouseY, dragState.startX, dragState.startY);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_1) return false;
        if (dragState.isSmallDrag() && parent.hasCategoryData()) {
            handleSkillClick(mouseX, mouseY);
        }
        dragState.endDrag();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (viewportWidget.isMouseNotInViewport(mouseX, mouseY) || !parent.hasCategoryData()) {
            return false;
        }

        viewportWidget.applyZoom(mouseX, mouseY, verticalAmount);
        return true;
    }

    private ViewportPosition getViewportPosition() {
        int x = (width - OnePieceScreen.backgroundWidth) / 2 + OnePieceScreen.skilltreeOffsetX;
        int y = (height - OnePieceScreen.backgroundHeight) / 2 + OnePieceScreen.contentOffsetY;
        return new ViewportPosition(x, y);
    }

    private static class DragState {
        double startX = 0;
        double startY = 0;
        double totalDrag = 0;
        boolean canDrag = false;

        void startDrag(double mouseX, double mouseY, net.puffish.skillsmod.client.data.ClientCategoryData categoryData) {
            this.startX = mouseX - categoryData.getX();
            this.startY = mouseY - categoryData.getY();
            this.totalDrag = 0;
            this.canDrag = true;
        }

        void updateDrag(double deltaX, double deltaY) {
            this.totalDrag += Math.abs(deltaX) + Math.abs(deltaY);
        }

        void preventDrag() {
            this.canDrag = false;
        }

        void endDrag() {
            this.canDrag = false;
            this.totalDrag = 0;
        }

        boolean canDrag() {
            return canDrag;
        }

        boolean shouldStartDragging() {
            return totalDrag > DRAG_THRESHOLD;
        }

        boolean isSmallDrag() {
            return canDrag && totalDrag <= DRAG_THRESHOLD;
        }
    }

    private record ViewportPosition(int x, int y) {}
}
