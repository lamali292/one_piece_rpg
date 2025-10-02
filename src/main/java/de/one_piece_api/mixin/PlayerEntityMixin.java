package de.one_piece_api.mixin;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.interfaces.IOnePiecePlayer;
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
public class PlayerEntityMixin implements IOnePiecePlayer {

    @Unique
    private static final TrackedData<String> ONE_PIECE_CLASS =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);

    @Unique
    private static final TrackedData<NbtCompound> SPELL_HOTBAR =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    @Unique
    private static final TrackedData<String> DEVIL_FRUIT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);


    @Unique
    private PlayerEntity onepiece$getSelf() {
        return (PlayerEntity) (Object) this;
    }

    // --- initDataTracker Injection ---
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onepiece$initTrackedData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(ONE_PIECE_CLASS, "");
        builder.add(SPELL_HOTBAR, new NbtCompound());
        builder.add(DEVIL_FRUIT, "");
    }

    @Unique
    private boolean combatMode = false;

    @Override
    public boolean onepiece$isCombatMode() {
        return combatMode;
    }

    @Override
    public void onepiece$setCombatMode(boolean combatMode) {
        this.combatMode = combatMode;
    }

    // --- IOnePiecePlayer Implementierung ---
    @Override
    public void onepiece$setSelectedSpellIds(List<String> spells) {
        NbtCompound data = new NbtCompound();
        for (int i = 0; i < spells.size(); i++) {
            data.putString("" + i, spells.get(i));
        }
        onepiece$getSelf().getDataTracker().set(SPELL_HOTBAR, data);
    }

    @Override
    public List<String> onepiece$getSelectedSpellIds() {
        NbtCompound data = onepiece$getSelf().getDataTracker().get(SPELL_HOTBAR);
        List<String> spells = new ArrayList<>();
        int slots = OnePieceRPG.getSpellSlots(onepiece$getSelf());
        for (int i = 0; i < slots; i++) {
            spells.add(data.getString("" + i));
        }
        return spells;
    }

    @Override
    public  List<RegistryEntry<Spell>> onepiece$getSelectedSpells() {
        Registry<Spell> registry = SpellRegistry.from(onepiece$getSelf().getWorld());
        return onepiece$getSelectedSpellIds().stream().map(id->{
            if (!id.isEmpty()) {
                return (RegistryEntry<Spell>) registry.getEntry(Identifier.of(id)).orElse(null);
            } else {
                return null;
            }
        }).toList();
    }

    @Override
    public String onepiece$getDevilFruit() {
        return onepiece$getSelf().getDataTracker().get(DEVIL_FRUIT);
    }

    @Override
    public void onepiece$setDevilFruit(String fruit) {
        onepiece$getSelf().getDataTracker().set(DEVIL_FRUIT, fruit);
    }

    @Override
    public Identifier onepiece$getOnePieceClass() {
        return Identifier.of(onepiece$getSelf().getDataTracker().get(ONE_PIECE_CLASS));
    }

    @Override
    public void onepiece$setOnePieceClass(Identifier className) {
        onepiece$getSelf().getDataTracker().set(ONE_PIECE_CLASS, className.toString());
    }

    // --- NBT Save ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onepiece$saveData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("OnePieceClass", onepiece$getOnePieceClass().toString());
        nbt.putString("DevilFruit", onepiece$getDevilFruit());
        nbt.put("OnePieceSpells", onepiece$getSelf().getDataTracker().get(SPELL_HOTBAR));
    }


    // --- NBT Load ---
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onepiece$loadData(NbtCompound nbt, CallbackInfo ci) {
        onepiece$setOnePieceClass(Identifier.of(nbt.getString("OnePieceClass")));
        onepiece$setDevilFruit(nbt.getString("DevilFruit"));
        NbtCompound spells = nbt.getCompound("OnePieceSpells");
        onepiece$getSelf().getDataTracker().set(SPELL_HOTBAR, spells);
    }
}
