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

public class SkillGen implements DataProvider {
    private final DataOutput dataOutput;

    public SkillGen(FabricDataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return writeSkills(writer, createClassSkills());
    }

    private CompletableFuture<?> writeSkills(DataWriter writer, JsonObject skills) {
        Path path = getFilePath();
        return DataProvider.writeToPath(writer, skills, path);
    }

    private JsonObject createClassSkills() {
        JsonObject skills = new JsonObject();
        for (var skill : ExampleSkills.ENTRIES) {
            String hash = DataGenUtil.generateDeterministicId(skill.config().id());
            addSkill(skills, hash, skill.config().id(), skill.x(), skill.y(), skill.isRoot());
        }
        return skills;
    }

    private void addSkill(JsonObject skills, String skillId, String definition, float x, float y, boolean root) {
        JsonObject skill = new JsonObject();
        skill.addProperty("definition", definition);
        skill.addProperty("x", x);
        skill.addProperty("y", y);
        skill.addProperty("root", root);
        skills.add(skillId, skill);
    }
    @Override
    public String getName() {
        return "One Piece RPG/Skill Tree";
    }
    private Path getFilePath() {
        return this.dataOutput.getResolver(DataOutput.OutputType.DATA_PACK, "puffish_skills/categories/" + ExampleMod.CATEGORY_ID.getPath()).resolveJson(Identifier.of(ExampleMod.CATEGORY_ID.getNamespace(), "skills"));
    }

}
