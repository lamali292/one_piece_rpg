package de.one_piece_content_data.datagen;

import com.google.gson.*;
import de.one_piece_api.config.ClassConfig;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.config.DevilFruitPathConfig;
import de.one_piece_api.config.SkillDefinitionReferenceConfig;
import de.one_piece_content_data.content.ExampleSkills;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.puffish.skillsmod.reward.builtin.DummyReward;
import net.spell_engine.api.spell.container.SpellContainer;

import java.util.List;
import java.util.Optional;

public class JsonParser {


    public static JsonObject toJson(ClassConfig classConfig) {
        JsonObject json = new JsonObject();
        if (classConfig.name() != null && !classConfig.name().getString().isEmpty()) {
            JsonObject title = new JsonObject();
            title.addProperty("translate", classConfig.name().getString());
            json.add("name", title);
        }
        if (classConfig.description() != null && !classConfig.description().getString().isEmpty()) {
            JsonObject title = new JsonObject();
            title.addProperty("translate", classConfig.description().getString());
            json.add("description", title);
        }
        var colorConfig =   new ClassConfig.ColorPairConfig(classConfig.primaryColor(), classConfig.secondaryColor());
        json.add("color", JsonParser.toJson(colorConfig));
        // Add passive skill
        json.addProperty("primary", classConfig.primary());
        json.addProperty("passive", classConfig.passive());

        // Add icon
        json.add("icon", JsonParser.toJson(classConfig.icon()));
        return json;
    }

    private static JsonObject toJson(ClassConfig.ColorPairConfig colorConfig) {
        JsonObject json = new JsonObject();
        json.addProperty("primary", argbToHex(colorConfig.primary().argb()));
        json.addProperty("secondary", argbToHex(colorConfig.secondary().argb()));
        return json;
    }

    public static String argbToHex(int argb) {
        // Extract RGB components (ignore alpha channel)
        int rgb = argb & 0xFFFFFF;
        return String.format("#%06X", rgb);
    }


    public static JsonObject toJson(DevilFruitConfig fruitConfig) {
        JsonObject json = new JsonObject();

        // spells (als Objekt mit key->config)
        JsonObject spellsObject = new JsonObject();
        for (int i = 0; i < fruitConfig.paths().size(); i++) {
            DevilFruitPathConfig pathConfig = fruitConfig.paths().get(i);
            spellsObject.add("path" + i, JsonParser.toJson(pathConfig));
        }
        json.add("spells", spellsObject);

        // passives (als Array)
        var passivesArray = new JsonArray();
        for (SkillDefinitionReferenceConfig passive : fruitConfig.instantPassives()) {
            passivesArray.add(JsonParser.toJson(passive));
        }
        json.add("passives", passivesArray);

        // modelId (als String)
        json.addProperty("modelId", fruitConfig.modelId().toString());

        return json;
    }

    public static JsonObject toJson(SkillDefinitionReferenceConfig config) {
        JsonObject json = new JsonObject();
        json.addProperty("id",config.id());
        return json;
    }


    public static JsonObject toJson(DevilFruitPathConfig config) {
        JsonObject json = new JsonObject();
        var skillsArray = new JsonArray();
        for (SkillDefinitionReferenceConfig skill : config.skillDefinitions()) {
            skillsArray.add(JsonParser.toJson(skill));
        }
        json.add("skills", skillsArray);
        return json;
    }


    public static JsonObject toJson(List<ExampleSkills.SkillDefinition> skill) {
        JsonObject json = new JsonObject();
        for (ExampleSkills.SkillDefinition entry : skill) {
            json.add(entry.config().id(), toJson(entry.config()));
        }
        return json;
    }


