package de.one_piece_content_data.builder;

import de.one_piece_api.mixin_interface.IStaminaCost;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.fx.Sound;
import net.spell_power.api.SpellSchool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Factory for creating spell configurations using fluent builder patterns.
 * <p>
 * This factory provides entry points for three types of spell builders:
 * <ul>
 *     <li><b>Active</b> - Player-triggered abilities with casting, delivery, and impact</li>
 *     <li><b>Passive</b> - Automatic abilities triggered by game events</li>
 *     <li><b>Modifier</b> - Spells that modify other spells' behavior</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Spell fireball = SpellFactory.active()
 *     .school(SpellSchools.FIRE)
 *     .range(30)
 *     .casting(cast -> cast
 *         .duration(1.5f)
 *         .animation("casting"))
 *     .delivery(delivery -> delivery.projectile(proj -> proj
 *         .velocity(1.5f)
 *         .model("fireball", 1.0f)))
 *     .impact(impact -> impact
 *         .damage(2.0f, 0.5f)
 *         .fire(5.0f))
 *     .cost(cost -> cost
 *         .cooldown(10f)
 *         .stamina(50f))
 *     .build();
 * }</pre>
 *
 * @see Spell
 * @see ActiveSpellBuilder
 * @see PassiveSpellBuilder
 * @see ModifierSpellBuilder
 */
public class SpellFactory {

    /**
     * Creates a new active spell builder.
     * <p>
     * Active spells are player-triggered abilities that involve casting,
     * delivery (projectile, cloud, etc.), and impact effects.
     *
     * @return a new active spell builder
     */
    public static ActiveSpellBuilder active() {
        return new ActiveSpellBuilder();
    }

    /**
     * Creates a new passive spell builder.
     * <p>
     * Passive spells are automatically triggered by game events like
     * melee attacks, spell casts, or taking damage.
     *
     * @return a new passive spell builder
     */
    public static PassiveSpellBuilder passive() {
        return new PassiveSpellBuilder();
    }

    /**
     * Creates a new modifier spell builder.
     * <p>
     * Modifier spells alter the behavior of other spells, such as
     * reducing cooldowns or increasing range.
     *
     * @return a new modifier spell builder
     */
    public static ModifierSpellBuilder modifier() {
        return new ModifierSpellBuilder();
    }

    /**
     * Builder for active spells with casting, targeting, delivery, and impact phases.
     * <p>
     * Active spells follow a complete lifecycle:
     * <ol>
     *     <li>Cast - Player initiates the spell with optional cast time</li>
     *     <li>Release - Animation/effects when cast completes</li>
     *     <li>Target - How targets are selected (aim, area, self)</li>
     *     <li>Delivery - How the spell reaches targets (projectile, cloud, direct)</li>
     *     <li>Impact - Effects applied to targets (damage, heal, status effects)</li>
     * </ol>
     */
    public static class ActiveSpellBuilder {
        /** The spell being built */
        private final Spell spell = new Spell();

        /**
         * Creates a new active spell builder with default configuration.
         */
        public ActiveSpellBuilder() {
            spell.type = Spell.Type.ACTIVE;
            spell.active = new Spell.Active();
            spell.active.cast = new Spell.Active.Cast();
            spell.impacts = new ArrayList<>();
        }

