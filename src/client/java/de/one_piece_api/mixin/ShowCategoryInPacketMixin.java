package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.ISkillTypeProvider;
import de.one_piece_api.mixin_interface.SkillType;
import de.one_piece_api.mixin_interface.StyledConnection;
import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import net.puffish.skillsmod.client.network.packets.in.ShowCategoryInPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for {@link ShowCategoryInPacket} that extends packet deserialization to include custom data.
 * <p>
 * This mixin injects additional data reading for skill connections and skills, allowing
 * custom style identifiers and hidden states to be transmitted over the network.
 *
 * @see ShowCategoryInPacket
 * @see StyledConnection
 * @see ISkillTypeProvider
 */
@Mixin(value = ShowCategoryInPacket.class, remap = false)
public class ShowCategoryInPacketMixin {

    /**
     * Reads custom style data for skill connections from the packet buffer.
     * <p>
     * After the default skill connection is deserialized, this method checks if
     * a custom style identifier is present in the packet and applies it to the connection.
     *
     * @param buf the packet buffer containing serialized data
     * @param cir callback info returning the deserialized {@link ClientSkillConnectionConfig}
     */
    @Inject(
            method = "readSkillConnection",
            at = @At("RETURN"),
            remap = true  // Method signature contains PacketByteBuf (Minecraft class)
    )
    private static void readSkillConnection(PacketByteBuf buf, CallbackInfoReturnable<ClientSkillConnectionConfig> cir) {
        ClientSkillConnectionConfig connection = cir.getReturnValue();
        boolean hasStyle = buf.readBoolean();
        if (hasStyle) {
            var style = buf.readIdentifier();
            ((StyledConnection) (Object) connection).onepiece$setStyle(style);
        }
    }

    /**
     * Reads custom hidden state for skills from the packet buffer.
     * <p>
     * After the default skill is deserialized, this method reads the hidden state
     * flag from the packet and applies it to the skill configuration.
     *
     * @param buf the packet buffer containing serialized data
     * @param cir callback info returning the deserialized {@link ClientSkillConfig}
     */
    @Inject(
            method = "readSkill",
            at = @At("RETURN"),
            remap = true  // Method signature contains PacketByteBuf (Minecraft class)
    )
    private static void readSkill(PacketByteBuf buf, CallbackInfoReturnable<ClientSkillConfig> cir) {
        ClientSkillConfig connection = cir.getReturnValue();
        SkillType isHidden = buf.readEnumConstant(SkillType.class);
        ((ISkillTypeProvider) (Object) connection).onepiece$setSkillType(isHidden);
    }

}