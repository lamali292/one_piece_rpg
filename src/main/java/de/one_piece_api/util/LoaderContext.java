package de.one_piece_api.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;

import java.util.ArrayList;
import java.util.List;

public record LoaderContext(MinecraftServer server, Identifier id,  List<String> warnings)  implements ConfigContext {


    public LoaderContext(MinecraftServer server, Identifier id) {
        this(server, id,  new ArrayList<>());
    }

    @Override
    public MinecraftServer getServer() {
        return this.server;
    }

    @Override
    public void emitWarning(String message) {
        this.warnings.add(message);
    }
}