        /**
         * Sets the spell school (element/type).
         *
         * @param school the spell school
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder school(SpellSchool school) {
            spell.school = school;
            return this;
        }

        /**
         * Sets the maximum range of the spell in blocks.
         *
         * @param range the range in blocks
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder range(float range) {
            spell.range = range;
            return this;
        }

        /**
         * Sets the spell tier for progression systems.
         *
         * @param tier the tier level
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder tier(int tier) {
            spell.tier = tier;
            return this;
        }

        /**
         * Sets how range is calculated and enforced.
         *
         * @param mechanic the range mechanic
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder rangeMechanic(Spell.RangeMechanic mechanic) {
            spell.range_mechanic = mechanic;
            return this;
        }

        /**
         * Sets the spell group for cooldown sharing.
         *
         * @param group the group identifier
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder group(String group) {
            spell.group = group;
            return this;
        }

        /**
         * Configures the casting phase of the spell.
         *
         * @param config consumer that configures the cast builder
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder casting(Consumer<CastBuilder> config) {
            var builder = new CastBuilder(spell.active.cast);
            config.accept(builder);
            return this;
        }

        /**
         * Configures the release phase (when cast completes).
         *
         * @param config consumer that configures the release builder
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder release(Consumer<ReleaseBuilder> config) {
            spell.release = new Spell.Release();
            var builder = new ReleaseBuilder(spell.release);
            config.accept(builder);
            return this;
        }

        /**
         * Configures how targets are selected.
         *
         * @param config consumer that configures the target builder
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder target(Consumer<TargetBuilder> config) {
            spell.target = new Spell.Target();
            var builder = new TargetBuilder(spell.target);
            config.accept(builder);
            return this;
        }

        /**
         * Configures how the spell is delivered to targets.
         *
         * @param config consumer that configures the delivery builder
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder delivery(Consumer<DeliveryBuilder> config) {
            spell.deliver = new Spell.Delivery();
            var builder = new DeliveryBuilder(spell.deliver);
            config.accept(builder);
            return this;
        }

        /**
         * Adds an impact effect to the spell.
         * <p>
         * Multiple impacts can be added for spells with multiple effects.
         *
         * @param config consumer that configures the impact builder
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder impact(Consumer<ImpactBuilder> config) {
            var builder = new ImpactBuilder();
            config.accept(builder);
            if (spell.impacts == null) {
                spell.impacts = new ArrayList<>();
            }
            spell.impacts.add(builder.build());
            return this;
        }

        /**
         * Configures area-of-effect impact settings.
         *
         * @param config consumer that configures the area impact builder
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder areaImpact(Consumer<AreaImpactBuilder> config) {
            spell.area_impact = new Spell.AreaImpact();
            var builder = new AreaImpactBuilder(spell.area_impact);
            config.accept(builder);
            return this;
        }

        /**
         * Configures the spell's costs (cooldown, stamina, items).
         *
         * @param config consumer that configures the cost builder
         * @return this builder for method chaining
         */
        public ActiveSpellBuilder cost(Consumer<CostBuilder> config) {
            spell.cost = new Spell.Cost();
            var builder = new CostBuilder(spell.cost);
            config.accept(builder);
            return this;
        }

        /**
         * Builds the final spell configuration.
         *
         * @return the completed spell
         */
        public Spell build() {
            return spell;
        }
    }

    /**
     * Builder for passive spells that trigger automatically from game events.
     * <p>
     * Passive spells respond to triggers like melee attacks, spell casts,
     * or taking damage, and apply their effects without player input.
     */
    public static class PassiveSpellBuilder {
        /** The spell being built */
        private final Spell spell = new Spell();

        /**
         * Creates a new passive spell builder with default configuration.
         */
        public PassiveSpellBuilder() {
            spell.type = Spell.Type.PASSIVE;
            spell.passive = new Spell.Passive();
            spell.passive.triggers = new ArrayList<>();
            spell.impacts = new ArrayList<>();
        }

        /**
         * Sets the spell school (element/type).
         *
         * @param school the spell school
         * @return this builder for method chaining
         */
        public PassiveSpellBuilder school(SpellSchool school) {
            spell.school = school;
            return this;
        }

        /**
         * Adds a trigger condition for the passive spell.
         * <p>
         * Multiple triggers can be added; the spell activates when any trigger fires.
         *
         * @param config consumer that configures the trigger builder
         * @return this builder for method chaining
         */
        public PassiveSpellBuilder trigger(Consumer<TriggerBuilder> config) {
            var builder = new TriggerBuilder();
            config.accept(builder);
            spell.passive.triggers.add(builder.build());
            return this;
        }

        /**
         * Adds an impact effect when the passive triggers.
         * <p>
         * Multiple impacts can be added for complex effects.
         *
         * @param config consumer that configures the impact builder
         * @return this builder for method chaining
         */
        public PassiveSpellBuilder impact(Consumer<ImpactBuilder> config) {
            var builder = new ImpactBuilder();
            config.accept(builder);
            if (spell.impacts == null) {
                spell.impacts = new ArrayList<>();
            }
            spell.impacts.add(builder.build());
            return this;
        }

