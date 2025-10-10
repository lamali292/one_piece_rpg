package de.one_piece_api.datagen.generator;

import com.google.gson.*;
import de.one_piece_api.data.loader.CategoryLoader;
import de.one_piece_api.datagen.JsonHandler;
import de.one_piece_api.util.OnePieceCategory;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class SkillCategoryGen implements DataProvider {
    protected final FabricDataOutput dataOutput;

    public SkillCategoryGen(FabricDataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.allOf(
                writeCategory(writer, OnePieceCategory.ID, createClassSkillsCategory())
        );
    }

    private CompletableFuture<?> writeCategory(DataWriter writer, Identifier id, JsonObject category) {
        Path path = getFilePath(id);
        return DataProvider.writeToPath(writer, category, path);
    }

    private JsonObject createClassSkillsCategory() {
        return JsonHandler.toJson(CategoryLoader.buildGeneralConfig());
    }

    private void addConnectionColor(JsonObject connections, String state, String fill, String stroke) {
        JsonObject stateColor = new JsonObject();
        stateColor.addProperty("fill", fill);
        stateColor.addProperty("stroke", stroke);
        connections.add(state, stateColor);
    }

    @Override
    public String getName() {
        return "One Piece RPG/Skill Categories";
    }

    private Path getFilePath(Identifier category) {
        return this.dataOutput.getResolver(DataOutput.OutputType.DATA_PACK, "puffish_skills/categories/" + category.getPath()).resolveJson(Identifier.of(category.getNamespace(), "category"));
    }
}