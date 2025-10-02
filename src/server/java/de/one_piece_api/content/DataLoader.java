package de.one_piece_api.content;

import de.one_piece_api.config.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;

import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    private static final List<GenericLoader<?>> LOADER_LIST = new ArrayList<>();
    public static final GenericLoader<DevilFruitConfig> DEVIL_FRUIT_LOADER = add(new GenericLoader<>("devil_fruit", DevilFruitConfig::parse));
    public static final GenericLoader<ClassConfig> CLASS_LOADER = add(new GenericLoader<>("one_piece_class", ClassConfig::parse));
    public static final GenericLoader<StyleConfig> STYLE_LOADER = add(new GenericLoader<>("styles", StyleConfig::parse));
    public static final GenericLoader<SkillDefinitionConfig> SKILL_DEFINITION_LOADER = add(new GenericLoader<>("skill_definition", de.one_piece_api.config.SkillDefinitionConfig::parse));
    public static final GenericLoader<SkillConnectionsConfig> CONNECTIONS_LOADER = add(new GenericLoader<>("connections", ConnectionsConfig::parse2));
    public static final GenericLoader<SkillsConfig> SKILL_LOADER = add(new GenericLoader<>("skill_tree", SkillTreeEntryConfig::parse));



    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server-> {
            LOADER_LIST.forEach(loader -> {
                loader.setServer(server);
                loader.reload(server.getResourceManager());
            });
        });
        LOADER_LIST.forEach(loader -> {
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(loader);
        });


    }

    public static <T> GenericLoader<T> add(GenericLoader<T> loader) {
        LOADER_LIST.add(loader);
        return loader;
    }
}
