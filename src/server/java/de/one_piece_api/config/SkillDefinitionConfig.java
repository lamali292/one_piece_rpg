package de.one_piece_api.config;

import net.minecraft.text.Text;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;

import java.util.List;

public record SkillDefinitionConfig(Text title,
                                    Text description,
                                    Text extraDescription,
                                    IconConfig icon,
                                    FrameConfig frame,
                                    float size,
                                    List<SkillRewardConfig> rewards,
                                    int cost,
                                    int required_skills,
                                    int required_points,
                                    int required_spent_points,
                                    int required_exclusions) {


    public static Result<net.puffish.skillsmod.config.skill.SkillDefinitionConfig, Problem> parse(JsonElement jsonElement, ConfigContext configContext) {
        String path = jsonElement.getPath().toString();
        String cleanPath = path.replaceAll("[´`'\"]", "");
        String[] parts = cleanPath.split(":", 2);
        String namespace = parts[0];
        String pathStr = parts[1];
        int lastSlash = pathStr.lastIndexOf('/');
        int dotJson = pathStr.lastIndexOf(".json");
        String skillName = pathStr.substring(lastSlash + 1, dotJson);
        String id = namespace + ":" + skillName;


        return net.puffish.skillsmod.config.skill.SkillDefinitionConfig.parse(id, jsonElement, configContext);
    }
}
