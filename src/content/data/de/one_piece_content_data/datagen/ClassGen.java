package de.one_piece_content_data.datagen;

import com.google.gson.JsonObject;
import de.one_piece_content_data.content.ExampleClasses;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ClassGen implements DataProvider {
    private final DataOutput output;

    public ClassGen(FabricDataOutput dataOutput) {
        this.output = dataOutput;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.allOf(
                ExampleClasses.ENTRIES.stream()
                        .map(entry -> {
                            JsonObject json = JsonParser.toJson(entry.config());
                            return DataProvider.writeToPath(writer, json, getFilePath(entry.id()));
                        })
                        .toArray(CompletableFuture[]::new)
        );
    }

    @Override
    public String getName() {
        return "One Piece Class Generator";
    }

    private Path getFilePath(Identifier spellId) {
        return this.output.getResolver(DataOutput.OutputType.DATA_PACK, "one_piece_class").resolveJson(spellId);
    }

}