        /**
         * Builds the final spell configuration.
         *
         * @return the completed spell
         */
        public Spell build() {
            return spell;
        }
    }

    /**
     * Builder for modifier spells that alter other spells' behavior.
     * <p>
     * Modifier spells don't have their own effects, but instead change
     * properties of other spells matching specified patterns.
     */
    public static class ModifierSpellBuilder {
        /** The spell being built */
        private final Spell spell = new Spell();

        /**
         * Creates a new modifier spell builder with default configuration.
         */
        public ModifierSpellBuilder() {
            spell.type = Spell.Type.MODIFIER;
            spell.range = 0;
            spell.modifiers = new ArrayList<>();
        }

        /**
         * Sets the spell school (element/type).
         *
         * @param school the spell school
         * @return this builder for method chaining
         */
        public ModifierSpellBuilder school(SpellSchool school) {
            spell.school = school;
            return this;
        }

        /**
         * Adds a modification to apply to matching spells.
         * <p>
         * Multiple modifications can be added to affect different spell patterns.
         *
         * @param config consumer that configures the modifier builder
         * @return this builder for method chaining
         */
        public ModifierSpellBuilder modify(Consumer<SpellModifierBuilder> config) {
            var builder = new SpellModifierBuilder();
            config.accept(builder);
            spell.modifiers.add(builder.build());
            return this;
        }

        /**
         * Builds the final spell configuration.
         *
         * @return the completed spell
         */
        public Spell build() {
            return spell;
        }
    }

    /**
     * Builder for individual spell modifications.
     * <p>
     * Defines which spells are affected and how their properties are changed.
     */
    public static class SpellModifierBuilder {
        /** The modifier being built */
        private final Spell.Modifier modifier = new Spell.Modifier();

        /**
         * Sets the pattern for matching spells to modify.
         * <p>
         * Patterns can use wildcards to match multiple spells.
         *
         * @param pattern the spell matching pattern
         * @return this builder for method chaining
         */
        public SpellModifierBuilder spellPattern(String pattern) {
            modifier.spell_pattern = pattern;
            return this;
        }

        /**
         * Reduces cooldown duration by a fixed amount.
         *
         * @param seconds the seconds to reduce
         * @return this builder for method chaining
         */
        public SpellModifierBuilder cooldownDeduct(float seconds) {
            modifier.cooldown_duration_deduct = seconds;
            return this;
        }

        /**
         * Increases spell range by a fixed amount.
         *
         * @param range the blocks to add to range
         * @return this builder for method chaining
         */
        public SpellModifierBuilder rangeAdd(float range) {
            modifier.range_add = range;
            return this;
        }

        /**
         * Builds the final modifier configuration.
         *
         * @return the completed modifier
         */
        public Spell.Modifier build() {
            return modifier;
        }
    }

    /**
     * Builder for passive spell triggers.
     * <p>
     * Defines when a passive spell should activate based on game events.
     */
    public static class TriggerBuilder {
        /** The trigger being built */
        private final Spell.Trigger trigger = new Spell.Trigger();

        /**
         * Triggers on melee attack impact.
         *
         * @param requireEquipped whether to require weapon in main hand
         * @return this builder for method chaining
         */
        public TriggerBuilder meleeAttack(boolean requireEquipped) {
            trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
            if (requireEquipped) {
                trigger.equipment_condition = EquipmentSlot.MAINHAND;
            }
            return this;
        }

        /**
         * Triggers when a specific status effect ticks on the entity.
         * <p>
         * This allows creating self-sustaining passive effects that
         * check conditions and apply effects continuously.
         *
         * @param effectId the status effect identifier
         * @return this builder for method chaining
         */
        public TriggerBuilder effectTick(String effectId) {
            trigger.type = Spell.Trigger.Type.EFFECT_TICK;
            trigger.effect = new Spell.Trigger.EffectCondition();
            trigger.effect.id = effectId;
            return this;
        }

