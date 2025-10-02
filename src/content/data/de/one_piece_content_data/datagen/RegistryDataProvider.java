package de.one_piece_content_data.datagen;

import com.google.gson.Gson;
import de.one_piece_content_data.registry.DataRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public record RegistryDataProvider<T>(FabricDataOutput output,
                                      DataRegistry<T> registry,
                                      String folder,
                                      Gson gson) implements DataProvider {


    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.allOf(
                registry.entries().entrySet().stream()
                        .map(entry -> DataProvider.writeToPath(writer, gson.toJsonTree(entry.getValue()), getFilePath(entry.getKey())))
                        .toArray(CompletableFuture[]::new)
        );
    }

    @Override
    public String getName() {
        return "One Piece Content" +  this.folder + " Registry Data Provider";
    }

    private Path getFilePath(Identifier id) {
        return output.getResolver(DataOutput.OutputType.DATA_PACK, this.folder).resolveJson(id);
    }
}