package de.one_piece_api.mixin;

import de.one_piece_api.hud.ISkillsClientMod;
import net.minecraft.client.option.KeyBinding;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import net.puffish.skillsmod.client.keybinding.KeyBindingHandler;
import net.puffish.skillsmod.client.keybinding.KeyBindingReceiver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

@Mixin(SkillsClientMod.class)
public class SkillsClientModMixin implements ISkillsClientMod {

    @Override
    public ClientSkillScreenData onepiece$getScreenData() {
        try {
            // Find the ClientSkillScreenData field by type (works regardless of field name)
            for (Field field : SkillsClientMod.class.getDeclaredFields()) {
                if (field.getType() == ClientSkillScreenData.class) {
                    field.setAccessible(true);
                    return (ClientSkillScreenData) field.get(this);
                }
            }
            throw new RuntimeException("ClientSkillScreenData field not found in SkillsClientMod");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access ClientSkillScreenData field", e);
        }
    }

    @Redirect(
            method = "setup(Lnet/puffish/skillsmod/client/setup/ClientRegistrar;Lnet/puffish/skillsmod/client/event/ClientEventReceiver;Lnet/puffish/skillsmod/client/keybinding/KeyBindingReceiver;Lnet/puffish/skillsmod/client/network/ClientPacketSender;)V",
            at = @At(value = "INVOKE", target = "Lnet/puffish/skillsmod/client/keybinding/KeyBindingReceiver;registerKeyBinding(Lnet/minecraft/client/option/KeyBinding;Lnet/puffish/skillsmod/client/keybinding/KeyBindingHandler;)V")
    )
    private static void redirectKeyBindingRegistration(KeyBindingReceiver receiver, KeyBinding keyBinding, KeyBindingHandler action) {
        // Do nothing
    }
}