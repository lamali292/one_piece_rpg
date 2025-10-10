package de.one_piece_api.init;

import de.one_piece_api.OnePieceRPG;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
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

    public static final RegistryEntry<EntityAttribute> STAMINA_BASE_REGEN = register(
            "stamina_base_regen",
            new ClampedEntityAttribute(
                    "player."+OnePieceRPG.MOD_ID+"stamina_base_regen",
                    1.0,
                    0.0,
                    1024.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> STAMINA_CROUCH_ADD = register(
            "stamina_crouch_add",
            new ClampedEntityAttribute(
                    "player."+OnePieceRPG.MOD_ID+"stamina_crouch_add",
                    0.0,
                    0.0,
                    1024.0
            ).setTracked(true)
    );

    public static final RegistryEntry<EntityAttribute> STAMINA_CROUCH_MULT = register(
            "stamina_crouch_mult",
            new ClampedEntityAttribute(
                    "player."+OnePieceRPG.MOD_ID+"stamina_crouch_mult",
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
