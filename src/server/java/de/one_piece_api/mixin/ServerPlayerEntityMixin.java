package de.one_piece_api.mixin;

import de.one_piece_api.mixin_interface.IClassPlayer;
import de.one_piece_api.mixin_interface.ICombatPlayer;
import de.one_piece_api.mixin_interface.ISpellPlayer;
import de.one_piece_api.mixin_interface.IXpPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.spell_engine.internals.container.SpellContainerSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Unique
    private ServerPlayerEntity getSelf() {
        return (ServerPlayerEntity)(Object)this;
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        PlayerEntity thisPlayer = this.getSelf();

        IClassPlayer thisClassPlayer = (IClassPlayer) this.getSelf();
        IClassPlayer thatClassPlayer = (IClassPlayer) oldPlayer;

        ISpellPlayer thisSpellPlayer = (ISpellPlayer) this.getSelf();
        ISpellPlayer thatSpellPlayer = (ISpellPlayer) oldPlayer;

        IXpPlayer thisXpPlayer = (IXpPlayer) this.getSelf();
        IXpPlayer thatXpPlayer = (IXpPlayer) oldPlayer;

        ICombatPlayer thisCombatPlayer = (ICombatPlayer) this.getSelf();
        ICombatPlayer thatCombatPlayer = (ICombatPlayer) oldPlayer;

        thisClassPlayer.onepiece$setOnePieceClass(thatClassPlayer.onepiece$getOnePieceClass());
        thisSpellPlayer.onepiece$setSelectedSpellIds(thatSpellPlayer.onepiece$getSelectedSpellIds());
        thisXpPlayer.onepiece$setXpTimeConfig(thatXpPlayer.onepiece$getXpTimeConfig());
        thisXpPlayer.onepiece$setTicksSinceLastXp(thatXpPlayer.onepiece$getTicksSinceLastXp());
        thisCombatPlayer.onepiece$setCombatMode(thatCombatPlayer.onepiece$isCombatMode());

        SpellContainerSource.Owner thisOwner = (SpellContainerSource.Owner) this.getSelf();
        SpellContainerSource.Owner thatOwner = (SpellContainerSource.Owner) oldPlayer;
        thisOwner.setSpellContainers(thatOwner.getSpellContainers());
        thisOwner.serverSideSpellContainers().clear();
        thisOwner.serverSideSpellContainers().putAll(thatOwner.serverSideSpellContainers());
        SpellContainerSource.setDirtyServerSide(this.getSelf());
        SpellContainerSource.syncServerSideContainers(this.getSelf());
    }


}
