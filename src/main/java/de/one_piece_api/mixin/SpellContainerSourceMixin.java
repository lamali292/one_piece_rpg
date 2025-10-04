package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.ICombatPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Mixin(SpellContainerSource.class)
public class SpellContainerSourceMixin {

    @Shadow
    private static void updateEquipmentSets(PlayerEntity player, ArrayList<SpellContainerSource.SourcedContainer> allContainers) {

    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private static void update(PlayerEntity player, CallbackInfo ci) {
        var owner = (SpellContainerSource.Owner) player;
        var allContainers = new ArrayList<SpellContainerSource.SourcedContainer>();
        for (var entry : SpellContainerSource.sources) {
            var freshContainers = entry.source().getSpellContainers(player, entry.name());
            allContainers.addAll(freshContainers);
        }
        for (var entry : owner.serverSideSpellContainers().entrySet()) {
            allContainers.add(new SpellContainerSource.SourcedContainer(entry.getKey(), null, entry.getValue()));
        }

        owner.spellModifierCache().clear();
        // equipment sets
        updateEquipmentSets(player, allContainers);

        boolean combat = ((ICombatPlayer) player).onepiece$isCombatMode();

        SpellContainer activeContainer;
        List<RegistryEntry<Spell>> activeSpells;

        if (combat) {
            var merged = SpellContainerSource.mergedContainerSources(
                    allContainers,
                    true,
                    null,
                    Spell.Type.ACTIVE,
                    player.getWorld()
            );
            activeContainer = merged.container();
            activeSpells = merged.spells();
        } else {
            activeContainer = SpellContainer.EMPTY;
            activeSpells = List.of();
        }

        List<RegistryEntry<Spell>> passiveSpells = SpellContainerSource.mergedContainerSources(
                allContainers,
                null,
                Spell.Type.PASSIVE,
                player.getWorld()
        );

        var registry = SpellRegistry.from(player.getWorld());
        LinkedHashSet<RegistryEntry<Spell>> modifiers = new LinkedHashSet<>();
        for (var container : allContainers) {
            var spellContainer = container.container();
            for (var idString : spellContainer.spell_ids()) {
                var id = Identifier.of(idString);
                var spell = registry.getEntry(id).orElse(null);
                if (spell != null && spell.value().type == Spell.Type.MODIFIER) {
                    modifiers.add(spell);
                }
            }
        }

        ((SpellContainerSource.Owner) player).setSpellContainers(
                new SpellContainerSource.Result(
                        activeContainer,
                        activeSpells,
                        passiveSpells,
                        modifiers.stream().toList(),
                        allContainers
                )
        );

        ci.cancel();
    }

}
