package de.one_piece_api.network;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.config.DevilFruitConfig;
import de.one_piece_api.content.DataLoader;
import de.one_piece_api.interfaces.IClassPlayer;
import de.one_piece_api.interfaces.ICombatPlayer;
import de.one_piece_api.interfaces.ISpellPlayer;
import de.one_piece_api.util.SkillHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_engine.internals.container.SpellContainerSource;

public class ServerPacketHandler {


    public static void handleSetClassPayload(SetClassPayload payload, ServerPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.SERVER_PAYLOAD_MARKER, "{} set class: {}", context.player().getName().getString(), payload.className());

        context.server().execute(() -> {
            if (context.player() instanceof IClassPlayer player) {
                player.onepiece$setOnePieceClass(payload.className());
            }
        });
    }

    public static void handleSetCombatModePayload(SetCombatModePayload payload, ServerPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.SERVER_PAYLOAD_MARKER, "{} swapped combat mode: {}", context.player().getName().getString(), payload.mode());

        context.server().execute(() -> {
            if (context.player() instanceof ICombatPlayer player) {
                player.onepiece$setCombatMode(payload.mode());
                SpellContainerSource.update(context.player());
            }
        });

    }

    public static void handleSetSpellsPayload(SetSpellsPayload payload, ServerPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.SERVER_PAYLOAD_MARKER, "{} swapped spell hotbar: {}", context.player().getName().getString(), payload.spells());
        context.server().execute(() -> {
            if (context.player() instanceof ISpellPlayer player) {
                player.onepiece$setSelectedSpellIds(payload.spells());
            }
        });
    }

    public static void handleClassConfigRequest(ClassConfigPayload.Request request, ServerPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.SERVER_PAYLOAD_MARKER, "{} requested classes", context.player().getName().getString());

        context.server().execute(() -> context.responseSender().sendPacket(new ClassConfigPayload(DataLoader.CLASS_LOADER.getItems())));
    }

    public static void handleDevilFruitRequest(DevilFruitPayload.Request request, ServerPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.SERVER_PAYLOAD_MARKER, "{} requested devil fruits: {}", context.player().getName().getString(), request.identifier().toString() );
        context.server().execute(() -> {
            Identifier id = request.identifier();
            DevilFruitConfig config = DataLoader.DEVIL_FRUIT_LOADER.getItems().get(id);
            if (config == null) {
                config = DevilFruitConfig.DEFAULT;
            }
            context.responseSender().sendPacket(new DevilFruitPayload(id, config));
        });
    }

    public static final ParticleBatch[] RESET_PARTICLES = new ParticleBatch[] {
            new ParticleBatch(
                    SpellEngineParticles.MagicParticles.get(
                            SpellEngineParticles.MagicParticles.Shape.SPARK,
                            SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                    ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                    30, 0.2F, 0.25F)
                    .color(Color.from(0x8000ff).toRGBA()),
            new ParticleBatch(
                    SpellEngineParticles.MagicParticles.get(
                            SpellEngineParticles.MagicParticles.Shape.SPARK,
                            SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                    ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                    30, 0.2F, 0.25F)
                    .color(Color.from(0x8000ff).toRGBA())
                    .invert()
    };

    public static void handleUi(UiPayload uiPayload, ServerPlayNetworking.Context context) {
        OnePieceRPG.debug(OnePieceRPG.SERVER_PAYLOAD_MARKER, "{} clicked ui: {}", context.player().getName().getString(), uiPayload.ui());
        ServerPlayerEntity player = context.player();
        context.server().execute(() -> {
            switch (uiPayload.ui()) {
                case "reset" -> {
                    SkillHelper.respec(player);
                    ParticleHelper.sendBatches(player, RESET_PARTICLES);
                    context.responseSender().sendPacket(uiPayload);
                }
            }
        });

    }
}
