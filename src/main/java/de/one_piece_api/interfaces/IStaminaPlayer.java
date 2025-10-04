package de.one_piece_api.interfaces;

import de.one_piece_api.registries.MyAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;

public interface IStaminaPlayer {


    void onepiece$setStamina(double stamina);
    void onepiece$addStamina(double stamina);
    void onepiece$removeStamina(double stamina);
    double onepiece$getStamina();
    void onepiece$updateStamina();
}