        /**
         * Adds a condition that must be met by the caster for the trigger to fire.
         *
         * @param config consumer that configures the condition
         * @return this builder for method chaining
         */
        public TriggerBuilder casterCondition(Consumer<TargetConditionBuilder> config) {
            if (trigger.caster_conditions == null) {
                trigger.caster_conditions = new ArrayList<>();
            }
            var builder = new TargetConditionBuilder();
            config.accept(builder);
            trigger.caster_conditions.add(builder.build());
            return this;
        }

        /**
         * Adds a condition that must be met by the target for the trigger to fire.
         *
         * @param config consumer that configures the condition
         * @return this builder for method chaining
         */
        public TriggerBuilder targetCondition(Consumer<TargetConditionBuilder> config) {
            if (trigger.target_conditions == null) {
                trigger.target_conditions = new ArrayList<>();
            }
            var builder = new TargetConditionBuilder();
            config.accept(builder);
            trigger.target_conditions.add(builder.build());
            return this;
        }

        /**
         * Triggers when casting spells matching a school pattern.
         *
         * @param schoolPattern the school pattern to match
         * @return this builder for method chaining
         */
        public TriggerBuilder spellCast(String schoolPattern) {
            trigger.type = Spell.Trigger.Type.SPELL_CAST;
            trigger.spell = new Spell.Trigger.SpellCondition();
            trigger.spell.school = schoolPattern;
            return this;
        }

        /**
         * Triggers when spells matching a school pattern hit targets.
         *
         * @param schoolPattern the school pattern to match
         * @return this builder for method chaining
         */
        public TriggerBuilder spellImpact(String schoolPattern) {
            trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
            trigger.spell = new Spell.Trigger.SpellCondition();
            trigger.spell.school = schoolPattern;
            return this;
        }

        /**
         * Triggers when taking damage.
         *
         * @return this builder for method chaining
         */
        public TriggerBuilder damageTaken() {
            trigger.type = Spell.Trigger.Type.DAMAGE_TAKEN;
            return this;
        }

        /**
         * Triggers when shooting an arrow.
         *
         * @return this builder for method chaining
         */
        public TriggerBuilder arrowShot() {
            trigger.type = Spell.Trigger.Type.ARROW_SHOT;
            return this;
        }

        /**
         * Triggers when an arrow impacts a target.
         *
         * @return this builder for method chaining
         */
        public TriggerBuilder arrowImpact() {
            trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
            return this;
        }

        /**
         * Sets the probability of triggering (0.0 to 1.0).
         *
         * @param chance the trigger chance
         * @return this builder for method chaining
         */
        public TriggerBuilder chance(float chance) {
            trigger.chance = chance;
            return this;
        }

        /**
         * Builds the final trigger configuration.
         *
         * @return the completed trigger
         */
        public Spell.Trigger build() {
            return trigger;
        }
    }


    /**
     * Builder for target condition configuration.
     * <p>
     * Defines conditions that must be met for triggers or impacts to execute.
     */
    public static class TargetConditionBuilder {
        /** The condition being built */
        private final Spell.TargetCondition condition = new Spell.TargetCondition();

        /**
         * Sets health percentage range requirement.
         *
         * @param min minimum health percentage (0.0 to 1.0)
         * @param max maximum health percentage (0.0 to 1.0)
         * @return this builder for method chaining
         */
        public TargetConditionBuilder healthPercent(float min, float max) {
            condition.health_percent_above = min;
            condition.health_percent_below = max;
            return this;
        }

        /**
         * Sets entity type pattern requirement.
         *
         * @param pattern the entity type pattern
         * @return this builder for method chaining
         */
        public TargetConditionBuilder entityType(String pattern) {
            condition.entity_type = pattern;
            return this;
        }

        /**
         * Sets entity predicate requirement.
         *
         * @param predicateId the predicate identifier
         * @return this builder for method chaining
         */
        public TargetConditionBuilder entityPredicate(String predicateId) {
            condition.entity_predicate_id = predicateId;
            return this;
        }

        /**
         * Sets entity predicate with parameter.
         *
         * @param predicateId the predicate identifier
         * @param param the parameter to pass to the predicate
         * @return this builder for method chaining
         */
        public TargetConditionBuilder entityPredicate(String predicateId, String param) {
            condition.entity_predicate_id = predicateId;
            condition.entity_predicate_param = param;
            return this;
        }

