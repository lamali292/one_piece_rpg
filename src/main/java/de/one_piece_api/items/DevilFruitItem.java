package de.one_piece_api.items;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.interfaces.IOnePiecePlayer;
import de.one_piece_api.registries.MyDataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class DevilFruitItem extends Item {
    public static Identifier DEFAULT_DEVIL_FRUIT = Identifier.of("");

    public DevilFruitItem(Settings settings) {
        super(settings.component(MyDataComponentTypes.DEVIL_FRUIT, DEFAULT_DEVIL_FRUIT));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack itemStack = super.getDefaultStack();
        itemStack.set(MyDataComponentTypes.DEVIL_FRUIT, DEFAULT_DEVIL_FRUIT);
        return itemStack;
    }



    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        Identifier id = stack.getOrDefault(MyDataComponentTypes.DEVIL_FRUIT, DEFAULT_DEVIL_FRUIT);
        String path = id.getPath().replaceAll("_", " ");
        if (path.isEmpty()) return;
        tooltip.add(Text.literal(path));
        /*DevilFruitConfig fruit = DevilFruitLoader.INSTANCE.get(id);
        if (fruit == null) return;
        tooltip.add(Text.literal("Devil Fruit: " + id).formatted(Formatting.GOLD));
        if (!fruit.paths().isEmpty()) {
            tooltip.add(Text.literal("Spells: " + fruit.paths().stream().map(s->s.skillDefinitions().getFirst().id()).toList()));
        }
        if (!fruit.instantPassives().isEmpty()) {
            tooltip.add(Text.literal("Passives: " + fruit.instantPassives().stream().map(SkillDefinitionReferenceConfig::id).toList()));
        }*/

    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        Identifier id = stack.getOrDefault(MyDataComponentTypes.DEVIL_FRUIT, DEFAULT_DEVIL_FRUIT);
        if (world instanceof ServerWorld serverWorld) {
            if (user instanceof IOnePiecePlayer player) {
                player.onepiece$setDevilFruit(id.toString());
            }
        }
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}
