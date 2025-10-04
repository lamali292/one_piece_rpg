package de.one_piece_content_data.content;

import de.one_piece_content.ExampleMod;
import de.one_piece_api.config.SpellConfig;
import de.one_piece_content_data.registry.Entry;
import de.one_piece_content_data.registry.Registries;
import de.one_piece_content_data.builder.SpellFactory;
import net.minecraft.sound.SoundEvents;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_engine.fx.SpellEngineSounds;
import net.spell_power.api.SpellSchools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleSpells {

    public static void init() {
    }

    public static final Entry<SpellConfig> SHISHI_SONSON = Registries.SPELLS.register(
            ExampleMod.id("shishi_sonson"),
            shishi_sonson());

    public static final Entry<SpellConfig>  YAKKODORI = Registries.SPELLS.register(
            ExampleMod.id("yakkodori"),
            yakkodori());

    public static final Entry<SpellConfig>  SANDSTORM = Registries.SPELLS.register(ExampleMod.id("sandstorm"),
            sandstorm());

    public static final Entry<SpellConfig>  SANDSTORM_MODIFIER_1 = Registries.SPELLS.register(
            ExampleMod.id("sandstorm_modifier_1"),
            arcane_spec_b_modifier_1());

    public static final Map<String, Entry<SpellConfig>> DUMMY_SPELLS = add_dummies(List.of("dummy", "quicksand", "sand_blade", "sand_drain", "sand_hand", "sand_spikes"));


    private static SpellConfig shishi_sonson() {
        var spell = SpellFactory.active()
                .school(SpellSchools.getSchool("physical_melee"))
                .range(0f)
                .tier(1)
                .rangeMechanic(Spell.RangeMechanic.MELEE)
                .casting(cast -> cast
                        .duration(0.25f)
                        .channel(1)
                        .animation("spell_engine:one_handed_throw_charge"))
                .release(release -> release
                        .animation("spell_engine:dual_handed_weapon_cross")
                        .sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP.getId())
                        .particles(new ParticleBatch("spell_engine:smoke_medium",
                                ParticleBatch.Shape.CIRCLE,
                                ParticleBatch.Origin.FEET,
                                25.0f, 0.15f, 0.15f).preSpawnTravel(1)))
                .target(target -> target
                        .area(area -> area.verticalRange(0.5f)))
                .delivery(deliver -> deliver
                        .custom(ExampleMod.id("shishi_sonson").toString()))
                .impact(impact -> impact
                        .damage(0, 0)
                        .particles(new ParticleBatch("spell_engine:smoke_medium",
                                ParticleBatch.Shape.CIRCLE,
                                ParticleBatch.Origin.FEET,
                                25.0f, 0.15f, 0.15f).preSpawnTravel(1)))
                .cost(cost -> cost
                        .cooldown(8f)
                        .stamina(10)
                        .exhaust(0.5f))
                .build();
        return new SpellConfig(spell, "Shishi Sonson",
                "Channels a fast, focused dash for 5 blocks forward, slashing any target in the path for {damage} damage.");
    }

    private static SpellConfig yakkodori() {

        var spell = SpellFactory.active()
                .school(SpellSchools.getSchool("physical_melee"))
                .range(5f)
                .tier(1)
                .casting(cast -> cast
                        .duration(0.5f)
                        .animation("spell_engine:one_handed_throw_charge"))
                .release(release -> release
                        .animation("spell_engine:one_handed_shout_release")
                        .sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP.getId()))
                .target(SpellFactory.TargetBuilder::aim)
                .delivery(deliver -> deliver
                        .custom(ExampleMod.id("yakkodori").toString()))
                .cost(cost -> cost
                        .cooldown(8f)
                        .stamina(10)
                        .exhaust(0.5f))
                .build();

        return new SpellConfig(spell, "Yakkodori",
                "Releases a spiritual slash that cuts through obstacles and enemies. Launches a slash of energy 5 blocks forward");
    }


    private static SpellConfig sandstorm() {
        var castParticles = new ParticleBatch[]{new ParticleBatch(
                SpellEngineParticles.flame.id().toString(),
                ParticleBatch.Shape.WIDE_PIPE,
                ParticleBatch.Origin.FEET,
                1, 0.05f, 0.1f
        )};
        var travelParticles = new ParticleBatch[]{new ParticleBatch(
                SpellEngineParticles.flame.id().toString(),
                ParticleBatch.Shape.CIRCLE,
                ParticleBatch.Origin.CENTER,
                ParticleBatch.Rotation.LOOK, 0, 0, 3,
                0, 0.1f, 0, 0, 0, false,
                -1, 1, false, 1F
        ), new ParticleBatch(
                SpellEngineParticles.smoke_large.id().toString(),
                ParticleBatch.Shape.CIRCLE,
                ParticleBatch.Origin.CENTER,
                ParticleBatch.Rotation.LOOK, 0, 0, 3,
                0, 0.1f, 0, 0, 0, false,
                -1, 1, false, 1F
        )};
        var impactParticles = new ParticleBatch[]{new ParticleBatch(
                SpellEngineParticles.smoke_large.id().toString(),
                ParticleBatch.Shape.SPHERE,
                ParticleBatch.Origin.CENTER,
                15, 0.01f, 0.1f
        ), new ParticleBatch(
                SpellEngineParticles.flame_medium_b.id().toString(),
                ParticleBatch.Shape.CIRCLE,
                ParticleBatch.Origin.CENTER,
                15, 0.1f, 0.2f
        )};
        var spell = SpellFactory.active()
                .school(SpellSchools.getSchool("physical_melee"))
                .range(30f)
                .tier(1)
                .group("primary")
                .casting(cast -> cast
                        .duration(1.5f)
                        .animation("spell_engine:one_handed_projectile_charge")
                        .sound(SpellEngineSounds.GENERIC_FIRE_CASTING.id())
                        .particles(castParticles))
                .release(release -> release
                        .animation("spell_engine:one_handed_projectile_release"))
                .target(SpellFactory.TargetBuilder::aim)
                .delivery(deliver -> deliver
                        .projectile(proj -> proj
                                .velocity(1.0f)
                                .homing(1)
                                .model("one_piece_api:fireball", 0.5f)
                                .lightLevel(12)
                                .launchSound(SpellEngineSounds.GENERIC_FIRE_RELEASE.id())
                                .travelParticles(travelParticles)))
                .impact(impact -> impact
                        .damage(0.8f, 0.8f)
                        .sound(SpellEngineSounds.SIGNAL_SPELL_CRIT.id())
                        .particles(impactParticles))
                .impact(impact -> impact
                        .fire(4))
                .cost(cost -> cost
                        .cooldown(8)
                        .stamina(15)
                        .exhaust(0.5f))
                .build();
        return new SpellConfig(spell, "Sandstorm", "...");
    }


    private static SpellConfig arcane_spec_b_modifier_1() {
        var spell = SpellFactory.modifier()
                .school(SpellSchools.ARCANE)
                .modify(mod -> mod
                        .spellPattern(SANDSTORM.id().toString())
                        .cooldownDeduct(5))
                .build();

        return new SpellConfig(spell, "Arcane Endurance",
                "Reduces the cooldown of Sandstorm by {cooldown_duration_deduct} sec.");
    }

    private static HashMap<String, Entry<SpellConfig> > add_dummies(List<String> list) {
        HashMap<String, Entry<SpellConfig>> map = new HashMap<>();
        list.forEach(id -> {
            var title = toTitleCase(id);
            var description = toTitleCase(id) + " Description";

            var spell = SpellFactory.active()
                    .school(SpellSchools.getSchool("physical_melee"))
                    .range(0f)
                    .tier(1)
                    .build();
            var entry = new SpellConfig(spell, title, description);
            map.put(id, Registries.SPELLS.register(ExampleMod.id(id), entry));
        });
        return map;
    }

    public static String toTitleCase(String input) {
        String[] parts = input.split("_"); // split by underscore
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            result.append(Character.toUpperCase(parts[i].charAt(0))) // first letter uppercase
                    .append(parts[i].substring(1)); // rest as is
            if (i < parts.length - 1) result.append(" "); // add space between words
        }

        return result.toString();
    }
}