        /**
         * Builds the final condition configuration.
         *
         * @return the completed condition
         */
        public Spell.TargetCondition build() {
            return condition;
        }
    }

    /**
     * Builder for spell casting configuration.
     * <p>
     * Defines cast duration, animation, sounds, and visual effects.
     */
    public static class CastBuilder {
        /** The cast configuration being built */
        private final Spell.Active.Cast cast;

        /**
         * Creates a new cast builder.
         *
         * @param cast the cast configuration to modify
         */
        public CastBuilder(Spell.Active.Cast cast) {
            this.cast = cast;
        }

        /**
         * Makes the spell cast instantly with no cast time.
         *
         * @return this builder for method chaining
         */
        public CastBuilder instant() {
            cast.duration = 0;
            return this;
        }

        /**
         * Sets the cast duration in seconds.
         *
         * @param duration the cast time in seconds
         * @return this builder for method chaining
         */
        public CastBuilder duration(float duration) {
            cast.duration = duration;
            return this;
        }

        /**
         * Makes the spell channeled with periodic ticks.
         *
         * @param ticks the interval between channel ticks
         * @return this builder for method chaining
         */
        public CastBuilder channel(int ticks) {
            cast.channel_ticks = ticks;
            return this;
        }

        /**
         * Sets the casting animation.
         *
         * @param animation the animation identifier
         * @return this builder for method chaining
         */
        public CastBuilder animation(String animation) {
            cast.animation = animation;
            return this;
        }

        /**
         * Sets the casting sound effect.
         *
         * @param soundId the sound identifier
         * @return this builder for method chaining
         */
        public CastBuilder sound(Identifier soundId) {
            cast.sound = new Sound(soundId);
            return this;
        }

        /**
         * Sets the casting particle effects.
         *
         * @param particles the particle batches to spawn
         * @return this builder for method chaining
         */
        public CastBuilder particles(ParticleBatch... particles) {
            cast.particles = particles;
            return this;
        }
    }

    /**
     * Builder for spell release configuration.
     * <p>
     * Defines effects that play when the cast completes and the spell releases.
     */
    public static class ReleaseBuilder {
        /** The release configuration being built */
        private final Spell.Release release;

        /**
         * Creates a new release builder.
         *
         * @param release the release configuration to modify
         */
        public ReleaseBuilder(Spell.Release release) {
            this.release = release;
        }

        /**
         * Sets the release animation.
         *
         * @param animation the animation identifier
         * @return this builder for method chaining
         */
        public ReleaseBuilder animation(String animation) {
            release.animation = animation;
            return this;
        }

        /**
         * Sets the release sound effect.
         *
         * @param soundId the sound identifier
         * @return this builder for method chaining
         */
        public ReleaseBuilder sound(Identifier soundId) {
            release.sound = new Sound(soundId);
            return this;
        }

        /**
         * Sets the release particle effects.
         *
         * @param particles the particle batches to spawn
         * @return this builder for method chaining
         */
        public ReleaseBuilder particles(ParticleBatch... particles) {
            release.particles = particles;
            return this;
        }
    }

    /**
     * Builder for spell targeting configuration.
     * <p>
     * Defines how the spell selects its targets.
     */
    public static class TargetBuilder {
        /** The target configuration being built */
        private final Spell.Target target;

        /**
         * Creates a new target builder.
         *
         * @param target the target configuration to modify
         */
        public TargetBuilder(Spell.Target target) {
            this.target = target;
        }

        /**
         * Targets where the player is aiming.
         *
         * @return this builder for method chaining
         */
        public TargetBuilder aim() {
            target.type = Spell.Target.Type.AIM;
            target.aim = new Spell.Target.Aim();
            return this;
        }

        /**
         * Targets an area around a point.
         *
         * @param config consumer that configures the area builder
         * @return this builder for method chaining
         */
        public TargetBuilder area(Consumer<AreaBuilder> config) {
            target.type = Spell.Target.Type.AREA;
            target.area = new Spell.Target.Area();
            var builder = new AreaBuilder(target.area);
            config.accept(builder);
            return this;
        }

        /**
         * Targets the caster themselves.
         *
         * @return this builder for method chaining
         */
        public TargetBuilder caster() {
            target.type = Spell.Target.Type.CASTER;
            return this;
        }
    }

