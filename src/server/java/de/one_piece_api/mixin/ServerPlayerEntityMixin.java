package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IClassPlayer;
import de.one_piece_api.interfaces.ISpellPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
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
        IClassPlayer thisClassPlayer = (IClassPlayer) this.getSelf();
        IClassPlayer thatClassPlayer = (IClassPlayer) oldPlayer;

        ISpellPlayer thisSpellPlayer = (ISpellPlayer) this.getSelf();
        ISpellPlayer thatSpellPlayer = (ISpellPlayer) oldPlayer;

        thisClassPlayer.onepiece$setOnePieceClass(thatClassPlayer.onepiece$getOnePieceClass());
        thisSpellPlayer.onepiece$setSelectedSpellIds(thatSpellPlayer.onepiece$getSelectedSpellIds());
    }


}
