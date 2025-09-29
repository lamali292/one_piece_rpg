package de.one_piece_content_data.datagen;

import de.one_piece_content.ExampleMod;
import de.one_piece_content_data.content.ExampleSkills;
import de.one_piece_content_data.content.ExampleSpells;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class LangGen extends FabricLanguageProvider {
    public LangGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder builder) {
        for (var skill : ExampleSkills.ENTRIES) {
            Text title = skill.config().title();
            Text description = skill.config().description();
            String id = skill.config().id();
            if (title != null && !title.toString().isEmpty()) {
                builder.add(ExampleSkills.titleTranslationKey(id), title.getString());
            }
            if (description != null && !description.toString().isEmpty()) {
                builder.add(ExampleSkills.descriptionTranslationKey(id), description.getString());
            }
        }
        for (var spell : ExampleSpells.ENTRIES) {
            String langId = "spell."+ExampleMod.MOD_ID+"."+spell.id().getPath();
            builder.add(langId+".name", spell.title());
            builder.add(langId+".description", spell.description());
        }
        String[] classes = {"fishman", "human", "mink"};

        for (String className : classes) {
            String displayName = StringUtil.capitalize(className);
            String description = "Auto-generated description for " + displayName + " class";

            builder.add("class."+ExampleMod.MOD_ID+"." + className + ".name", displayName);
            builder.add("class."+ExampleMod.MOD_ID+"." + className + ".description", description);
        }


    }
}
