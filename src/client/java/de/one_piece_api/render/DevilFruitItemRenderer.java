package de.one_piece_api.render;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.init.MyDataComponentTypes;
import de.one_piece_api.item.DevilFruitItem;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class DevilFruitItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        FabricBakedModelManager modelManager = (FabricBakedModelManager) client.getBakedModelManager();

        // Get the devil fruit identifier from the component
        Identifier fruitId = stack.getOrDefault(MyDataComponentTypes.DEVIL_FRUIT,
                DevilFruitItem.DEFAULT_DEVIL_FRUIT);

        // Construct model identifier
        Identifier modelId = fruitId.withPrefixedPath("devil_fruit/");

        // Use Fabric's getModel(Identifier)
        BakedModel model = modelManager.getModel(modelId);

        if (model == null || model == client.getBakedModelManager().getMissingModel()) {
            // Try fallback
            model = modelManager.getModel(OnePieceRPG.id( "devil_fruit/default"));

            if (model == null || model == client.getBakedModelManager().getMissingModel()) {
                model = client.getBakedModelManager().getMissingModel();
            }
        }

        client.getItemRenderer().renderItem(
                stack,
                mode,
                false,
                matrices,
                vertexConsumers,
                light,
                overlay,
                model  // This is the key - we pass OUR model
        );
    }

    private void renderQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads,
                             int light, int overlay) {
        MatrixStack.Entry entry = matrices.peek();

        for (BakedQuad quad : quads) {
            // Use white color for all quads (you can add item color support later via mixin if needed)
            vertices.quad(entry, quad, 1.0f, 1.0f, 1.0f, 1.0f, light, overlay);
        }
    }
}