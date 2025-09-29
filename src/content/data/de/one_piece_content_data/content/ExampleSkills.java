package de.one_piece_content_data.content;

import com.google.gson.JsonObject;
import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.datagen.JsonParser;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillRewardConfig;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.container.SpellContainerHelper;

import java.util.ArrayList;
import java.util.List;

public class ExampleSkills {

    public static class Icon {
        public static IconConfig texture(Identifier texture) {
            return new IconConfig.TextureIconConfig(texture);
        }

        public static IconConfig item(Item item) {
            return new IconConfig.ItemIconConfig(new ItemStack(item));
        }

        public static IconConfig effect(StatusEffect effect) {
            return new IconConfig.EffectIconConfig(effect);
        }

        public static IconConfig spell(Identifier spellId) {
            return texture(Identifier.of(spellId.getNamespace(), "textures/spell/" + spellId.getPath() + ".png"));
        }
    }

    public record AttributeReward(RegistryEntry<EntityAttribute> attribute, float value,
                                  EntityAttributeModifier.Operation operation) implements Reward {
        public static final Identifier ID = SkillsMod.createIdentifier("attribute");
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("attribute", attribute.getKey().get().getValue().toString());
            json.addProperty("value", value);
            String opString = switch (operation) {
                case ADD_VALUE -> "addition";
                case ADD_MULTIPLIED_BASE -> "multiply_base";
                case ADD_MULTIPLIED_TOTAL -> "multiply_total";
            };
            json.addProperty("operation", opString);
            return json;
        }
        @Override
        public void update(RewardUpdateContext context) {
        }

        @Override
        public void dispose(RewardDisposeContext context) {
        }
    }

    public static final ArrayList<SkillDefinition> ENTRIES = new ArrayList<>();

    private static SkillDefinition add(SkillDefinition entry) {
        ENTRIES.add(entry);
        return entry;
    }

    public record SkillDefinition(SkillDefinitionConfig config, boolean isRoot, int x, int y) {
    }

    public static String titleTranslationKey(String id) {
        return "skill." + ExampleMod.MOD_ID + "." + id + ".title";
    }

    public static String descriptionTranslationKey(String id) {
        return "skill." + ExampleMod.MOD_ID + "." + id + ".description";
    }


    public static SkillDefinition dummySpell(Identifier spellId) {
        SpellContainer container = SpellContainerHelper.createForMeleeWeapon(spellId);
        SkillRewardConfig reward = new SkillRewardConfig(JsonParser.SpellContainerReward.ID, new JsonParser.SpellContainerReward(List.of(container)));

        var spell = ExampleSpells.ENTRIES.stream().filter(e -> e.id().equals(spellId)).findFirst();
        // Same Title and Description as spell or a custom one:
        String description = spell.map(ExampleSpells.Entry::description).orElse("...");
        String title = spell.map(ExampleSpells.Entry::title).orElse("Unlock " + spellId);

        var config = new SkillDefinitionConfig(
                spellId.getPath(),
                Text.literal(title),
                Text.literal(description),
                null,
                Icon.spell(spellId),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, true, 0, 0);
    }

    static {
        ExampleSpells.DUMMY_SPELLS.forEach(
                e -> add(dummySpell(e.id()))
        );
        add(dummySpell(ExampleSpells.SANDSTORM.id()));
        add(dummySpell(ExampleSpells.SANDSTORM_MODIFIER_1.id()));
    }

    public static final SkillDefinition SWORDSMEN_SKILL_1 = add(swordsmenSkill1());

    public static SkillDefinition swordsmenSkill1() {
        Identifier spellId = ExampleMod.id("shishi_sonson");

        SpellContainer container = SpellContainerHelper.createForMeleeWeapon(spellId);
        SkillRewardConfig reward = new SkillRewardConfig(JsonParser.SpellContainerReward.ID, new JsonParser.SpellContainerReward(List.of(container)));
        var config = new SkillDefinitionConfig("swordsmen_skill_1", Text.literal("Unlock ShiShi Sonson"),
                Text.literal("Channels a fast, focused dash for 1 second, moving 5 blocks forward and slashing any target in the path"),
                null,
                Icon.spell(spellId),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, true, -70, -40);
    }

    public static final SkillDefinition SWORDSMEN_SKILL_2 = add(swordsmenSkill2());

    public static SkillDefinition swordsmenSkill2() {
        Identifier spellId = ExampleMod.id("yakkodori");
        SpellContainer container = SpellContainerHelper.createForMeleeWeapon(spellId);
        SkillRewardConfig reward = new SkillRewardConfig(JsonParser.SpellContainerReward.ID, new JsonParser.SpellContainerReward(List.of(container)));
        var config = new SkillDefinitionConfig("swordsmen_skill_2", Text.literal("Unlock Yakkodori"),
                Text.literal("[Yakkodori Description text]"),
                null,
                Icon.spell(spellId),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, false, -138, 0);
    }

    public static final SkillDefinition SWORDSMEN_SKILL_3 = add(swordsmenSkill3());

    public static SkillDefinition swordsmenSkill3() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.08f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        var config = new SkillDefinitionConfig("swordsmen_skill_3", Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.DIAMOND_SWORD),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, false, -70, -120);
    }

    public static final SkillDefinition BRAWLER_SKILL_1 = add(brawlerSkill1());

    public static SkillDefinition brawlerSkill1() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.08f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        var config = new SkillDefinitionConfig("brawler_skill_1", Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.IRON_INGOT),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, true, 70, -40);
    }

    public static final SkillDefinition BRAWLER_SKILL_2 = add(brawlerSkill2());

    public static SkillDefinition brawlerSkill2() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                0.15f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        var config = new SkillDefinitionConfig("brawler_skill_2", Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.effect(StatusEffects.STRENGTH.value()),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, false, 130, 0);
    }

    public static final SkillDefinition BRAWLER_SKILL_3 = add(brawlerSkill3());

    public static SkillDefinition brawlerSkill3() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ARMOR,
                2.0f,
                EntityAttributeModifier.Operation.ADD_VALUE
        ));
        var config = new SkillDefinitionConfig("brawler_skill_3", Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.effect(StatusEffects.STRENGTH.value()),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, false, 70, -120);
    }

    public static final SkillDefinition SNIPER_SKILL_1 = add(sniperSkill1());

    public static SkillDefinition sniperSkill1() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_LUCK,
                0.25f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        var config = new SkillDefinitionConfig("sniper_skill_1", Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.SPYGLASS),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, true, 0, 80);
    }

    public static final SkillDefinition SNIPER_SKILL_2 = add(sniperSkill2());

    public static SkillDefinition sniperSkill2() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                -0.05f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        var config = new SkillDefinitionConfig("sniper_skill_2", Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.SPYGLASS),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, false, 70, 120);
    }

    public static final SkillDefinition SNIPER_SKILL_3 = add(sniperSkill3());

    public static SkillDefinition sniperSkill3() {
        SkillRewardConfig reward = new SkillRewardConfig(AttributeReward.ID, new AttributeReward(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                0.15f,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
        var config = new SkillDefinitionConfig("sniper_skill_3", Text.literal("..."),
                Text.literal("..."),
                null,
                Icon.item(Items.CROSSBOW),
                null,
                1,
                List.of(reward),
                1, 1, 0, 0, 1
        );
        return new SkillDefinition(config, false, -70, 120);
    }


}
