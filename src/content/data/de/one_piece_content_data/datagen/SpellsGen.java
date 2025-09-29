package de.one_piece_content_data.datagen;

import de.one_piece_content_data.content.ExampleSpells;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.spell_engine.api.datagen.SpellGenerator;

import java.util.concurrent.CompletableFuture;

public class SpellsGen extends SpellGenerator {
    public SpellsGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateSpells(Builder builder) {
        for (var entry : ExampleSpells.ENTRIES) {
            builder.add(entry.id(), entry.spell());
        }
    }
}
