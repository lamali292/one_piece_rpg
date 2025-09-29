package de.one_piece_api.datagen.generator;

import com.google.gson.*;
import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.datagen.JsonHandler;
import de.one_piece_api.util.OnePieceCategory;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.common.BackgroundPosition;
import net.puffish.skillsmod.config.BackgroundConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;
import net.puffish.skillsmod.config.colors.ColorsConfig;
import net.puffish.skillsmod.config.colors.ConnectionsColorsConfig;
import net.puffish.skillsmod.config.colors.FillStrokeColorsConfig;
import net.puffish.skillsmod.config.experience.ExperienceConfig;

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
        var locked = new FillStrokeColorsConfig(
                new ColorConfig(0xFF3A3A3A), new ColorConfig(0xFF3D3D3D)
        );
        var available = new FillStrokeColorsConfig(
                new ColorConfig(0xFF808080), new ColorConfig(0xFF8D8D8D)
        );
        var unlocked = new FillStrokeColorsConfig(
                new ColorConfig(0xFFb37d12), new ColorConfig(0xFFbf8c26)
        );
        var excluded = new FillStrokeColorsConfig(
                new ColorConfig(0xFFb31212), new ColorConfig(0xFFbf2626)
        );
        GeneralConfig config = new GeneralConfig(
                Text.literal("Class Skills"),
                new IconConfig.TextureIconConfig(OnePieceRPG.id("textures/spell/yakkodori")),
                new BackgroundConfig(OnePieceRPG.id("textures/gui/background_one_piece"),
                        768,
                        463,
                        BackgroundPosition.FILL),
                new ColorsConfig(new ConnectionsColorsConfig(locked,
                        available,
                        null,
                        unlocked,
                        excluded), null),
                true,
                3,
                false,
                -1
        );

        return JsonHandler.toJson(config);
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