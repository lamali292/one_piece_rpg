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
public class SpellPlayerMixin implements ISpellPlayer {



    @Unique
    private static final TrackedData<NbtCompound> SPELL_HOTBAR =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);



    @Unique
    private PlayerEntity onepiece$getSpellSelf() {
        return (PlayerEntity) (Object) this;
    }

    // --- initDataTracker Injection ---
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedSpellData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(SPELL_HOTBAR, new NbtCompound());
    }


    // --- IOnePiecePlayer Implementierung ---
    @Override
    public void onepiece$setSelectedSpellIds(List<String> spells) {
        NbtCompound data = new NbtCompound();
        for (int i = 0; i < spells.size(); i++) {
            data.putString("" + i, spells.get(i));
        }
        onepiece$getSpellSelf().getDataTracker().set(SPELL_HOTBAR, data);
    }

    @Override
    public List<String> onepiece$getSelectedSpellIds() {
        NbtCompound data = onepiece$getSpellSelf().getDataTracker().get(SPELL_HOTBAR);
        List<String> spells = new ArrayList<>();
        int slots = OnePieceRPG.getSpellSlots(onepiece$getSpellSelf());
        for (int i = 0; i < slots; i++) {
            spells.add(data.getString("" + i));
        }
        return spells;
    }

    @Override
    public  List<RegistryEntry<Spell>> onepiece$getSelectedSpells() {
        Registry<Spell> registry = SpellRegistry.from(onepiece$getSpellSelf().getWorld());
        return onepiece$getSelectedSpellIds().stream().map(id->{
            if (!id.isEmpty()) {
                return (RegistryEntry<Spell>) registry.getEntry(Identifier.of(id)).orElse(null);
            } else {
                return null;
            }
        }).toList();
    }

    // --- NBT Save ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onepiece$saveSpellData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("OnePieceSpells", onepiece$getSpellSelf().getDataTracker().get(SPELL_HOTBAR));
    }


    // --- NBT Load ---
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onepiece$loadSpellData(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound spells = nbt.getCompound("OnePieceSpells");
        onepiece$getSpellSelf().getDataTracker().set(SPELL_HOTBAR, spells);
    }
}
