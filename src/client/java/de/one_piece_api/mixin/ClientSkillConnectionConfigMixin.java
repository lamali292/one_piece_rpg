package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.StyledConnection;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

/**
 * Mixin that adds style functionality to {@link ClientSkillConnectionConfig}.
 * <p>
 * This mixin implements the {@link StyledConnection} interface, allowing skill
 * connections to have custom visual styles identified by an {@link Identifier}.
 *
 * @see StyledConnection
 * @see ClientSkillConnectionConfig
 */
@Mixin(value = ClientSkillConnectionConfig.class, remap = false)
public class ClientSkillConnectionConfigMixin implements StyledConnection {

    /**
     * The style identifier for this skill connection.
     * May be {@code null} if no style is set.
     */
    @Unique
    private Identifier style;

    /**
     * Gets the style identifier for this skill connection.
     *
     * @return an {@link Optional} containing the style {@link Identifier},
     *         or {@link Optional#empty()} if no style is set
     */
    @Override
    public Optional<Identifier> onepiece$getStyle() {
        return Optional.ofNullable(style);
    }

    /**
     * Sets the style identifier for this skill connection.
     *
     * @param style the {@link Identifier} representing the style to apply,
     *              or {@code null} to clear the style
     */
    @Override
    public void onepiece$setStyle(Identifier style) {
        this.style = style;
    }
}