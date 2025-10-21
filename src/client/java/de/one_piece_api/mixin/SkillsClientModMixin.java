package de.one_piece_api.mixin;

import de.one_piece_api.network.ClientPacketHandler;
import de.one_piece_api.util.ClientData;
import de.one_piece_api.util.ISkillsClientMod;
import net.minecraft.client.option.KeyBinding;
import net.puffish.skillsmod.client.SkillsClientMod;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import net.puffish.skillsmod.client.keybinding.KeyBindingHandler;
import net.puffish.skillsmod.client.keybinding.KeyBindingReceiver;
import net.puffish.skillsmod.client.network.packets.in.ExperienceUpdateInPacket;
import net.puffish.skillsmod.client.network.packets.in.PointsUpdateInPacket;
import net.puffish.skillsmod.client.network.packets.in.SkillUpdateInPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * Mixin for {@link SkillsClientMod} that provides access to internal data and modifies keybinding registration.
 * <p>
 * This mixin implements {@link ISkillsClientMod} to expose the client skill screen data and
 * redirects keybinding registration to prevent default keybindings from being registered.
 *
 * @see SkillsClientMod
 * @see ISkillsClientMod
 */
@Mixin(value = SkillsClientMod.class, remap = false)
public class SkillsClientModMixin implements ISkillsClientMod {

    /**
     * Retrieves the client skill screen data using reflection.
     * <p>
     * This method uses reflection to access the private {@link ClientSkillScreenData} field
     * in {@link SkillsClientMod}, working regardless of the field's name by searching for
     * the field by its type.
     *
     * @return the {@link ClientSkillScreenData} instance from the skills client mod
     * @throws RuntimeException if the field cannot be found or accessed
     */
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

    /**
     * Prevents default keybinding registration for the skills screen.
     * <p>
     * This redirect intercepts the keybinding registration call during setup and
     * prevents it from executing, allowing custom keybinding handling to take over.
     *
     * @param receiver the keybinding receiver that would register the binding
     * @param keyBinding the keybinding to register
     * @param action the handler for the keybinding action
     */
    @Redirect(
            method = "setup(Lnet/puffish/skillsmod/client/setup/ClientRegistrar;Lnet/puffish/skillsmod/client/event/ClientEventReceiver;Lnet/puffish/skillsmod/client/keybinding/KeyBindingReceiver;Lnet/puffish/skillsmod/client/network/ClientPacketSender;)V",
            at = @At(value = "INVOKE", target = "Lnet/puffish/skillsmod/client/keybinding/KeyBindingReceiver;registerKeyBinding(Lnet/minecraft/client/option/KeyBinding;Lnet/puffish/skillsmod/client/keybinding/KeyBindingHandler;)V"), remap = false
    )
    private static void redirectKeyBindingRegistration(KeyBindingReceiver receiver, KeyBinding keyBinding, KeyBindingHandler action) {
        // Do nothing - prevents default keybinding registration
    }



    @Inject(method = "onPointsUpdatePacket", at = @At("TAIL"), remap = false)
    private void onPointsUpdatePacket(PointsUpdateInPacket packet, CallbackInfo ci) {
        ClientPacketHandler.handlePointsUpdate(packet);
    }

    @Inject(method = "onExperienceUpdatePacket", at = @At("TAIL"), remap = false)
    private void onExperienceUpdatePacket(ExperienceUpdateInPacket packet, CallbackInfo ci) {
        ClientPacketHandler.handleExperienceUpdate(packet);
    }

    @Inject(method = "onSkillUpdatePacket", at = @At("TAIL"), remap = false)
    private void onSkillUpdatePacket(SkillUpdateInPacket packet, CallbackInfo ci) {
        ClientPacketHandler.handleSkillUpdate(packet);
    }
}