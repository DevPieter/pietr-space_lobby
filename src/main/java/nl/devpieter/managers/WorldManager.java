package nl.devpieter.managers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import nl.devpieter.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.UUID;

public class WorldManager {

    private static WorldManager INSTANCE;

    private final Logger logger = MinecraftServer.LOGGER;
    private final InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    private final HashMap<UUID, InstanceContainer> worlds = new HashMap<>();

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

    public @Nullable InstanceContainer getWorld(@Nullable UUID uuid) {
        if (uuid == null) return null;
        return this.worlds.get(uuid);
    }

    public @Nullable UUID getWorldUUID(@Nullable Instance world) {
        if (world == null) return null;

        for (UUID uuid : this.worlds.keySet()) {
            if (this.worlds.get(uuid).equals(world)) {
                return uuid;
            }
        }

        return null;
    }

    private void loadMainWorld() {
        this.mainWorld = this.instanceManager.createInstanceContainer();

        this.mainWorld.setTime(Settings.MAIN_WORLD_TIME);
        this.mainWorld.setTimeRate(Settings.MAIN_WORLD_TIME_RATE);
        this.mainWorld.setChunkLoader(new AnvilLoader(Settings.WORLD_PATH_BASE + "/main"));

        this.logger.info("Main world loaded");
        this.worlds.put(UUID.fromString("52ccbf8b-7528-41c2-9f4c-9bc2b5e10951"), this.mainWorld);
    }

    private void loadSpecialWorld() {
        this.specialWorld = this.instanceManager.createInstanceContainer();

        this.specialWorld.setTime(Settings.SPECIAL_WORLD_TIME);
        this.specialWorld.setTimeRate(Settings.SPECIAL_WORLD_TIME_RATE);
        this.specialWorld.setChunkLoader(new AnvilLoader(Settings.WORLD_PATH_BASE + "/special"));

        this.logger.info("Special world loaded");
        this.worlds.put(UUID.fromString("aba23b3b-0cae-42dd-92a0-7aaf63f4fe54"), this.specialWorld);
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
