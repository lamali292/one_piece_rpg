package de.one_piece_content_data.content;

import de.one_piece_content.ExampleMod;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.spell_engine.api.datagen.SpellBuilder;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.fx.Sound;
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_engine.fx.SpellEngineSounds;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.List;

public class ExampleSpells {

    public record Entry(Identifier id, Spell spell, String title, String description) {
    }

    public static final List<Entry> ENTRIES = new ArrayList<>();

    private static Entry add(Entry entry) {
        ENTRIES.add(entry);
        return entry;
    }

    public static final Entry shishi_sonson = add(shishi_sonson());

    private static Entry shishi_sonson() {
        var id = ExampleMod.id("shishi_sonson");
        var title = "Shishi Sonson";
        var description = "Channels a fast, focused dash for 5 blocks forward, slashing any target in the path for {damage} damage.";

        var spell = SpellBuilder.createSpellActive();
        spell.school = SpellSchools.getSchool("physical_melee");
        spell.range = 0.0F;
        spell.tier = 1;

        spell.range_mechanic = Spell.RangeMechanic.MELEE;
        spell.active = new Spell.Active();
        spell.active.cast.duration = 0.25F;
        spell.active.cast.channel_ticks = 1;
        spell.active.cast.animation = "spell_engine:one_handed_throw_charge";

        SpellBuilder.Release.visuals(
                spell, "spell_engine:dual_handed_weapon_cross", new ParticleBatch[]{
                        new ParticleBatch("spell_engine:smoke_medium",
                                ParticleBatch.Shape.CIRCLE,
                                ParticleBatch.Origin.FEET,
                                25.0F, 0.15F, 0.15F
                        ).preSpawnTravel(1)
                }, new Sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP.getId())
        );

        spell.target = new Spell.Target();
        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.vertical_range_multiplier = 0.5F;

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.CUSTOM;
        spell.deliver.custom = new Spell.Delivery.Custom();
        spell.deliver.custom.handler = ExampleMod.id("shishi_sonson").toString();

        Spell.Impact impact = SpellBuilder.Impacts.damage(0, 0);
        impact.particles = new ParticleBatch[]{new ParticleBatch("spell_engine:smoke_medium",
                ParticleBatch.Shape.CIRCLE,
                ParticleBatch.Origin.FEET,
                25.0F, 0.15F, 0.15F
        ).preSpawnTravel(1)};
        spell.impacts = List.of(impact);