    /**
     * Builder for area targeting configuration.
     * <p>
     * Configures area-of-effect targeting parameters.
     */
    public static class AreaBuilder {
        /** The area configuration being built */
        private final Spell.Target.Area area;

        /**
         * Creates a new area builder.
         *
         * @param area the area configuration to modify
         */
        public AreaBuilder(Spell.Target.Area area) {
            this.area = area;
        }

        /**
         * Sets the vertical range multiplier for cylindrical areas.
         *
         * @param multiplier the vertical range multiplier
         * @return this builder for method chaining
         */
        public AreaBuilder verticalRange(float multiplier) {
            area.vertical_range_multiplier = multiplier;
            return this;
        }

        /**
         * Sets whether the caster is included in area targeting.
         *
         * @param include {@code true} to include caster, {@code false} to exclude
         * @return this builder for method chaining
         */
        public AreaBuilder includeCaster(boolean include) {
            area.include_caster = include;
            return this;
        }
    }

    /**
     * Builder for spell delivery configuration.
     * <p>
     * Defines how the spell travels from caster to targets.
     */
    public static class DeliveryBuilder {
        /** The delivery configuration being built */
        private final Spell.Delivery delivery;

        /**
         * Creates a new delivery builder.
         *
         * @param delivery the delivery configuration to modify
         */
        public DeliveryBuilder(Spell.Delivery delivery) {
            this.delivery = delivery;
        }

        /**
         * Delivers via projectile entity.
         *
         * @param config consumer that configures the projectile builder
         * @return this builder for method chaining
         */
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

        /**
         * Delivers via lingering cloud entity.
         *
         * @param config consumer that configures the cloud builder
         * @return this builder for method chaining
         */
        public DeliveryBuilder cloud(Consumer<CloudBuilder> config) {
            delivery.type = Spell.Delivery.Type.CLOUD;
            var cloud = new Spell.Delivery.Cloud();
            var builder = new CloudBuilder(cloud);
            config.accept(builder);
            delivery.clouds = List.of(cloud);
            return this;
        }

        /**
         * Delivers via custom handler logic.
         *
         * @param handler the custom handler identifier
         * @return this builder for method chaining
         */
        public DeliveryBuilder custom(String handler) {
            delivery.type = Spell.Delivery.Type.CUSTOM;
            delivery.custom = new Spell.Delivery.Custom();
            delivery.custom.handler = handler;
            return this;
        }

        /**
         * Delivers directly with no travel time.
         *
         * @return this builder for method chaining
         */
        public DeliveryBuilder direct() {
            delivery.type = Spell.Delivery.Type.DIRECT;
            return this;
        }
    }

    /**
     * Builder for projectile delivery configuration.
     * <p>
     * Configures projectile appearance, behavior, and effects.
     */
    public static class ProjectileBuilder {
        /** The projectile configuration being built */
        private final Spell.Delivery.ShootProjectile projectile;

        /**
         * Creates a new projectile builder.
         *
         * @param projectile the projectile configuration to modify
         */
        public ProjectileBuilder(Spell.Delivery.ShootProjectile projectile) {
            this.projectile = projectile;
        }

        /**
         * Sets the projectile velocity.
         *
         * @param velocity the velocity in blocks per second
         * @return this builder for method chaining
         */
        public ProjectileBuilder velocity(float velocity) {
            projectile.launch_properties.velocity = velocity;
            return this;
        }

        /**
         * Makes the projectile home toward targets.
         *
         * @param angle the maximum homing angle in degrees
         * @return this builder for method chaining
         */
        public ProjectileBuilder homing(float angle) {
            projectile.projectile.homing_angle = angle;
            return this;
        }

        /**
         * Sets the projectile 3D model.
         *
         * @param modelId the model identifier
         * @param scale the model scale
         * @return this builder for method chaining
         */
        public ProjectileBuilder model(String modelId, float scale) {
            var model = new Spell.ProjectileModel();
            model.model_id = modelId;
            model.scale = scale;
            projectile.projectile.client_data.model = model;
            return this;
        }

