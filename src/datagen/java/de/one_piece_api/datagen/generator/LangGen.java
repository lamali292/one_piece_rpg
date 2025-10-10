package de.one_piece_api.datagen.generator;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.init.MyItems;
import de.one_piece_api.init.MyKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class LangGen extends FabricLanguageProvider {
    public LangGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder builder) {
        for (var item : MyItems.ENTRIES) {
            builder.add(item.item(), item.title());
            for (var lore : item.loreTranslation()) {
                builder.add(lore.translationKey(), lore.line().text());
            }
        }
        builder.add("gui."+ OnePieceRPG.MOD_ID+".class.primary", "Primary");
        builder.add("gui."+ OnePieceRPG.MOD_ID+".class.passive", "Passive");
        builder.add("gui."+ OnePieceRPG.MOD_ID+".skill.skilltree", "Arbre de compétence");
        builder.add("gui."+ OnePieceRPG.MOD_ID+".skill.description", "Déscription :");
        builder.add("gui."+ OnePieceRPG.MOD_ID+".tab.devilfruit", "Fruit du démon");
        builder.add("gui."+ OnePieceRPG.MOD_ID+".tab.class", "Classes");
        builder.add("gui."+ OnePieceRPG.MOD_ID+".tab.haki", "Haki");
        builder.add("hud.cast_attempt_error.insufficient_stamina", "Insufficient Stamina!");

        builder.add("key."+ OnePieceRPG.MOD_ID+"."+ MyKeys.TOGGLE_COMBAT_MODE, "Activate Combat Mode");
        builder.add("category."+ OnePieceRPG.MOD_ID+".controls", "One Piece RPG");

    }
}
