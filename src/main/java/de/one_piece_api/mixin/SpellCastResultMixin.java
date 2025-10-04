package de.one_piece_api.mixin;

import net.spell_engine.internals.casting.SpellCast;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = SpellCast.Attempt.Result.class, remap = false)
@Unique
public abstract class SpellCastResultMixin {
    @Shadow
    @Final
    @Mutable
    private static SpellCast.Attempt.Result[] $VALUES;

    @Unique
    private static final SpellCast.Attempt.Result INSUFFICIENT_STAMINA =
            onepiece$addVariant("INSUFFICIENT_STAMINA");

    @Invoker("<init>")
    private static SpellCast.Attempt.Result onepiece$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static SpellCast.Attempt.Result onepiece$addVariant(String name) {
        if ($VALUES == null) {
            return null;
        }
        ArrayList<SpellCast.Attempt.Result> variants = new ArrayList<>(Arrays.asList($VALUES));
        SpellCast.Attempt.Result result = onepiece$invokeInit(name, variants.size());
        variants.add(result);
        $VALUES = variants.toArray(new SpellCast.Attempt.Result[0]);
        return result;
    }
}