        /**
         * Sets the light level emitted by the projectile.
         *
         * @param level the light level (0-15)
         * @return this builder for method chaining
         */
        public ProjectileBuilder lightLevel(int level) {
            projectile.projectile.client_data.light_level = level;
            return this;
        }

        /**
         * Sets particles spawned during projectile travel.
         *
         * @param particles the particle batches to spawn
         * @return this builder for method chaining
         */
        public ProjectileBuilder travelParticles(ParticleBatch... particles) {
            projectile.projectile.client_data.travel_particles = particles;
            return this;
        }

        /**
         * Sets the sound played when launching the projectile.
         *
         * @param soundId the sound identifier
         * @return this builder for method chaining
         */
        public ProjectileBuilder launchSound(Identifier soundId) {
            projectile.launch_properties.sound = new Sound(soundId);
            return this;
        }
    }

    /**
     * Builder for cloud delivery configuration.
     * <p>
     * Configures lingering cloud appearance, duration, and effects.
     */
    public static class CloudBuilder {
        /** The cloud configuration being built */
        private final Spell.Delivery.Cloud cloud;

        /**
         * Creates a new cloud builder.
         *
         * @param cloud the cloud configuration to modify
         */
        public CloudBuilder(Spell.Delivery.Cloud cloud) {
            this.cloud = cloud;
        }

        /**
         * Sets the cloud radius in blocks.
         *
         * @param radius the radius
         * @return this builder for method chaining
         */
        public CloudBuilder radius(float radius) {
            cloud.volume.radius = radius;
            return this;
        }

        /**
         * Sets how long the cloud persists.
         *
         * @param seconds the duration in seconds
         * @return this builder for method chaining
         */
        public CloudBuilder duration(float seconds) {
            cloud.time_to_live_seconds = seconds;
            return this;
        }

        /**
         * Sets how often the cloud applies its effects.
         *
         * @param ticks the ticks between impacts
         * @return this builder for method chaining
         */
        public CloudBuilder impactInterval(int ticks) {
            cloud.impact_tick_interval = ticks;
            return this;
        }

        /**
         * Sets the cloud particle effects.
         *
         * @param particles the particle batches to spawn
         * @return this builder for method chaining
         */
        public CloudBuilder particles(ParticleBatch... particles) {
            cloud.client_data.particles = particles;
            return this;
        }

        /**
         * Sets the light level emitted by the cloud.
         *
         * @param level the light level (0-15)
         * @return this builder for method chaining
         */
        public CloudBuilder lightLevel(int level) {
            cloud.client_data.light_level = level;
            return this;
        }

        /**
         * Sets the sound played when the cloud spawns.
         *
         * @param soundId the sound identifier
         * @return this builder for method chaining
         */
        public CloudBuilder spawnSound(Identifier soundId) {
            cloud.spawn.sound = new Sound(soundId);
            return this;
        }
    }

    /**
     * Builder for spell impact effects.
     * <p>
     * Defines what happens when the spell hits a target.
     */
    public static class ImpactBuilder {
        /** The impact being built */
        private final Spell.Impact impact = new Spell.Impact();

        /**
         * Applies damage to the target.
         *
         * @param coefficient the spell power coefficient for damage scaling
         * @param knockback the knockback amount
         * @return this builder for method chaining
         */
        public ImpactBuilder damage(float coefficient, float knockback) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.DAMAGE;
            impact.action.damage = new Spell.Impact.Action.Damage();
            impact.action.damage.spell_power_coefficient = coefficient;
            impact.action.damage.knockback = knockback;
            return this;
        }

