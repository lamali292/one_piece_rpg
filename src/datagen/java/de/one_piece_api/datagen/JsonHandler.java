package de.one_piece_api.datagen;

import com.google.gson.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.common.BackgroundPosition;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;
import net.puffish.skillsmod.config.experience.ExperienceConfig;

import java.lang.reflect.Type;

public class JsonHandler {
    static class ColorConfigAdapter implements JsonSerializer<ColorConfig> {
        @Override
        public JsonElement serialize(ColorConfig src, Type typeOfSrc, JsonSerializationContext context) {
            int rgb = src.argb() & 0xFFFFFF;
            String hex = String.format("#%06x", rgb);
            return new JsonPrimitive(hex);
        }
    }

    static class TextAdapter implements JsonSerializer<Text> {
        @Override
        public JsonElement serialize(Text src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getString());
        }
    }

    static class IdentifierAdapter implements JsonSerializer<Identifier> {
        @Override
        public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getNamespace() + ":" + src.getPath() + ".png");
        }
    }

    static class TextureConfigAdapter implements JsonSerializer<IconConfig.TextureIconConfig> {
        @Override
        public JsonElement serialize(IconConfig.TextureIconConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject texture = new JsonObject();
            texture.addProperty("type", "texture");
            JsonObject textureData = new JsonObject();
            textureData.add("texture", gson.toJsonTree(src.texture()));
            texture.add("data", textureData);
            return texture;
        }
    }

    static class BackgroundPositionAdapter implements JsonSerializer<BackgroundPosition> {
        @Override
        public JsonElement serialize(BackgroundPosition src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.name().toLowerCase());
        }
    }

    static class GeneralConfigAdapter implements JsonSerializer<GeneralConfig>{

        @Override
        public JsonElement serialize(GeneralConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject general = new JsonObject();
            general.add("title", context.serialize(src.title()));
            general.add("icon", context.serialize(src.icon()));
            general.add("unlocked_by_default", context.serialize(src.unlockedByDefault()));
            if (src.spentPointsLimit() >= 0) {
                general.add("spent_point_limit", context.serialize(src.spentPointsLimit()));
            }
            general.add("exclusive_root", context.serialize(src.exclusiveRoot()));
            general.add("background", context.serialize(src.background()));
            general.add("colors", context.serialize(src.colors()));
            if (src.startingPoints() > 0) {
                general.add("starting_points", context.serialize(src.startingPoints()));
            }
            return general;
        }
    }

    public static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ColorConfig.class, new ColorConfigAdapter())
            .registerTypeHierarchyAdapter(Text.class, new TextAdapter())
            .registerTypeAdapter(Identifier.class, new IdentifierAdapter())
            .registerTypeAdapter(IconConfig.TextureIconConfig.class, new TextureConfigAdapter())
            .registerTypeAdapter(BackgroundPosition.class, new BackgroundPositionAdapter())
            .registerTypeAdapter(GeneralConfig.class, new GeneralConfigAdapter())
            .create();


    public static JsonObject toJson(Record config) {
        JsonElement element = gson.toJsonTree(config);
        return element.getAsJsonObject();
    }
}
