package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.StyledConnection;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.common.SkillConnection;
import net.puffish.skillsmod.config.skill.SkillConnectionConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsGroupConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = SkillConnectionsGroupConfig.class, remap = false)
public class SkillConnectionsGroupConfigMixin {

    @Inject(
            method = "build",
            at = @At("RETURN"),
            remap = false
    )
    private static void transferStyles(
            List<SkillConnectionConfig> bidirectional,
            List<SkillConnectionConfig> unidirectional,
            CallbackInfoReturnable<SkillConnectionsGroupConfig> cir
    ) {
        var groupConfig = cir.getReturnValue();

        // Create a map of skill pairs to their styles
        Map<String, Identifier> styleMap = new HashMap<>();

        // Collect styles from both bidirectional and unidirectional configs
        for (SkillConnectionConfig config : bidirectional) {
            ((StyledConnection) (Object) config).onepiece$getStyle().ifPresent(style -> {
                String key = makeKey(config.skillAId(), config.skillBId());
                styleMap.put(key, style);
            });
        }

        for (SkillConnectionConfig config : unidirectional) {
            ((StyledConnection) (Object) config).onepiece$getStyle().ifPresent(style -> {
                String key = makeKey(config.skillAId(), config.skillBId());
                styleMap.put(key, style);
            });
        }

        // Apply styles to created SkillConnections
        for (SkillConnection connection : groupConfig.getAll()) {
            String key = makeKey(connection.skillAId(), connection.skillBId());
            Identifier style = styleMap.get(key);
            if (style != null) {
                ((StyledConnection) (Object) connection).onepiece$setStyle(style);
            }
        }
    }

    @Unique
    private static String makeKey(String a, String b) {
        // Normalize the key so order doesn't matter
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }
}