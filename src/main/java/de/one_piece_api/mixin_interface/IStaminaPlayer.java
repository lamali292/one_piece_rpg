package de.one_piece_api.mixin_interface;

public interface IStaminaPlayer {


    void onepiece$setStamina(double stamina);
    void onepiece$addStamina(double stamina);
    void onepiece$removeStamina(double stamina);
    double onepiece$getStamina();
    void onepiece$updateStamina();
}