        SpellBuilder.Cost.exhaust(spell, 0.5F);
        SpellBuilder.Cost.cooldown(spell, 8.0F);
        return new Entry(id, spell, title, description);
    }

    public static final Entry yakkodori = add(yakkodori());

    private static Entry yakkodori() {
        var id = ExampleMod.id("yakkodori");
        var title = "Yakkodori";
        var description = "Releases a spiritual slash that cuts through obstacles and enemies. Launches a slash of energy 5 blocks forward";
        var spell = SpellBuilder.createSpellActive();
        spell.school = SpellSchools.getSchool("physical_melee");
        spell.range = 5.0F;
        spell.tier = 1;

        spell.active = new Spell.Active();
        spell.active.cast.duration = 0.5F;
        spell.active.cast.animation = "spell_engine:one_handed_throw_charge";

        SpellBuilder.Release.visuals(spell,
                "spell_engine:one_handed_shout_release",
                null,
                new Sound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP.getId())
        );

        spell.target = new Spell.Target();
        spell.target.type = Spell.Target.Type.AIM;
        spell.target.aim = new Spell.Target.Aim();

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.CUSTOM;
        spell.deliver.custom = new Spell.Delivery.Custom();
        spell.deliver.custom.handler = ExampleMod.id("yakkodori").toString();

        SpellBuilder.Cost.exhaust(spell, 0.5F);
        SpellBuilder.Cost.cooldown(spell, 8.0F);

        return new Entry(id, spell, title, description);
    }

    public static final Entry SANDSTORM = add(sandstorm());
    private static Entry sandstorm() {
        var id = ExampleMod.id("sandstorm");
        var title = "Sandstorm";
        var description = "...";
        var spell = new Spell();
        var active = new Spell.Active();
        var cast = new Spell.Active.Cast();
        var release = new Spell.Release();
        var target = new Spell.Target();
        var deliver = new Spell.Delivery();
        var impact1 = new Spell.Impact();
        var impact2 = new Spell.Impact();
        var action1 = new Spell.Impact.Action();
        var action2 = new Spell.Impact.Action();
        var fire = new Spell.Impact.Action.Fire();
        var damage = new Spell.Impact.Action.Damage();
        var cost = new Spell.Cost();
        var projectile = new Spell.Delivery.ShootProjectile();
        var cooldown = new Spell.Cost.Cooldown();
        var launchProperties = new Spell.LaunchProperties();
        var projectileData = new Spell.ProjectileData();
        var projectileClientData = new Spell.ProjectileData.Client();
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
        var projectileModel = new Spell.ProjectileModel();

        {
            {
                {
                    {
                        {
                            projectileModel.model_id =  "one_piece_api:fireball";
                            projectileModel.scale = 0.5F;
                        }
                        projectileClientData.light_level = 12;
                        projectileClientData.model = projectileModel;
                        projectileClientData.travel_particles = travelParticles;
                    }
                    projectileData.client_data = projectileClientData;
                    projectileData.homing_angle = 1;

                    launchProperties.velocity = 1.0F;
                    launchProperties.sound = new Sound(SpellEngineSounds.GENERIC_FIRE_RELEASE.id());
                }
                projectile.projectile = projectileData;
                projectile.launch_properties = launchProperties;
            }
            deliver.projectile = projectile;
            deliver.type = Spell.Delivery.Type.PROJECTILE;

        }
        spell.deliver = deliver;
        {
            release.animation = "spell_engine:one_handed_projectile_release";
        }
        spell.release = release;
        {
            target.type = Spell.Target.Type.AIM;
            target.aim = new Spell.Target.Aim();
        }
        spell.target = target;
        {
            {
                cast.duration = 1.5F;
                cast.animation = "spell_engine:one_handed_projectile_charge";
                cast.sound = Sound.withRandomness(SpellEngineSounds.GENERIC_FIRE_CASTING.id(), 0);
                cast.particles = castParticles;
            }
            active.cast = cast;
        }
        spell.active = active;
        {
            {
                {
                    damage.knockback = 0.8f;
                    damage.spell_power_coefficient = 0.8f;
                }
                action1.type = Spell.Impact.Action.Type.DAMAGE;
                action1.damage = damage;
            }
            impact1.action = action1;
            impact1.sound = new Sound(SpellEngineSounds.SIGNAL_SPELL_CRIT.id());
            impact1.particles = impactParticles;

            {
                {
                    fire.duration = 4;
                }
                action2.type = Spell.Impact.Action.Type.FIRE;
                action2.fire = fire;
            }
            impact2.action = action2;
        }
        {
            {
                cooldown.duration = 8;
            }
            cost.cooldown = cooldown;
            cost.exhaust = 0.5f;
        }

        spell.cost = cost;
        spell.impacts = List.of(impact1, impact2);
        spell.school = SpellSchools.getSchool("physical_melee");;
        spell.group = "primary";
        spell.range = 30.0F;
        spell.tier = 1;

        return new Entry(id, spell, title, description);
    }

    public static final Entry SANDSTORM_MODIFIER_1 = add(arcane_spec_b_modifier_1());
    private static Entry arcane_spec_b_modifier_1() {
        var id = ExampleMod.id( "sandstorm_modifier_1");
        var title = "Arcane Endurance";
        var description = "Reduces the cooldown of Sandstorm by {cooldown_duration_deduct} sec.";
        var spell = SpellBuilder.createSpellModifier();
        spell.school = SpellSchools.ARCANE;

        var modifier = new Spell.Modifier();
        modifier.spell_pattern = SANDSTORM.id().toString();
        modifier.cooldown_duration_deduct = 5;
        spell.modifiers = List.of(modifier);

        return new Entry(id, spell, title, description);
    }

    public static final List<Entry> DUMMY_SPELLS = add_dummies(List.of("dummy", "quicksand", "sand_blade", "sand_drain", "sand_hand", "sand_spikes"));
    private static List<Entry> add_dummies(List<String> list) {
        return list.stream().map(
                id -> {
                    var title = toTitleCase(id);
                    var description = toTitleCase(id) + " Description";
                    var spell = SpellBuilder.createSpellActive();
                    spell.school = SpellSchools.getSchool("physical_melee");
                    spell.range = .0F;
                    spell.tier = 1;
                    return add(new Entry(ExampleMod.id(id), spell, title, description));
                }
        ).toList();
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
