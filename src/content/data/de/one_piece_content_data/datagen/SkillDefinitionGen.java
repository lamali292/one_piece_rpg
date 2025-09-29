package de.one_piece_content_data.datagen;

import com.google.gson.JsonObject;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.content.ExampleSkills;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class SkillDefinitionGen implements DataProvider {
    private final DataOutput output;

    public SkillDefinitionGen(FabricDataOutput dataOutput) {
        this.output = dataOutput;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Path path = getFilePath();
        JsonObject json = JsonParser.toJson(ExampleSkills.ENTRIES);
        return DataProvider.writeToPath(writer, json, path);
    }

    @Override
    public String getName() {
        return "Skill Definition Generator";
    }

    private Path getFilePath() {
        return this.output.getResolver(DataOutput.OutputType.DATA_PACK, "puffish_skills/categories/" + ExampleMod.CATEGORY_ID.getPath()).resolveJson(Identifier.of(ExampleMod.CATEGORY_ID.getNamespace(), "definitions"));
    }
}
