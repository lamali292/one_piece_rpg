package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.IHidden;
import de.one_piece_api.mixin_interface.StyledConnection;
import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.common.SkillConnection;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.server.network.packets.out.ShowCategoryOutPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShowCategoryOutPacket.class, remap = false)
public class ShowCategoryOutPacketMixin {

    @Inject(
            method = "write(Lnet/minecraft/network/PacketByteBuf;Lnet/puffish/skillsmod/common/SkillConnection;)V",
            at = @At("TAIL"),
            remap = true  // Need to remap because PacketByteBuf is a Minecraft class
    )
    private static void write(PacketByteBuf buf, SkillConnection skill, CallbackInfo ci) {
        StyledConnection styled = (StyledConnection) (Object) skill;
        if (styled == null) return;
        var style = styled.onepiece$getStyle();
        buf.writeBoolean(style.isPresent());
        style.ifPresent(buf::writeIdentifier);
    }

    @Inject(
            method = "write(Lnet/minecraft/network/PacketByteBuf;Lnet/puffish/skillsmod/config/skill/SkillConfig;)V",
            at = @At("TAIL"),
            remap = true  // Need to remap because PacketByteBuf is a Minecraft class
    )
    private static void write(PacketByteBuf buf, SkillConfig skill, CallbackInfo ci) {
        IHidden styled = (IHidden) (Object) skill;
        if (styled == null) return;
        buf.writeBoolean(styled.onepiece$isHidden());
    }
}