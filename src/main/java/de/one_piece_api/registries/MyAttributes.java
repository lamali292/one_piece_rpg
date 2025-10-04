package de.one_piece_api.registries;

import de.one_piece_api.OnePieceRPG;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class MyAttributes {


    public static final RegistryEntry<EntityAttribute> MAX_STAMINA = register(
            "max_stamina",
            new ClampedEntityAttribute(
                    "player."+OnePieceRPG.MOD_ID+".max_stamina",
                    100.0,  // Default value
                    0.0,    // Min value
                    1024.0  // Max value
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> STAMINA_REGEN = register(
            "stamina_regen",
            new ClampedEntityAttribute(
                    "player."+OnePieceRPG.MOD_ID+"stamina_regen",
                    1.0,
                    0.0,
                    1024.0
            ).setTracked(true)
    );

    private static RegistryEntry<EntityAttribute> register(String name, EntityAttribute attribute) {
        return Registry.registerReference(
                Registries.ATTRIBUTE,
                OnePieceRPG.id(name),
                attribute
        );
    }

    public static void initialize() {
        // Called during mod initialization to ensure attributes are registered

    }
}