    public static JsonObject toJson(SkillDefinitionConfig skill) {
        JsonObject json = new JsonObject();
        if (skill.title() != null && !skill.description().getString().isEmpty()) {
            JsonObject title = new JsonObject();
            title.addProperty("translate", ExampleSkills.titleTranslationKey(skill.id()));
            json.add("title", title);
        }
        if (skill.description() != null && !skill.description().getString().isEmpty()) {
            JsonObject description = new JsonObject();
            description.addProperty("translate", ExampleSkills.descriptionTranslationKey(skill.id()));
            json.add("description", description);
        }
        if (skill.extraDescription() != null && !skill.extraDescription().getString().isEmpty()) {
            JsonObject extraDescription = new JsonObject();
            extraDescription.addProperty("translate", ExampleSkills.descriptionTranslationKey(skill.id())+"_extra");
            json.add("extra_description", extraDescription);
        }
        json.add("icon", JsonParser.toJson(skill.icon()));
        if (skill.frame() != null) {
            json.add("frame", JsonParser.toJson(skill.frame()));
        }
        if (skill.size() != 1f) {
            json.addProperty("size", skill.size());
        }
        if (skill.rewards() != null && !skill.rewards().isEmpty()) {
            JsonArray rewardsArray = new JsonArray();
            for (SkillRewardConfig reward : skill.rewards()) {
                rewardsArray.add(JsonParser.toJson(reward));
            }
            json.add("rewards", rewardsArray);
        }
        if (skill.cost() != 1) {
            json.addProperty("cost", skill.cost());
        }
        if (skill.requiredSkills() != 1) {
            json.addProperty("required_skills", skill.requiredSkills());
        }
        if (skill.requiredPoints() != 0) {
            json.addProperty("required_points", skill.requiredPoints());
        }
        if (skill.requiredSpentPoints() != 0) {
            json.addProperty("required_spent_points", skill.requiredSpentPoints());
        }
        if (skill.requiredExclusions() != 1) {
            json.addProperty("required_exclusions", skill.requiredExclusions());
        }

        return json;
    }


    public static JsonElement toJson(Identifier id) {
        return new JsonPrimitive(id.toString());
    }

    public static JsonElement toJson(StatusEffect effect) {
        Identifier id = Registries.STATUS_EFFECT.getId(effect);
        if (id == null) {
            throw new IllegalArgumentException("Unregistered StatusEffect: " + effect);
        }
        return new JsonPrimitive(id.toString());
    }

    public static JsonElement toJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        obj.addProperty("item", itemId.toString());
        return obj;
    }

    public static JsonObject toJson(IconConfig config) {
        JsonObject root = new JsonObject();
        switch (config) {
            case IconConfig.ItemIconConfig(ItemStack item) -> {
                root.addProperty("type", "item");
                // You'll need your own util for ItemStack → Json (SkillsMod probably has it)
                JsonElement itemJson = toJson(item);
                root.add("data", itemJson);
            }
            case IconConfig.EffectIconConfig(StatusEffect effect) -> {
                root.addProperty("type", "effect");
                JsonObject data = new JsonObject();
                data.add("effect", toJson(effect));
                root.add("data", data);

            }
            case IconConfig.TextureIconConfig(Identifier texture) -> {
                root.addProperty("type", "texture");
                JsonObject data = new JsonObject();
                data.add("texture", toJson(texture));
                root.add("data", data);

            }
            case null, default -> throw new IllegalArgumentException("Unknown IconConfig type: " + config);
        }

        return root;
    }


    public record SpellContainerReward(List<SpellContainer> containers) implements Reward {
        public static final Identifier ID = Identifier.of("one_piece_api","spell");
        @Override public void update(RewardUpdateContext context) {}
        @Override public void dispose(RewardDisposeContext context) {}
    }
    public static JsonElement toJson(SpellContainerReward reward) {
        Gson gson = new GsonBuilder().create();
        return gson.toJsonTree(reward);
    }


    public static JsonObject toJson(SkillRewardConfig config) {
        JsonObject json = new JsonObject();
        json.addProperty("type", config.type().toString());
        if (!(config.instance() instanceof DummyReward)) {
            if (config.instance() instanceof ExampleSkills.AttributeReward reward) {
                json.add("data", reward.toJson());
            } else if (config.instance() instanceof SpellContainerReward reward) {
                json.add("data", toJson(reward));
            }
        }
        if (config.instance() instanceof DummyReward) {
            json.addProperty("required", false);
        }
        return json;
    }

    public static JsonObject toJson(FrameConfig frameConfig) {
        JsonObject json = new JsonObject();

        if (frameConfig instanceof FrameConfig.AdvancementFrameConfig(AdvancementFrame frame)) {
            json.addProperty("type", "advancement");
            json.addProperty("frame", frame.name().toLowerCase()); // Matches BuiltinJson.parseFrame
        }

        if (frameConfig instanceof FrameConfig.TextureFrameConfig(
                Optional<Identifier> lockedTexture, Identifier availableTexture,
                Optional<Identifier> affordableTexture, Identifier unlockedTexture,
                Optional<Identifier> excludedTexture
        )) {
            json.addProperty("type", "texture");

            lockedTexture.ifPresent(id -> json.addProperty("locked", id.toString()));
            json.addProperty("available", availableTexture.toString());
            affordableTexture.ifPresent(id -> json.addProperty("affordable", id.toString()));
            json.addProperty("unlocked", unlockedTexture.toString());
            excludedTexture.ifPresent(id -> json.addProperty("excluded", id.toString()));
        }

        return json;
    }

}