        /**
         * Sets the target on fire.
         *
         * @param duration the fire duration in seconds
         * @return this builder for method chaining
         */
        public ImpactBuilder fire(float duration) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.FIRE;
            impact.action.fire = new Spell.Impact.Action.Fire();
            impact.action.fire.duration = duration;
            return this;
        }

        /**
         * Heals the target.
         *
         * @param coefficient the spell power coefficient for healing scaling
         * @return this builder for method chaining
         */
        public ImpactBuilder heal(float coefficient) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.HEAL;
            impact.action.heal = new Spell.Impact.Action.Heal();
            impact.action.heal.spell_power_coefficient = coefficient;
            return this;
        }

        /**
         * Applies a status effect to the target.
         *
         * @param effectId the status effect identifier
         * @param duration the effect duration in seconds
         * @param amplifier the effect amplifier (level - 1)
         * @return this builder for method chaining
         */
        public ImpactBuilder statusEffect(String effectId, float duration, int amplifier) {
            impact.action = new Spell.Impact.Action();
            impact.action.type = Spell.Impact.Action.Type.STATUS_EFFECT;
            impact.action.status_effect = new Spell.Impact.Action.StatusEffect();
            impact.action.status_effect.effect_id = effectId;
            impact.action.status_effect.duration = duration;
            impact.action.status_effect.amplifier = amplifier;
            return this;
        }

        /**
         * Sets the sound played on impact.
         *
         * @param soundId the sound identifier
         * @return this builder for method chaining
         */
        public ImpactBuilder sound(Identifier soundId) {
            impact.sound = new Sound(soundId);
            return this;
        }

        /**
         * Sets the particle effects on impact.
         *
         * @param particles the particle batches to spawn
         * @return this builder for method chaining
         */
        public ImpactBuilder particles(ParticleBatch... particles) {
            impact.particles = particles;
            return this;
        }

        /**
         * Builds the final impact configuration.
         *
         * @return the completed impact
         */
        public Spell.Impact build() {
            return impact;
        }
    }

    /**
     * Builder for area-of-effect impact configuration.
     * <p>
     * Configures effects that apply to all entities in an area.
     */
    public static class AreaImpactBuilder {
        /** The area impact being built */
        private final Spell.AreaImpact area;

        /**
         * Creates a new area impact builder.
         *
         * @param area the area impact configuration to modify
         */
        public AreaImpactBuilder(Spell.AreaImpact area) {
            this.area = area;
        }

        /**
         * Sets the radius of the impact area.
         *
         * @param radius the radius in blocks
         * @return this builder for method chaining
         */
        public AreaImpactBuilder radius(float radius) {
            area.radius = radius;
            return this;
        }

        /**
         * Sets the particle effects for the area impact.
         *
         * @param particles the particle batches to spawn
         * @return this builder for method chaining
         */
        public AreaImpactBuilder particles(ParticleBatch... particles) {
            area.particles = particles;
            return this;
        }

        /**
         * Sets the sound played for the area impact.
         *
         * @param soundId the sound identifier
         * @return this builder for method chaining
         */
        public AreaImpactBuilder sound(Identifier soundId) {
            area.sound = new Sound(soundId);
            return this;
        }
    }

    /**
     * Builder for spell cost configuration.
     * <p>
     * Defines the resources required to cast the spell.
     */
    public static class CostBuilder {
        /** The cost configuration being built */
        private final Spell.Cost cost;

        /**
         * Creates a new cost builder.
         *
         * @param cost the cost configuration to modify
         */
        public CostBuilder(Spell.Cost cost) {
            this.cost = cost;
        }

        /**
         * Sets the cooldown duration before the spell can be cast again.
         *
         * @param duration the cooldown in seconds
         * @return this builder for method chaining
         */
        public CostBuilder cooldown(float duration) {
            cost.cooldown = new Spell.Cost.Cooldown();
            cost.cooldown.duration = duration;
            return this;
        }

        /**
         * Sets the exhaustion cost (affects hunger).
         *
         * @param amount the exhaustion amount
         * @return this builder for method chaining
         */
        public CostBuilder exhaust(float amount) {
            cost.exhaust = amount;
            return this;
        }

        /**
         * Sets the stamina cost for casting.
         * <p>
         * Only works if the cost object implements {@link IStaminaCost}.
         *
         * @param amount the stamina cost
         * @return this builder for method chaining
         */
        public CostBuilder stamina(float amount) {
            if (cost instanceof IStaminaCost staminaCost) {
                staminaCost.onepiece$setStaminaCost(amount);
            }
            return this;
        }

        /**
         * Sets the item cost for casting.
         *
         * @param itemId the item identifier
         * @param amount the number of items required
         * @return this builder for method chaining
         */
        public CostBuilder item(String itemId, int amount) {
            cost.item = new Spell.Cost.Item();
            cost.item.id = itemId;
            cost.item.amount = amount;
            return this;
        }
    }
}