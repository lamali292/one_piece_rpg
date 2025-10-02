package de.one_piece_api.mixin;

import de.one_piece_api.interfaces.IOnePiecePlayer;
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
        IOnePiecePlayer thisPlayer = (IOnePiecePlayer) this.getSelf();
        IOnePiecePlayer thatPlayer = (IOnePiecePlayer) oldPlayer;
        thisPlayer.onepiece$setOnePieceClass(thatPlayer.onepiece$getOnePieceClass());
        thisPlayer.onepiece$setSelectedSpellIds(thatPlayer.onepiece$getSelectedSpellIds());
    }


}
