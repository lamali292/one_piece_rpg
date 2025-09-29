package de.one_piece_content;

import de.one_piece_content.registries.MySounds;
import de.one_piece_content.spells.SpellHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements DedicatedServerModInitializer {
	public static final String MOD_ID = "one_piece_content";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier CATEGORY_ID = Identifier.of("one_piece_api", "one_piece_api");

	@Override
	public void onInitializeServer() {
		LOGGER.info("Example Content initializing");
		//MySounds.register();
		SpellHandler.register();
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}


}