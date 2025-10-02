package de.one_piece_content_data.content;

import de.one_piece_api.node.SpellContainerReward;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.data.Icon;
import de.one_piece_api.config.SkillDefinitionConfig;
import de.one_piece_api.config.SpellConfig;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.registry.Registries;
import de.one_piece_content_data.rewards.AttributeReward;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.container.SpellContainerHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExampleSkillDefinitions {


    public static void init() {
    }


    public static Entry<SkillDefinitionConfig> dummySpell(Identifier spellId) {
        SpellContainer container = SpellContainerHelper.createForMeleeWeapon(spellId);
        SkillRewardConfig reward = new SkillRewardConfig(SpellContainerReward.ID, new SpellContainerReward(null, List.of(container)));

        var spell = Registries.SPELLS.entries().entrySet().stream().filter(e -> e.getKey().equals(spellId)).findFirst().map(Map.Entry::getValue);
        String description = spell.map(SpellConfig::description).orElse("...");
        String title = spell.map(SpellConfig::title).orElse("Unlock " + spellId);

        var config = new SkillDefinitionConfig(
                Text.literal(title),
                Text.literal(description),
                null,
                Icon.spell(spellId),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return Registries.SKILL_DEFINITION.register(spellId, config);
    }


    public static final HashMap<String, Entry<SkillDefinitionConfig>> DUMMY_DEFINITIONS =  ExampleSpells.DUMMY_SPELLS.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> dummySpell(entry.getValue().id()),
                    (a, b) -> a,  // merge function (in case of duplicates)
                    HashMap::new
            ));
    public static final Entry<SkillDefinitionConfig> SANDSTORM =  dummySpell(ExampleSpells.SANDSTORM.id());
    public static final Entry<SkillDefinitionConfig> SANDSTORM_MODIFIER_1 = dummySpell(ExampleSpells.SANDSTORM_MODIFIER_1.id());

    public static final Entry<SkillDefinitionConfig> SWORDSMEN_SKILL_1 = Registries.SKILL_DEFINITION.register(ExampleMod.id("swordsmen_skill_1"), swordsmenSkill1());
    public static SkillDefinitionConfig swordsmenSkill1() {
        Identifier spellId = ExampleMod.id("shishi_sonson");
        SpellContainer container = SpellContainerHelper.createForMeleeWeapon(spellId);
        SkillRewardConfig reward = new SkillRewardConfig(SpellContainerReward.ID, new SpellContainerReward(null, List.of(container)));
        return new SkillDefinitionConfig(Text.literal("Unlock ShiShi Sonson"),
                Text.literal("Channels a fast, focused dash for 1 second, moving 5 blocks forward and slashing any target in the path"),
                null,
                Icon.spell(spellId),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> SWORDSMEN_SKILL_2 = Registries.SKILL_DEFINITION.register(ExampleMod.id("swordsmen_skill_2"),  swordsmenSkill2());
    public static SkillDefinitionConfig swordsmenSkill2() {
        Identifier spellId = ExampleMod.id("yakkodori");
        SpellContainer container = SpellContainerHelper.createForMeleeWeapon(spellId);
        SkillRewardConfig reward = new SkillRewardConfig(SpellContainerReward.ID, new SpellContainerReward(null, List.of(container)));
        return new SkillDefinitionConfig(Text.literal("Unlock Yakkodori"),
                Text.literal("[Yakkodori Description text]"),
                null,
                Icon.spell(spellId),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> SWORDSMEN_SKILL_3 = Registries.SKILL_DEFINITION.register(ExampleMod.id("swordsmen_skill_3"), swordsmenSkill3());

    public static SkillDefinitionConfig swordsmenSkill3() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.08f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
       return new SkillDefinitionConfig(Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.DIAMOND_SWORD),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> REQUIRES_BOTH = Registries.SKILL_DEFINITION.register(ExampleMod.id("requires_both"), requiresBoth());
    public static SkillDefinitionConfig requiresBoth() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.08f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        return new SkillDefinitionConfig( Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.DIAMOND),
                null,
                1,
                List.of(reward),
                1, 2, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> BRAWLER_SKILL_1 = Registries.SKILL_DEFINITION.register(ExampleMod.id("brawler_skill_1"), brawlerSkill1());

    public static SkillDefinitionConfig brawlerSkill1() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.08f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        return new SkillDefinitionConfig( Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.IRON_INGOT),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> BRAWLER_SKILL_2 = Registries.SKILL_DEFINITION.register(ExampleMod.id("brawler_skill_2"), brawlerSkill2());

    public static SkillDefinitionConfig brawlerSkill2() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                0.15f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        return new SkillDefinitionConfig(Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.effect(StatusEffects.STRENGTH.value()),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }


    public static final Entry<SkillDefinitionConfig> BRAWLER_SKILL_3 = Registries.SKILL_DEFINITION.register(ExampleMod.id("brawler_skill_3"), brawlerSkill3());

    public static SkillDefinitionConfig brawlerSkill3() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ARMOR,
                2.0f,
                EntityAttributeModifier.Operation.ADD_VALUE
        ));
        return new SkillDefinitionConfig(Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.effect(StatusEffects.STRENGTH.value()),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> SNIPER_SKILL_1 =Registries.SKILL_DEFINITION.register(ExampleMod.id("sniper_skill_1"), sniperSkill1());

    public static SkillDefinitionConfig sniperSkill1() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_LUCK,
                0.25f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        return new SkillDefinitionConfig(Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.SPYGLASS),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> SNIPER_SKILL_2 = Registries.SKILL_DEFINITION.register(ExampleMod.id("sniper_skill_2"), sniperSkill2());

    public static SkillDefinitionConfig sniperSkill2() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                -0.05f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        return new SkillDefinitionConfig(Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.SPYGLASS),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }

    public static final Entry<SkillDefinitionConfig> SNIPER_SKILL_3 = Registries.SKILL_DEFINITION.register(ExampleMod.id("sniper_skill_3"), sniperSkill3());

    public static SkillDefinitionConfig sniperSkill3() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.15f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        return new SkillDefinitionConfig( Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.CROSSBOW),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
    }


}
