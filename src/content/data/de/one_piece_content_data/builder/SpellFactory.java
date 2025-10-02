package de.one_piece_content_data.builder;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.fx.Sound;
import net.spell_power.api.SpellSchool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpellFactory {

    public static ActiveSpellBuilder active() {
        return new ActiveSpellBuilder();
    }

    public static PassiveSpellBuilder passive() {
        return new PassiveSpellBuilder();
    }

    public static ModifierSpellBuilder modifier() {
        return new ModifierSpellBuilder();
    }

    public static class ActiveSpellBuilder {
        private final Spell spell = new Spell();

        public ActiveSpellBuilder() {
            spell.type = Spell.Type.ACTIVE;
            spell.active = new Spell.Active();
            spell.active.cast = new Spell.Active.Cast();
            spell.impacts = new ArrayList<>();
        }

        public ActiveSpellBuilder school(SpellSchool school) {
            spell.school = school;
            return this;
        }

        public ActiveSpellBuilder range(float range) {
            spell.range = range;
            return this;
        }

        public ActiveSpellBuilder tier(int tier) {
            spell.tier = tier;
            return this;
        }

        public ActiveSpellBuilder rangeMechanic(Spell.RangeMechanic mechanic) {
            spell.range_mechanic = mechanic;
            return this;
        }

        public ActiveSpellBuilder group(String group) {
            spell.group = group;
            return this;
        }

        public ActiveSpellBuilder casting(Consumer<CastBuilder> config) {
            var builder = new CastBuilder(spell.active.cast);
            config.accept(builder);
            return this;
        }

        public ActiveSpellBuilder release(Consumer<ReleaseBuilder> config) {
            spell.release = new Spell.Release();
            var builder = new ReleaseBuilder(spell.release);
            config.accept(builder);
            return this;
        }

        public ActiveSpellBuilder target(Consumer<TargetBuilder> config) {
            spell.target = new Spell.Target();
            var builder = new TargetBuilder(spell.target);
            config.accept(builder);
            return this;
        }

        public ActiveSpellBuilder delivery(Consumer<DeliveryBuilder> config) {
            spell.deliver = new Spell.Delivery();
            var builder = new DeliveryBuilder(spell.deliver);
            config.accept(builder);
            return this;
        }

        public ActiveSpellBuilder impact(Consumer<ImpactBuilder> config) {
            var builder = new ImpactBuilder();
            config.accept(builder);
            if (spell.impacts == null) {
                spell.impacts = new ArrayList<>();
            }
            spell.impacts.add(builder.build());
            return this;
        }

        public ActiveSpellBuilder areaImpact(Consumer<AreaImpactBuilder> config) {
            spell.area_impact = new Spell.AreaImpact();
            var builder = new AreaImpactBuilder(spell.area_impact);
            config.accept(builder);
            return this;
        }

        public ActiveSpellBuilder cost(Consumer<CostBuilder> config) {
            spell.cost = new Spell.Cost();
            var builder = new CostBuilder(spell.cost);
            config.accept(builder);
            return this;
        }

        public Spell build() {
            return spell;
        }
    }

    public static class PassiveSpellBuilder {
        private final Spell spell = new Spell();

        public PassiveSpellBuilder() {
            spell.type = Spell.Type.PASSIVE;
            spell.passive = new Spell.Passive();
            spell.passive.triggers = new ArrayList<>();
            spell.impacts = new ArrayList<>();
        }

        public PassiveSpellBuilder school(SpellSchool school) {
            spell.school = school;
            return this;
        }

        public PassiveSpellBuilder trigger(Consumer<TriggerBuilder> config) {
            var builder = new TriggerBuilder();
            config.accept(builder);
            spell.passive.triggers.add(builder.build());
            return this;
        }

        public PassiveSpellBuilder impact(Consumer<ImpactBuilder> config) {
            var builder = new ImpactBuilder();
            config.accept(builder);
            if (spell.impacts == null) {
                spell.impacts = new ArrayList<>();
            }
            spell.impacts.add(builder.build());
            return this;
        }

        public Spell build() {
            return spell;
        }
    }

    public static class ModifierSpellBuilder {
        private final Spell spell = new Spell();

        public ModifierSpellBuilder() {
            spell.type = Spell.Type.MODIFIER;
            spell.range = 0;
            spell.modifiers = new ArrayList<>();
            spell.modifiers = new ArrayList<>();
        }

        public ModifierSpellBuilder school(SpellSchool school) {
            spell.school = school;
            return this;
        }

        public ModifierSpellBuilder modify(Consumer<SpellModifierBuilder> config) {
            var builder = new SpellModifierBuilder();
            config.accept(builder);
            spell.modifiers.add(builder.build());
            return this;
        }

        public Spell build() {
            return spell;
        }
    }

    public static class SpellModifierBuilder {
        private final Spell.Modifier modifier = new Spell.Modifier();

        public SpellModifierBuilder spellPattern(String pattern) {
            modifier.spell_pattern = pattern;
            return this;
        }

        public SpellModifierBuilder cooldownDeduct(float seconds) {
            modifier.cooldown_duration_deduct = seconds;
            return this;
        }

        public SpellModifierBuilder cooldownMultiply(float multiplier) {
            modifier.cooldown_duration_deduct = multiplier;
            return this;
        }

        public SpellModifierBuilder rangeAdd(float range) {
            modifier.range_add = range;
            return this;
        }

        public Spell.Modifier build() {
            return modifier;
        }
    }

    public static class TriggerBuilder {
        private final Spell.Trigger trigger = new Spell.Trigger();

        public TriggerBuilder meleeAttack(boolean requireEquipped) {
            trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
            if (requireEquipped) {
                trigger.equipment_condition = EquipmentSlot.MAINHAND;
            }
            return this;
        }

        public TriggerBuilder spellCast(String schoolPattern) {
            trigger.type = Spell.Trigger.Type.SPELL_CAST;
            trigger.spell = new Spell.Trigger.SpellCondition();
            trigger.spell.school = schoolPattern;
            return this;
        }

        public TriggerBuilder spellImpact(String schoolPattern) {
            trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
            trigger.spell = new Spell.Trigger.SpellCondition();
            trigger.spell.school = schoolPattern;
            return this;
        }

        public TriggerBuilder damageTaken() {
            trigger.type = Spell.Trigger.Type.DAMAGE_TAKEN;
            return this;
        }

        public TriggerBuilder arrowShot() {
            trigger.type = Spell.Trigger.Type.ARROW_SHOT;
            return this;
        }

        public TriggerBuilder arrowImpact() {
            trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
            return this;
        }

        public TriggerBuilder chance(float chance) {
            trigger.chance = chance;
            return this;
        }

        public Spell.Trigger build() {
            return trigger;
        }
    }

    public static class CastBuilder {
        private final Spell.Active.Cast cast;

        public CastBuilder(Spell.Active.Cast cast) {
            this.cast = cast;
        }

        public CastBuilder instant() {
            cast.duration = 0;
            return this;
        }

        public CastBuilder duration(float duration) {
            cast.duration = duration;
            return this;
        }

        public CastBuilder channel(int ticks) {
            cast.channel_ticks = ticks;
            return this;
        }

        public CastBuilder animation(String animation) {
            cast.animation = animation;
            return this;
        }

        public CastBuilder sound(Identifier soundId) {
            cast.sound = new Sound(soundId);
            return this;
        }

        public CastBuilder particles(ParticleBatch... particles) {
            cast.particles = particles;
            return this;
        }
    }

    public static class ReleaseBuilder {
        private final Spell.Release release;

        public ReleaseBuilder(Spell.Release release) {
            this.release = release;
        }

        public ReleaseBuilder animation(String animation) {
            release.animation = animation;
            return this;
        }

        public ReleaseBuilder sound(Identifier soundId) {
            release.sound = new Sound(soundId);
            return this;
        }

        public ReleaseBuilder particles(ParticleBatch... particles) {
            release.particles = particles;
            return this;
        }
    }

    public static class TargetBuilder {
        private final Spell.Target target;

        public TargetBuilder(Spell.Target target) {
            this.target = target;
        }

        public TargetBuilder aim() {
            target.type = Spell.Target.Type.AIM;
            target.aim = new Spell.Target.Aim();
            return this;
        }

        public TargetBuilder area(Consumer<AreaBuilder> config) {
            target.type = Spell.Target.Type.AREA;
            target.area = new Spell.Target.Area();
            var builder = new AreaBuilder(target.area);
            config.accept(builder);
            return this;
        }

        public TargetBuilder caster() {
            target.type = Spell.Target.Type.CASTER;
            return this;
        }
    }

    public static class AreaBuilder {
        private final Spell.Target.Area area;

        public AreaBuilder(Spell.Target.Area area) {
            this.area = area;
        }

        public AreaBuilder verticalRange(float multiplier) {
            area.vertical_range_multiplier = multiplier;
            return this;
        }

        public AreaBuilder includeCaster(boolean include) {
            area.include_caster = include;
            return this;
        }
    }

    public static class DeliveryBuilder {
        private final Spell.Delivery delivery;

        public DeliveryBuilder(Spell.Delivery delivery) {
            this.delivery = delivery;
        }

        public DeliveryBuilder projectile(Consumer<ProjectileBuilder> config) {
            delivery.type = Spell.Delivery.Type.PROJECTILE;
            delivery.projectile = new Spell.Delivery.ShootProjectile();
            delivery.projectile.projectile = new Spell.ProjectileData();
            delivery.projectile.projectile.client_data = new Spell.ProjectileData.Client();
            delivery.projectile.launch_properties = new Spell.LaunchProperties();
            var builder = new ProjectileBuilder(delivery.projectile);
            config.accept(builder);
            return this;
        }

        public DeliveryBuilder cloud(Consumer<CloudBuilder> config) {
            delivery.type = Spell.Delivery.Type.CLOUD;
            var cloud = new Spell.Delivery.Cloud();
            var builder = new CloudBuilder(cloud);
            config.accept(builder);
            delivery.clouds = List.of(cloud);
            return this;
        }

        public DeliveryBuilder custom(String handler) {
            delivery.type = Spell.Delivery.Type.CUSTOM;
            delivery.custom = new Spell.Delivery.Custom();
            delivery.custom.handler = handler;
            return this;
        }

        public DeliveryBuilder direct() {
            delivery.type = Spell.Delivery.Type.DIRECT;
            return this;
        }
    }

    public static class ProjectileBuilder {
        private final Spell.Delivery.ShootProjectile projectile;

        public ProjectileBuilder(Spell.Delivery.ShootProjectile projectile) {
            this.projectile = projectile;
        }

        public ProjectileBuilder velocity(float velocity) {
            projectile.launch_properties.velocity = velocity;
            return this;
        }

        public ProjectileBuilder homing(float angle) {
            projectile.projectile.homing_angle = angle;
            return this;
        }

        public ProjectileBuilder model(String modelId, float scale) {
            var model = new Spell.ProjectileModel();
            model.model_id = modelId;
            model.scale = scale;
            projectile.projectile.client_data.model = model;
            return this;
        }

        public ProjectileBuilder lightLevel(int level) {
            projectile.projectile.client_data.light_level = level;
            return this;
        }

        public ProjectileBuilder travelParticles(ParticleBatch... particles) {
            projectile.projectile.client_data.travel_particles = particles;
            return this;
        }

        public ProjectileBuilder launchSound(Identifier soundId) {
            projectile.launch_properties.sound = new Sound(soundId);
            return this;
        }
    }

    public static class CloudBuilder {
        private final Spell.Delivery.Cloud cloud;

        public CloudBuilder(Spell.Delivery.Cloud cloud) {
            this.cloud = cloud;
        }

        public CloudBuilder radius(float radius) {
            cloud.volume.radius = radius;
            return this;
        }

        public CloudBuilder duration(float seconds) {
            cloud.time_to_live_seconds = seconds;
            return this;
        }

        public CloudBuilder impactInterval(int ticks) {
            cloud.impact_tick_interval = ticks;
            return this;
        }

        public CloudBuilder particles(ParticleBatch... particles) {
            cloud.client_data.particles = particles;
            return this;
        }

        public CloudBuilder lightLevel(int level) {
            cloud.client_data.light_level = level;
            return this;
        }

        public CloudBuilder spawnSound(Identifier soundId) {
            cloud.spawn.sound = new Sound(soundId);
            return this;
        }
    }

    public static class ImpactBuilder {
        private final Spell.Impact impact = new Spell.Impact();

        public ImpactBuilder damage(float coefficient, float knockback) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.DAMAGE;
            impact.action.damage = new Spell.Impact.Action.Damage();
            impact.action.damage.spell_power_coefficient = coefficient;
            impact.action.damage.knockback = knockback;
            return this;
        }

        public ImpactBuilder fire(float duration) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.FIRE;
            impact.action.fire = new Spell.Impact.Action.Fire();
            impact.action.fire.duration = duration;
            return this;
        }

        public ImpactBuilder heal(float coefficient) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.HEAL;
            impact.action.heal = new Spell.Impact.Action.Heal();
            impact.action.heal.spell_power_coefficient = coefficient;
            return this;
        }

        public ImpactBuilder statusEffect(String effectId, float duration, int amplifier) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.STATUS_EFFECT;
            impact.action.status_effect = new Spell.Impact.Action.StatusEffect();
            impact.action.status_effect.effect_id = effectId;
            impact.action.status_effect.duration = duration;
            impact.action.status_effect.amplifier = amplifier;
            return this;
        }

        public ImpactBuilder sound(Identifier soundId) {
            impact.sound = new Sound(soundId);
            return this;
        }

        public ImpactBuilder particles(ParticleBatch... particles) {
            impact.particles = particles;
            return this;
        }

        public Spell.Impact build() {
            return impact;
        }
    }

    public static class AreaImpactBuilder {
        private final Spell.AreaImpact area;

        public AreaImpactBuilder(Spell.AreaImpact area) {
            this.area = area;
        }

        public AreaImpactBuilder radius(float radius) {
            area.radius = radius;
            return this;
        }

        public AreaImpactBuilder particles(ParticleBatch... particles) {
            area.particles = particles;
            return this;
        }

        public AreaImpactBuilder sound(Identifier soundId) {
            area.sound = new Sound(soundId);
            return this;
        }
    }

    public static class CostBuilder {
        private final Spell.Cost cost;

        public CostBuilder(Spell.Cost cost) {
            this.cost = cost;
        }

        public CostBuilder cooldown(float duration) {
            cost.cooldown = new Spell.Cost.Cooldown();
            cost.cooldown.duration = duration;
            return this;
        }

        public CostBuilder exhaust(float amount) {
            cost.exhaust = amount;
            return this;
        }

        public CostBuilder item(String itemId, int amount) {
            cost.item = new Spell.Cost.Item();
            cost.item.id = itemId;
            cost.item.amount = amount;
            return this;
        }
    }
}