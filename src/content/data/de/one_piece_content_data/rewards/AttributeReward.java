package de.one_piece_content_data.rewards;

import com.google.gson.JsonObject;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;

public record AttributeReward(RegistryEntry<EntityAttribute> attribute, float value,
                              EntityAttributeModifier.Operation operation) implements Reward {
    public static final Identifier ID = SkillsMod.createIdentifier("attribute");
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("attribute", attribute.getKey().get().getValue().toString());
        json.addProperty("value", value);
        String opString = switch (operation) {
            case ADD_VALUE -> "addition";
            case ADD_MULTIPLIED_BASE -> "multiply_base";
            case ADD_MULTIPLIED_TOTAL -> "multiply_total";
        };
        json.addProperty("operation", opString);
        return json;
    }
    @Override
    public void update(RewardUpdateContext context) {
    }

    @Override
    public void dispose(RewardDisposeContext context) {
    }
}
