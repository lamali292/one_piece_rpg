package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.StyledConnection;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonArray;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.config.skill.SkillConnectionConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Optional;

@Mixin(value = SkillConnectionConfig.class, remap = false)
public class SkillConnectionConfigMixin implements StyledConnection {

    @Unique
    private Identifier style;

    @Override
    public Optional<Identifier> onepiece$getStyle() {
        return Optional.ofNullable(style);
    }

    @Override
    public void onepiece$setStyle(Identifier style) {
        this.style = style;
    }

    @Inject(
            method = "parse(Lnet/puffish/skillsmod/api/json/JsonArray;Lnet/puffish/skillsmod/config/skill/SkillsConfig;)Lnet/puffish/skillsmod/api/util/Result;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void handleThreeElements(
            JsonArray rootArray,
            SkillsConfig skills,
            CallbackInfoReturnable<Result<SkillConnectionConfig, Problem>> cir
    ) {
        int size = rootArray.getSize();

        if (size == 3) {
            var problems = new ArrayList<Problem>();
            var optIds = rootArray.getAsList((i, element) -> {
                        if (i >= 2) {
                            return Result.success("dummy");
                        }

                        return element.getAsString().andThen(id -> {
                            if (skills.getById(id).isEmpty()) {
                                return Result.failure(element.getPath().createProblem("Expected a valid skill"));
                            } else {
                                return Result.success(id);
                            }
                        });
                    })
                    .ifFailure(problems::addAll)
                    .getSuccess();

            if (problems.isEmpty()) {
                var ids = optIds.orElseThrow();
                var config = new SkillConnectionConfig(ids.get(0), ids.get(1));

                // Parse style from 3rd element
                rootArray.stream()
                        .skip(2)
                        .findFirst()
                        .ifPresent(element -> {
                            element.getAsString().ifSuccess(styleStr -> {
                                try {
                                    Identifier identifier = Identifier.of(styleStr);
                                    ((StyledConnection) (Object) config).onepiece$setStyle(identifier);
                                } catch (Exception ignored) {
                                }
                            });
                        });

                cir.setReturnValue(Result.success(config));
            } else {
                cir.setReturnValue(Result.failure(Problem.combine(problems)));
            }
        }
        // For 2 elements or other sizes, let the original method handle it
    }
}