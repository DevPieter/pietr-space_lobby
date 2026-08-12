package nl.devpieter.features.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.*;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.events.WorldManagerInitializedEvent;
import nl.devpieter.features.world.enums.LoadedWorldType;
import nl.devpieter.features.world.models.LoadedWorld;
import nl.devpieter.features.world.models.World;
import nl.devpieter.features.world.models.settings.WorldSettings;
import nl.devpieter.features.world.models.settings.WorldTimeSetting;
import nl.devpieter.features.world.models.settings.WorldWeatherSetting;
import nl.devpieter.features.world.setting.WorldSettingsGsonAdapter;
import nl.devpieter.sees.Sees;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldManager {

    private static WorldManager INSTANCE;

    private final Logger logger = MinecraftServer.LOGGER;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(WorldSettings.class, new WorldSettingsGsonAdapter())
            .serializeNulls()
            .create();

    private final Sees sees = Sees.getSharedInstance();
    private final InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    private final List<World> worldsFromDisk = new ArrayList<>();
    private final List<World> dynamicWorlds = new ArrayList<>();

    private final HashMap<String, LoadedWorld> loadedWorlds = new HashMap<>();

    private WorldManager() {
    }

    public static WorldManager getInstance() {
        if (INSTANCE == null) INSTANCE = new WorldManager();
        return INSTANCE;
    }

    public void init() {
        if (!loadedWorlds.isEmpty()) {
            logger.info("Worlds are already loaded, unloading first...");
            unloadWorlds();
        }

        scanWorldFolders();
        loadWorldsFromDisk();

        sees.dispatch(new WorldManagerInitializedEvent());
    }

    public @Nullable LoadedWorld loadDynamicWorld(String id, String name, WorldSettings settings) {
        id = "__dynamic__" + id;

        if (loadedWorlds.containsKey(id)) {
            logger.warn("World with id {} is already loaded, skipping...", id);
            return null;
        }

        World world = new World(id, name, settings);
        dynamicWorlds.add(world);

        return loadWorld(world, LoadedWorldType.DYNAMIC);
    }

    private void loadWorldsFromDisk() {
        logger.info("Loading worlds from disk...");
        for (World world : worldsFromDisk) loadWorld(world, LoadedWorldType.DISK);
        logger.info("Finished loading worlds from disk. Loaded {} worlds.", loadedWorlds.size());
    }

    private LoadedWorld loadWorld(World world, LoadedWorldType type) {
        RegistryKey<DimensionType> dimType = DimensionType.OVERWORLD;

        logger.info("Loading world: {} ({})", world.name(), world.id());
        InstanceContainer instance = instanceManager.createInstanceContainer(dimType);

        WorldSettings settings = world.settings();
        settings.freeze();

        Clock clock = instance.defaultClock();
        if (clock != null) {
            WorldTimeSetting timeSetting = settings.time();

            clock.time(timeSetting.time());
            clock.rate(timeSetting.cycle() ? 1 : 0);
        }

        WorldWeatherSetting weatherSetting = settings.weather();
        if (weatherSetting.type().equalsIgnoreCase("rain")) instance.setWeather(Weather.RAIN);
        else if (weatherSetting.type().equalsIgnoreCase("thunder")) instance.setWeather(Weather.THUNDER);
        else instance.setWeather(Weather.CLEAR);

        Path worldPath = Path.of(EnvStatics.WORLD_DATA_PATH, world.id());
        instance.setChunkLoader(new AnvilLoader(worldPath, dimType.key()));

        LoadedWorld loadedWorld = new LoadedWorld(type, world, instance);
        loadedWorlds.put(world.id(), loadedWorld);

        return loadedWorld;
    }

    private void unloadWorlds() {
        logger.info("Unloading worlds...");

        for (LoadedWorld loadedWorld : loadedWorlds.values()) {
            logger.info("Unloading world: {} ({})", loadedWorld.world().name(), loadedWorld.world().id());
            Instance instance = loadedWorld.instance();

            instance.getPlayers().forEach(player -> player.kick("World is unloading"));
            instanceManager.unregisterInstance(instance);

            loadedWorld.world().settings().melt();
        }

        loadedWorlds.clear();
        logger.info("Worlds unloaded");
    }

    private @NotNull List<World> scanWorldFolders() {
        logger.info("Scanning world folders in: {}", EnvStatics.WORLD_DATA_PATH);

        File worldsDir = new File(EnvStatics.WORLD_DATA_PATH);
        List<World> foundWorlds = new ArrayList<>();

        if (!worldsDir.exists() || !worldsDir.isDirectory()) {
            logger.warn("Worlds directory does not exist or is not a directory: {}", EnvStatics.WORLD_DATA_PATH);
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

        this.worldsFromDisk.clear();
        this.worldsFromDisk.addAll(foundWorlds);

        logger.info("Finished scanning worlds. Found {} worlds.", foundWorlds.size());
        return foundWorlds;
    }

    public List<World> getFoundWorlds() {
        return new ArrayList<>(worldsFromDisk);
    }

    public List<LoadedWorld> getWorlds() {
        return new ArrayList<>(loadedWorlds.values());
    }

    public @Nullable World getWorldById(@Nullable String id) {
        if (id == null) return null;

        for (World world : worldsFromDisk) {
            if (!world.id().equals(id)) continue;
            return world;
        }

        return null;
    }

    public @Nullable LoadedWorld getLoadedWorld(World world) {
        if (world == null) return null;
        return loadedWorlds.get(world.id());
    }

    public @Nullable LoadedWorld getLoadedWorldById(@Nullable String id) {
        if (id == null) return null;
        return loadedWorlds.get(id);
    }

    public @Nullable LoadedWorld getLoadedWorldByInstance(@Nullable Instance instance) {
        if (instance == null) return null;

        for (LoadedWorld loadedWorld : loadedWorlds.values()) {
            if (!loadedWorld.instance().equals(instance)) continue;
            return loadedWorld;
        }

        return null;
    }
}
