package de.one_piece_api.data.loader;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.*;
import de.one_piece_api.config.attribute.AttributeScalingConfig;
import de.one_piece_api.config.skill.ConnectionsConfig;
import de.one_piece_api.config.skill.SkillTreeEntryConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;

import java.util.ArrayList;
import java.util.List;

public class DataLoaders {

    private static final List<BaseLoader<?>> LOADER_LIST = new ArrayList<>();

    // Directory loaders - scan all .json files in the folder
    public static final MultiFileLoader<DevilFruitConfig> DEVIL_FRUIT_LOADER = add(new MultiFileLoader<>("devil_fruit", DevilFruitConfig::parse));
    public static final MultiFileLoader<ClassConfig> CLASS_LOADER = add(new MultiFileLoader<>("one_piece_class", ClassConfig::parse));
    public static final MultiFileLoader<StyleConfig> STYLE_LOADER = add(new MultiFileLoader<>("styles", StyleConfig::parse));
    public static final MultiFileLoader<SkillDefinitionConfig> SKILL_DEFINITION_LOADER = add(new MultiFileLoader<>("skill_definition", de.one_piece_api.config.skill.SkillDefinitionConfig::parse));
    public static final MultiFileLoader<SkillConnectionsConfig> CONNECTIONS_LOADER = add(new MultiFileLoader<>("connections", ConnectionsConfig::parse2));
    public static final MultiFileLoader<SkillsConfig> SKILL_LOADER = add(new MultiFileLoader<>("skill_tree", SkillTreeEntryConfig::parse));

    // Single file loader - loads only one specific file
    public static final SingleFileLoader<AttributeScalingConfig> ATTRIBUTE_SCALING =
            add(new SingleFileLoader<>(
                    OnePieceRPG.id("curve.json"),
                    AttributeScalingConfig::parse
            ));

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOADER_LIST.forEach(loader -> {
                loader.setServer(server);
                loader.reload(server.getResourceManager());
            });
        });

        LOADER_LIST.forEach(loader -> {
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(loader);
        });
    }

    public static <T extends BaseLoader<?>> T add(T loader) {
        LOADER_LIST.add(loader);
        return loader;
    }
}