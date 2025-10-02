package de.one_piece_content_data.datagen;

import de.one_piece_content.ExampleMod;
import de.one_piece_api.config.SkillDefinitionConfig;
import de.one_piece_api.config.SpellConfig;
import de.one_piece_content_data.registry.Registries;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class LangGen extends FabricLanguageProvider {
    public LangGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }



    public static String titleTranslationKey(Identifier id) {
        return "skill." + id.getPath() + "." + id.getNamespace() + ".title";
    }

    public static String descriptionTranslationKey(Identifier id) {
        return "skill." + id.getPath() + "." + id.getNamespace() + ".description";
    }


    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder builder) {
        for (var entry :  Registries.SKILL_DEFINITION.entries().entrySet()) {
            Identifier id = entry.getKey();
            SkillDefinitionConfig skill = entry.getValue();

            Text title = skill.title();
            Text description = skill.description();
            if (title != null && !title.toString().isEmpty()) {
                builder.add(titleTranslationKey(id), title.getString());
            }
            if (description != null && !description.toString().isEmpty()) {
                builder.add(descriptionTranslationKey(id), description.getString());
            }
        }
        for (var spell :  Registries.SPELLS.entries().entrySet()) {
            Identifier id = spell.getKey();
            SpellConfig spellEntry = spell.getValue();
            String langId = "spell."+ExampleMod.MOD_ID+"."+id.getPath();
            builder.add(langId+".name", spellEntry.title());
            builder.add(langId+".description", spellEntry.description());
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
