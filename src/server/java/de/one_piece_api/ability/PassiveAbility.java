package de.one_piece_api.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A simple passive ability system that triggers effects based on conditions.
 * <p>
 * Unlike spells, passive abilities are lightweight and designed specifically
 * for continuous or conditional effects without the overhead of the spell system.
 */
public class PassiveAbility {
    private final Identifier id;
    private final String name;
    private final String description;
    private final Predicate<LivingEntity> condition;
    private final List<Effect> effects;
    private final int checkInterval; // How often to check condition (in ticks)

    private PassiveAbility(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.condition = builder.condition;
        this.effects = builder.effects;
        this.checkInterval = builder.checkInterval;
    }

    /**
     * Checks if the ability should activate for the given entity.
     *
     * @param entity the entity to check
     * @return true if the condition is met
     */
    public boolean shouldActivate(LivingEntity entity) {
        return condition.test(entity);
    }

    /**
     * Applies all effects to the entity.
     *
     * @param entity the entity to apply effects to
     */
    public void applyEffects(LivingEntity entity) {
        for (Effect effect : effects) {
            effect.apply(entity);
        }
    }

    /**
     * Ticks the ability for the given entity.
     * Checks conditions and applies effects if needed.
     *
     * @param entity the entity
     * @param tickCount the current tick count
     */
    public void tick(LivingEntity entity, long tickCount) {
        if (tickCount % checkInterval == 0) {
            if (shouldActivate(entity)) {
                applyEffects(entity);
            }
        }
    }

    // Getters
    public Identifier getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCheckInterval() { return checkInterval; }

    /**
     * Represents an effect that can be applied by a passive ability.
     */
    public interface Effect {
        void apply(LivingEntity entity);
    }

    /**
     * Status effect implementation.
     */
    public static class StatusEffectEffect implements Effect {
        private final RegistryEntry<StatusEffect> effect;
        private final int duration;
        private final int amplifier;
        private final boolean ambient;
        private final boolean showParticles;
        private final boolean showIcon;

        public StatusEffectEffect(RegistryEntry<StatusEffect> effect, int duration, int amplifier,
                                  boolean ambient, boolean showParticles, boolean showIcon) {
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
            this.ambient = ambient;
            this.showParticles = showParticles;
            this.showIcon = showIcon;
        }

        @Override
        public void apply(LivingEntity entity) {
            entity.addStatusEffect(new StatusEffectInstance(
                    effect, duration, amplifier, ambient, showParticles, showIcon
            ));
        }
    }

    /**
     * Custom action effect that executes arbitrary code.
     */
    public static class CustomEffect implements Effect {
        private final java.util.function.Consumer<LivingEntity> action;

        public CustomEffect(java.util.function.Consumer<LivingEntity> action) {
            this.action = action;
        }

        @Override
        public void apply(LivingEntity entity) {
            action.accept(entity);
        }
    }

    /**
     * Builder for creating passive abilities.
     */
    public static class Builder {
        private final Identifier id;
        private String name;
        private String description;
        private Predicate<LivingEntity> condition = entity -> true;
        private final List<Effect> effects = new ArrayList<>();
        private int checkInterval = 1; // Check every tick by default

        public Builder(Identifier id) {
            this.id = id;
            this.name = id.getPath();
            this.description = "";
        }

        /**
         * Sets the display name of the ability.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description of the ability.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the condition that must be met for the ability to activate.
         */
        public Builder condition(Predicate<LivingEntity> condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Adds a status effect to apply when the ability activates.
         */
        public Builder addStatusEffect(RegistryEntry<StatusEffect> effect, int durationTicks, int amplifier) {
            return addStatusEffect(effect, durationTicks, amplifier, false, false, true);
        }

        /**
         * Adds a status effect with full control over parameters.
         */
        public Builder addStatusEffect(RegistryEntry<StatusEffect> effect, int durationTicks, int amplifier,
                                       boolean ambient, boolean showParticles, boolean showIcon) {
            effects.add(new StatusEffectEffect(effect, durationTicks, amplifier,
                    ambient, showParticles, showIcon));
            return this;
        }

        /**
         * Adds a custom effect action.
         */
        public Builder addCustomEffect(java.util.function.Consumer<LivingEntity> action) {
            effects.add(new CustomEffect(action));
            return this;
        }

        /**
         * Sets how often (in ticks) to check the condition.
         * Default is 1 (every tick).
         */
        public Builder checkInterval(int ticks) {
            this.checkInterval = ticks;
            return this;
        }

        /**
         * Builds the passive ability.
         */
        public PassiveAbility build() {
            if (effects.isEmpty()) {
                throw new IllegalStateException("Passive ability must have at least one effect");
            }
            return new PassiveAbility(this);
        }
    }
}