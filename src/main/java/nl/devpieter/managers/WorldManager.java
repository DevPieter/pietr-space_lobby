package nl.devpieter.managers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import nl.devpieter.Settings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class WorldManager {

    private static WorldManager INSTANCE;

    private final Logger logger = MinecraftServer.LOGGER;
    private final InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    private InstanceContainer mainWorld;
    private InstanceContainer specialWorld;

    private WorldManager() {
    }

    public static WorldManager getInstance() {
        if (INSTANCE == null) INSTANCE = new WorldManager();
        return INSTANCE;
    }

    public void loadWorlds() {
        this.loadMainWorld();
        this.loadSpecialWorld();

        this.logger.info("Worlds loaded");
    }

    private void loadMainWorld() {
        this.mainWorld = this.instanceManager.createInstanceContainer();

        this.mainWorld.setTime(Settings.MAIN_WORLD_TIME);
        this.mainWorld.setTimeRate(Settings.MAIN_WORLD_TIME_RATE);
        this.mainWorld.setChunkLoader(new AnvilLoader(Settings.WORLD_PATH_BASE + "/main"));

        this.logger.info("Main world loaded");
    }

    private void loadSpecialWorld() {
        this.specialWorld = this.instanceManager.createInstanceContainer();

        this.specialWorld.setTime(Settings.SPECIAL_WORLD_TIME);
        this.specialWorld.setTimeRate(Settings.SPECIAL_WORLD_TIME_RATE);
        this.specialWorld.setChunkLoader(new AnvilLoader(Settings.WORLD_PATH_BASE + "/special"));

        this.logger.info("Special world loaded");
    }

    public @NotNull InstanceContainer getMainWorld() {
        if (this.mainWorld == null) throw new IllegalStateException("Main world is not loaded");
        return this.mainWorld;
    }

    public @NotNull InstanceContainer getSpecialWorld() {
        if (this.specialWorld == null) throw new IllegalStateException("Special world is not loaded");
        return this.specialWorld;
    }
}
