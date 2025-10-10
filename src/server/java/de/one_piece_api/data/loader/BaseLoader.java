package de.one_piece_api.data.loader;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.impl.config.ConfigContextImpl;

import java.util.function.BiFunction;

public abstract class BaseLoader<T> implements SimpleSynchronousResourceReloadListener {
    protected final BiFunction<JsonElement, ConfigContext, Result<T, Problem>> parser;
    protected MinecraftServer server;

    public BaseLoader(BiFunction<JsonElement, ConfigContext, Result<T, Problem>> parser) {
        this.parser = parser;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    protected ConfigContext createContext() {
        return new ConfigContextImpl(server);
    }
}