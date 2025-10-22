package nl.devpieter.features.world;

import com.google.gson.Gson;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import nl.devpieter.Settings;
import nl.devpieter.events.WorldManagerInitializedEvent;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.features.world.models.World;
import nl.devpieter.sees.Sees;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldManager {

    private static WorldManager INSTANCE;

    private final Logger logger = MinecraftServer.LOGGER;
    private final Gson gson = new Gson();

    private final Sees sees = Sees.getInstance();
    private final InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    private final List<World> foundWorlds = new ArrayList<>();
    private final HashMap<String, LoadedWorld> loadedWorlds = new HashMap<>();

    private WorldManager() {
    }

    public static WorldManager getInstance() {
        if (INSTANCE == null) INSTANCE = new WorldManager();
        return INSTANCE;
    }

    public void init() {
        scanWorldFolders();
        loadWorlds();

        sees.dispatch(new WorldManagerInitializedEvent());
    }

    private void loadWorlds() {
        if (!loadedWorlds.isEmpty()) {
            logger.info("Worlds are already loaded, unloading first...");
            unloadWorlds();
        }

        logger.info("Loading worlds...");

        for (World world : foundWorlds) {
            logger.info("Loading world: {} ({})", world.name(), world.id());
            InstanceContainer instance = instanceManager.createInstanceContainer();

            instance.setTime(world.settings().time().time());
            instance.setTimeRate(world.settings().time().cycle() ? 1 : 0);
            instance.setChunkLoader(new AnvilLoader(Settings.WORLD_PATH_BASE + "/" + world.id()));

            loadedWorlds.put(world.id(), new LoadedWorld(world, instance));
        }

        logger.info("Worlds loaded");
    }

    private void unloadWorlds() {
        logger.info("Unloading worlds...");

        for (LoadedWorld loadedWorld : loadedWorlds.values()) {
            logger.info("Unloading world: {} ({})", loadedWorld.world().name(), loadedWorld.world().id());
            instanceManager.unregisterInstance(loadedWorld.instance());
        }

        loadedWorlds.clear();
        logger.info("Worlds unloaded");
    }

    private @NotNull List<World> scanWorldFolders() {
        logger.info("Scanning world folders in: {}", Settings.WORLD_PATH_BASE);

        File worldsDir = new File(Settings.WORLD_PATH_BASE);
        List<World> foundWorlds = new ArrayList<>();

        if (!worldsDir.exists() || !worldsDir.isDirectory()) {
            logger.warn("Worlds directory does not exist or is not a directory: {}", Settings.WORLD_PATH_BASE);
            return foundWorlds;
        }

        File[] files = worldsDir.listFiles();
        if (files == null) return foundWorlds;

        for (File file : files) {
            if (!file.isDirectory()) continue;
            File worldJson = new File(file, "world.json");
            if (!worldJson.exists()) {
                logger.warn("world.json not found in folder: {}", file.getName());
                continue;
            }

            try (FileReader reader = new FileReader(worldJson)) {
                World world = gson.fromJson(reader, World.class);
                foundWorlds.add(world);
                logger.info("Found world: {} ({})", world.name(), world.id());
            } catch (Exception e) {
                logger.error("Failed to read world.json in folder: {}", file.getName(), e);
            }
        }

        this.foundWorlds.clear();
        this.foundWorlds.addAll(foundWorlds);

        logger.info("Finished scanning worlds. Found {} worlds.", foundWorlds.size());
        return foundWorlds;
    }

    public List<World> getFoundWorlds() {
        return new ArrayList<>(foundWorlds);
    }

    public List<LoadedWorld> getWorlds() {
        return new ArrayList<>(loadedWorlds.values());
    }

    public @Nullable LoadedWorld getWorldById(@Nullable String id) {
        if (id == null) return null;
        return loadedWorlds.get(id);
    }

    public @Nullable LoadedWorld getWorldByInstance(@Nullable Instance instance) {
        if (instance == null) return null;

        for (LoadedWorld loadedWorld : loadedWorlds.values()) {
            if (!loadedWorld.instance().equals(instance)) continue;
            return loadedWorld;
        }

        return null;
    }
}
