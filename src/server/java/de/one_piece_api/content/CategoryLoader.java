package de.one_piece_api.content;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.config.DevilFruitPathConfig;
import de.one_piece_api.experience.ItemExperienceSource;
import de.one_piece_api.experience.TimeExperienceSource;
import de.one_piece_api.interfaces.ICategoryAccessor;
import de.one_piece_api.interfaces.IHidden;
import de.one_piece_api.registries.MyDataComponentTypes;
import de.one_piece_api.util.DataGenUtil;
import de.one_piece_api.util.OnePieceCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.common.BackgroundPosition;
import net.puffish.skillsmod.common.SkillConnection;
import net.puffish.skillsmod.config.*;
import net.puffish.skillsmod.config.colors.*;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.experience.ExperienceSourceConfig;
import net.puffish.skillsmod.config.skill.*;
import net.puffish.skillsmod.experience.ExperienceCurve;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class CategoryLoader {

    private static final int MAX_LEVEL = 20000;
    private static final double LEVEL_BASE_MULTIPLIER = 8.0;
    private static final double LEVEL_EXPONENT = 1.3;
    private static final int LEVEL_BASE_XP = 100;

    // Skill Collection

    private static List<SkillConfig> collectSkillsFromFruit(List<Identifier> skillIds) {
        List<SkillConfig> skillConfigs = new ArrayList<>();
        for (Identifier skillId : skillIds) {
            String id = DataGenUtil.generateDeterministicId(skillId);
            skillConfigs.add(new SkillConfig(id, 0, 0, skillId.toString(), true));
        }
        return skillConfigs;
    }

    // Merging Methods

    private static SkillDefinitionsConfig mergeDefinitions(Map<Identifier, SkillDefinitionConfig> definitions) {
        Map<String, SkillDefinitionConfig> stringMap = definitions.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        Map.Entry::getValue
                ));
        return createInstance(SkillDefinitionsConfig.class, Map.class, stringMap);
    }

    private static SkillsConfig mergeSkills(Map<Identifier, SkillsConfig> skills) {
        Map<String, SkillConfig> mergedMap = new HashMap<>();
        skills.values().forEach(config -> mergedMap.putAll(config.getMap()));
        return new SkillsConfig(mergedMap);
    }

    private static SkillConnectionsConfig mergeConnections(Map<Identifier, SkillConnectionsConfig> connections) {
        List<SkillConnection> normalConnections = new ArrayList<>();
        List<SkillConnection> exclusiveConnections = new ArrayList<>();

        connections.values().forEach(config -> {
            normalConnections.addAll(config.normal().getAll());
            exclusiveConnections.addAll(config.exclusive().getAll());
        });

        SkillConnectionsGroupConfig normalGroup = createConnectionGroup(normalConnections);
        SkillConnectionsGroupConfig exclusiveGroup = createConnectionGroup(exclusiveConnections);

        return new SkillConnectionsConfig(normalGroup, exclusiveGroup);
    }

    // Connection Helpers

    private static SkillConnectionsGroupConfig createConnectionGroup(List<SkillConnection> connections) {
        Map<String, Collection<String>> neighbors = buildNeighborsMap(connections);
        return createInstance(SkillConnectionsGroupConfig.class,
                new Class<?>[]{List.class, Map.class},
                connections, neighbors);
    }

    private static Map<String, Collection<String>> buildNeighborsMap(List<SkillConnection> connections) {
        Map<String, Collection<String>> neighbors = new HashMap<>();

        for (SkillConnection conn : connections) {
            if (conn.bidirectional()) {
                neighbors.computeIfAbsent(conn.skillAId(), k -> new HashSet<>()).add(conn.skillBId());
                neighbors.computeIfAbsent(conn.skillBId(), k -> new HashSet<>()).add(conn.skillAId());
            } else {
                neighbors.computeIfAbsent(conn.skillBId(), k -> new HashSet<>()).add(conn.skillAId());
            }
        }
        return neighbors;
    }

    // Reflection Utility

    private static <T> T createInstance(Class<T> clazz, Class<?> paramType, Object param) {
        return createInstance(clazz, new Class<?>[]{paramType}, param);
    }

    private static <T> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object... params) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getSimpleName(), e);
        }
    }

    // Experience Configuration

    private static int calculateLevelCurve(int level) {
        return (int) Math.pow(LEVEL_BASE_MULTIPLIER * level, LEVEL_EXPONENT) + LEVEL_BASE_XP;
    }

    private static ExperienceConfig buildExperienceConfig() {
        ExperienceCurve curve = ExperienceCurve.create(CategoryLoader::calculateLevelCurve, MAX_LEVEL);

        ExperienceSourceConfig timeSource = createTimeExperienceSource();
        ExperienceSourceConfig itemSource = createItemExperienceSource();

        return new ExperienceConfig(curve, List.of(timeSource, itemSource));
    }

    private static ExperienceSourceConfig createTimeExperienceSource() {
        Calculation<TimeExperienceSource.Data> calculation = data -> (double) data.ticks();
        TimeExperienceSource source = new TimeExperienceSource(calculation);
        return new ExperienceSourceConfig(TimeExperienceSource.ID, source, Optional.empty());
    }

    private static ExperienceSourceConfig createItemExperienceSource() {
        Calculation<ItemExperienceSource.Data> calculation = data -> {
            int xp = data.item().getOrDefault(MyDataComponentTypes.XP, 0);
            int count = data.item().getCount();
            return (double) (xp * count);
        };
        ItemExperienceSource source = new ItemExperienceSource(calculation);
        return new ExperienceSourceConfig(ItemExperienceSource.ID, source, Optional.empty());
    }

    // General Configuration

    public static GeneralConfig buildGeneralConfig() {
        FillStrokeColorsConfig locked = new FillStrokeColorsConfig(
                new ColorConfig(0xFF3A3A3A), new ColorConfig(0xFF3D3D3D));
        FillStrokeColorsConfig available = new FillStrokeColorsConfig(
                new ColorConfig(0xFF808080), new ColorConfig(0xFF808080));
        FillStrokeColorsConfig unlocked = new FillStrokeColorsConfig(
                new ColorConfig(0xFFb37d12), new ColorConfig(0xFFbf8c26));
        FillStrokeColorsConfig affordable = new FillStrokeColorsConfig(
                new ColorConfig(0xFF808080), new ColorConfig(0xFF808080));
        FillStrokeColorsConfig excluded = new FillStrokeColorsConfig(
                new ColorConfig(0xFFb31212), new ColorConfig(0xFFbf2626));
        FillStrokeColorsConfig points =
                new FillStrokeColorsConfig(new ColorConfig(-8323296), new ColorConfig(-16777216));
        ConnectionsColorsConfig connectionColors = new ConnectionsColorsConfig(
                locked, available, affordable, unlocked, excluded);
        ColorsConfig colors = new ColorsConfig(connectionColors, points);

        BackgroundConfig background = new BackgroundConfig(
                OnePieceRPG.id("textures/gui/background_one_piece"),
                768, 463, BackgroundPosition.FILL);

        IconConfig icon = new IconConfig.TextureIconConfig(
                OnePieceRPG.id("textures/spell/yakkodori"));

        return new GeneralConfig(
                Text.literal("Class Skills"),
                icon,
                background,
                colors,
                true,
                3,
                false,
                Integer.MAX_VALUE
        );
    }

    // Category Building

    public static CategoryConfig buildCategory(
            Map<Identifier, SkillConnectionsConfig> connections,
            Map<Identifier, SkillDefinitionConfig> definitions,
            Map<Identifier, DevilFruitConfig> devilFruits,
            Map<Identifier, SkillsConfig> skills
    ) {
        processDevilFruits(devilFruits, skills);

        SkillDefinitionsConfig definitionsConfig = mergeDefinitions(definitions);
        SkillsConfig skillsConfig = mergeSkills(skills);
        SkillConnectionsConfig connectionsConfig = mergeConnections(connections);
        GeneralConfig generalConfig = buildGeneralConfig();
        ExperienceConfig experienceConfig = buildExperienceConfig();

        return new CategoryConfig(
                OnePieceCategory.ID,
                generalConfig,
                definitionsConfig,
                skillsConfig,
                connectionsConfig,
                Optional.of(experienceConfig)
        );
    }

    private static void processDevilFruits(
            Map<Identifier, DevilFruitConfig> devilFruits,
            Map<Identifier, SkillsConfig> skills
    ) {
        devilFruits.forEach((fruitId, fruitConfig) -> {
            Map<String, SkillConfig> skillConfigs = fruitConfig.paths().stream()
                    .map(DevilFruitPathConfig::skills)
                    .flatMap(skillIds -> collectSkillsFromFruit(skillIds).stream())
                    .peek(CategoryLoader::markSkillAsHidden)
                    .collect(Collectors.toMap(
                            SkillConfig::id,
                            skill -> skill,
                            (existing, duplicate) -> existing.isRoot() ? existing : duplicate
                    ));

            skills.put(fruitId, new SkillsConfig(skillConfigs));
        });
    }

    private static void markSkillAsHidden(SkillConfig skillConfig) {
        IHidden hidden = (IHidden) (Object) skillConfig;

        if (hidden != null) {
            hidden.onepiece$setHidden(true);
        }
    }

    // Category Injection

    public static void addCategory(Identifier id, CategoryConfig config) {
        SkillsMod mod = SkillsMod.getInstance();
        ICategoryAccessor accessor = (ICategoryAccessor) mod;

        net.puffish.skillsmod.util.ChangeListener<Optional<Map<Identifier, CategoryConfig>>> categories =
                accessor.getCategories();
        Optional<Map<Identifier, CategoryConfig>> current = categories.get();

        Map<Identifier, CategoryConfig> updated = current
                .map(HashMap::new)
                .orElseGet(HashMap::new);
        updated.put(id, config);

        Optional<Map<Identifier, CategoryConfig>> newCategories = Optional.of(updated);
        categories.set(newCategories, () -> categories.set(current, () -> {}));
    }
}