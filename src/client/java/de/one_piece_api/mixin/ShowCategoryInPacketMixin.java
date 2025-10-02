package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IHidden;
import de.one_piece_api.interfaces.StyledConnection;
import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import net.puffish.skillsmod.client.network.packets.in.ShowCategoryInPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShowCategoryInPacket.class)
public class ShowCategoryInPacketMixin {

    @Inject(
            method = "readSkillConnection",
            at = @At("RETURN"),
            remap = false
    )
    private static void readSkillConnection(PacketByteBuf buf, CallbackInfoReturnable<ClientSkillConnectionConfig> cir) {
        ClientSkillConnectionConfig connection = cir.getReturnValue();
        boolean hasStyle = buf.readBoolean();
        if (hasStyle) {
            var style = buf.readIdentifier();
            ((StyledConnection) (Object) connection).onepiece$setStyle(style);
        }
    }


    @Inject(
            method = "readSkill",
            at = @At("RETURN"),
            remap = false
    )
    private static void readSkill(PacketByteBuf buf, CallbackInfoReturnable<ClientSkillConfig> cir) {
        ClientSkillConfig connection = cir.getReturnValue();
        boolean isHidden = buf.readBoolean();
        ((IHidden) (Object) connection).onepiece$setHidden(isHidden);
    }

}
