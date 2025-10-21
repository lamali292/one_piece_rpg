package de.one_piece_api.mixin;

import de.one_piece_api.util.ClientData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Listens for DataTracker updates on the client player
 * to trigger UI invalidation when class or devil fruit changes
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerDataTrackerMixin {

    @Inject(method = "onTrackedDataSet", at = @At("HEAD"))
    private void onDataTrackerUpdate(TrackedData<?> data, CallbackInfo ci) {
        // When ANY tracked data changes on the client player, check if it's our data
        // Since we can't directly compare the TrackedData instances from the mixin,
        // we invalidate on any change and let the ScreenDataManager filter it
        ClientData.invalidate(ClientData.DataInvalidationType.CLASS_CONFIG);
    }
}