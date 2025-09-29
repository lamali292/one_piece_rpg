package de.one_piece_api.datagen.generator;

import de.one_piece_api.OnePieceRPG;
import de.one_piece_api.registries.MyItems;
import de.one_piece_api.registries.MyKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.loader.impl.util.StringUtil;
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
        builder.add("gui."+ OnePieceRPG.MOD_ID+".skill.skilltree", "Skill Tree");
        builder.add("gui."+ OnePieceRPG.MOD_ID+".skill.description", "Description:");
        builder.add("key."+ OnePieceRPG.MOD_ID+"."+ MyKeys.TOGGLE_COMBAT_MODE, "Activate Combat Mode");
        builder.add("key."+ OnePieceRPG.MOD_ID+"."+ MyKeys.OPEN_SKILLS_KEY, "Open Skill Selection");
        builder.add("category."+ OnePieceRPG.MOD_ID+".controls", "One Piece RPG");

        String[] classes = {"fishman", "human", "mink"};

        for (String className : classes) {
            String displayName = StringUtil.capitalize(className);
            String description = "Auto-generated description for " + displayName + " class";

            builder.add("class."+ OnePieceRPG.MOD_ID+"." + className + ".name", displayName);
            builder.add("class."+ OnePieceRPG.MOD_ID+"." + className + ".description", description);
        }


    }
}
