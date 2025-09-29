
package de.one_piece_content_data.datagen;

import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.content.ExampleConnections;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class SkillConnectionsGen implements DataProvider {
    private final DataOutput dataOutput;

    public SkillConnectionsGen(FabricDataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Path path = getFilePath();
        return DataProvider.writeToPath(writer, ExampleConnections.createClassConnections(), path);
    }

    @Override
    public String getName() {
        return "One Piece RPG / Skill Connections";
    }

    private Path getFilePath() {
        return this.dataOutput.getResolver(DataOutput.OutputType.DATA_PACK, "puffish_skills/categories/" + ExampleMod.CATEGORY_ID.getPath()).resolveJson(Identifier.of(ExampleMod.CATEGORY_ID.getNamespace(), "connections"));
    }
}