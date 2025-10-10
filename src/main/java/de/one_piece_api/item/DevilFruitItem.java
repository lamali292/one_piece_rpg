package de.one_piece_api.item;

import de.one_piece_api.event.EventRegistry;
import de.one_piece_api.init.MyDataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class DevilFruitItem extends Item {
    public static Identifier DEFAULT_DEVIL_FRUIT = Identifier.of("none");

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
        if (!fruit.passives().isEmpty()) {
            tooltip.add(Text.literal("Passives: " + fruit.passives().stream().map(SkillDefinitionReferenceConfig::id).toList()));
        }*/

    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player) {
            Identifier id = stack.getOrDefault(MyDataComponentTypes.DEVIL_FRUIT, DEFAULT_DEVIL_FRUIT);
            EventRegistry.DEVIL_FRUIT_EATEN.invoker().onDevilFruitEaten(player, id);
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
