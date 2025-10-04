package de.one_piece_api.mixin;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.interfaces.IClassPlayer;
import de.one_piece_api.interfaces.ICombatPlayer;
import de.one_piece_api.interfaces.IDevilFruitPlayer;
import de.one_piece_api.interfaces.ISpellPlayer;
import de.one_piece_api.items.DevilFruitItem;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public class ClassPlayerMixin implements IClassPlayer {


    @Unique
    private static final TrackedData<String> ONE_PIECE_CLASS =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);

    @Unique
    private PlayerEntity onepiece$getClassSelf() {
        return (PlayerEntity) (Object) this;
    }

    // --- initDataTracker Injection ---
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedClassData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(ONE_PIECE_CLASS, "");
    }


    @Override
    public Identifier onepiece$getOnePieceClass() {
        return Identifier.of(onepiece$getClassSelf().getDataTracker().get(ONE_PIECE_CLASS));
    }

    @Override
    public void onepiece$setOnePieceClass(Identifier className) {
        onepiece$getClassSelf().getDataTracker().set(ONE_PIECE_CLASS, className.toString());
    }

    // --- NBT Save ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onepiece$saveClassData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("OnePieceClass", onepiece$getOnePieceClass().toString());
    }


    // --- NBT Load ---
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onepiece$loadClassData(NbtCompound nbt, CallbackInfo ci) {
        onepiece$setOnePieceClass(Identifier.of(nbt.getString("OnePieceClass")));
    }
}
