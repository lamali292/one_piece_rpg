package de.one_piece_content_data.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import de.one_piece_api.config.ConnectionsConfig;
import de.one_piece_api.config.SpellConfig;
import de.one_piece_api.node.SpellContainerReward;
import de.one_piece_content_data.rewards.AttributeReward;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.puffish.skillsmod.reward.builtin.DummyReward;
import net.spell_engine.api.datagen.SpellGenerator;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.fx.Sound;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonParser {


    public static String argbToHex(int argb) {
        int rgb = argb & 0xFFFFFF;
        return String.format("#%06X", rgb);
    }

    public static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Identifier.class, new IdentifierAdapter())
            .registerTypeAdapter(ColorConfig.class, new ColorConfigAdapter())
            .registerTypeAdapter(Text.class, new TextAdapter())
            .registerTypeAdapter(Reward.class, new RewardAdapter())
            .registerTypeAdapter(IconConfig.class, new IconConfigAdapter())
            .registerTypeAdapter(FrameConfig.class, new FrameConfigAdapter())
            .registerTypeAdapter(StatusEffect.class, new StatusEffectAdapter())
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeAdapter(ConnectionsConfig.Connection.class, new SkillConnectionAdapter())
            .registerTypeAdapter(SkillRewardConfig.class, new SkillRewardConfigAdapter())
            .create();


    public static Gson SPELL_GSON = compactGSON();

    private static Gson compactGSON() {
        var gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(SpellConfig.class, new SpellConfigAdapter())
                .registerTypeAdapter(Spell.class, new SpellGenerator.DefaultValueSkippingSerializer<>(Spell.class))
                .registerTypeAdapter(ParticleBatch.class, new SpellGenerator.DefaultValueSkippingSerializer<>(ParticleBatch.class))
                .registerTypeAdapter(Sound.class, new SpellGenerator.DefaultValueSkippingSerializer<>(Sound.class));
        for (var nestedClass : getAllNestedClasses(Spell.class)) {
            gson.registerTypeAdapter(nestedClass, new SpellGenerator.DefaultValueSkippingSerializer<>(nestedClass));
        }
        return gson.create();
    }

    public static class SpellConfigAdapter implements JsonSerializer<SpellConfig> {
        @Override
        public JsonElement serialize(SpellConfig src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.spell(), Spell.class);
        }
    }



    private static List<Class<?>> getAllNestedClasses(Class<?> clazz) {
        List<Class<?>> nestedClasses = new ArrayList<>();
        collectNestedClasses(clazz, nestedClasses);
        return nestedClasses;
    }

    private static void collectNestedClasses(Class<?> clazz, List<Class<?>> nestedClasses) {
        for (Class<?> nested : clazz.getDeclaredClasses()) {
            if (nested.isEnum()) {
                continue; // Skip enums
            }
            if (hasJsonAdapter(nested)) {
                continue; // Skip classes with JsonAdapter
            }
            if (!nested.getPackageName().contains("spell_engine")) {
                continue; // Skip classes outside of spell engine
            }
            nestedClasses.add(nested);
            collectNestedClasses(nested, nestedClasses); // Recursively collect deeper nested classes
        }
    }

    private static boolean hasJsonAdapter(Class<?> clazz) {
        return clazz.isAnnotationPresent(JsonAdapter.class);
    }

    public static class ColorConfigAdapter implements JsonSerializer<ColorConfig> {
        @Override
        public JsonElement serialize(ColorConfig src, Type typeOfSrc, JsonSerializationContext context) {
            return src.argb() == 0 ? JsonNull.INSTANCE : new JsonPrimitive(argbToHex(src.argb()));
        }
    }

    public static class IdentifierAdapter implements JsonSerializer<Identifier> {

        @Override
        public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
            return src.toString().isEmpty() ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }
    }

    static class TextAdapter implements JsonSerializer<Text> {
        @Override
        public JsonElement serialize(Text src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getString());
        }
    }


    static class SkillRewardConfigAdapter implements JsonSerializer<SkillRewardConfig> {

        @Override
        public JsonElement serialize(SkillRewardConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.add("type", GSON.toJsonTree(src.type()));
            json.add("data", GSON.toJsonTree(src.instance(), Reward.class));
            return json;
        }
    }

    static class RewardAdapter implements JsonSerializer<Reward> {
        @Override
        public JsonElement serialize(Reward src, Type typeOfSrc, JsonSerializationContext context) {
            switch (src) {
                case DummyReward ignored -> {
                    return new JsonPrimitive("dummy");
                }
                case SpellContainerReward reward -> {
                    return reward.toJson();
                }
                case AttributeReward reward -> {
                    return reward.toJson();
                }
                case null, default -> throw new IllegalArgumentException("Unknown Reward type: " + src);
            }
        }
    }

    static class IconConfigAdapter implements JsonSerializer<IconConfig> {
        @Override
        public JsonElement serialize(IconConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject root = new JsonObject();
            switch (src) {
                case IconConfig.ItemIconConfig(ItemStack item) -> {
                    root.addProperty("type", "item");
                    // You'll need your own util for ItemStack → Json (SkillsMod probably has it)
                    JsonElement itemJson = GSON.toJsonTree(item);
                    root.add("data", itemJson);
                }
                case IconConfig.EffectIconConfig(StatusEffect effect) -> {
                    root.addProperty("type", "effect");
                    JsonObject data = new JsonObject();
                    data.add("effect", GSON.toJsonTree(effect));
                    root.add("data", data);

                }
                case IconConfig.TextureIconConfig(Identifier texture) -> {
                    root.addProperty("type", "texture");
                    JsonObject data = new JsonObject();
                    data.add("texture", GSON.toJsonTree(texture));
                    root.add("data", data);

                }
                case null, default -> throw new IllegalArgumentException("Unknown IconConfig type: " + src);
            }

            return root;
        }
    }

    static class FrameConfigAdapter implements JsonSerializer<FrameConfig> {

        @Override
        public JsonElement serialize(FrameConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            if (src instanceof FrameConfig.AdvancementFrameConfig(AdvancementFrame frame)) {
                json.addProperty("type", "advancement");
                json.addProperty("frame", frame.name().toLowerCase()); // Matches BuiltinJson.parseFrame
            }

            if (src instanceof FrameConfig.TextureFrameConfig(
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

    static class StatusEffectAdapter implements JsonSerializer<StatusEffect> {
        @Override
        public JsonElement serialize(StatusEffect src, Type typeOfSrc, JsonSerializationContext context) {
            Identifier id = Registries.STATUS_EFFECT.getId(src);
            if (id == null) {
                throw new IllegalArgumentException("Unregistered StatusEffect: " + src);
            }
            return new JsonPrimitive(id.toString());
        }
    }

    static class ItemStackAdapter implements JsonSerializer<ItemStack> {
        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            Identifier itemId = Registries.ITEM.getId(src.getItem());
            obj.addProperty("item", itemId.toString());
            return obj;
        }
    }

    static class SkillConnectionAdapter implements JsonSerializer<ConnectionsConfig.Connection> {
        @Override
        public JsonElement serialize(ConnectionsConfig.Connection src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("source", src.sourceSkill().toString());
            obj.addProperty("target", src.targetSkill().toString());
            src.styleId().ifPresent(id -> obj.addProperty("style", id.toString()));
            return obj;
        }
    }



}
