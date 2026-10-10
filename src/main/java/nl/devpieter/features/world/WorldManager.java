package nl.devpieter.features.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.*;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import nl.devpieter.Lobby;
import nl.devpieter.config.EnvStatics;
import nl.devpieter.features.world.gsonAdapters.WorldFeatureSettingsGsonAdapter;
import nl.devpieter.features.world.gsonAdapters.WorldSettingsGsonAdapter;
import nl.devpieter.sees.Sees;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import space.pietr.lobby.api.features.world.IWorldManager;
import space.pietr.lobby.api.features.world.events.WorldManagerInitializedEvent;
import space.pietr.lobby.common.features.world.LoadedWorld;
import space.pietr.lobby.common.features.world.World;
import space.pietr.lobby.common.features.world.enums.LoadedWorldType;
import space.pietr.lobby.common.features.world.settings.WorldSettings;
import space.pietr.lobby.common.features.world.settings.WorldTimeSetting;
import space.pietr.lobby.common.features.world.settings.WorldWeatherSetting;
import space.pietr.lobby.common.features.world.settings.features.WorldFeatureSettings;
import space.pietr.lobby.common.models.tuple.Pair;
import space.pietr.lobby.common.utils.minestom.PlayerUtils;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class WorldManager implements IWorldManager {

    private static final WorldManager INSTANCE = new WorldManager();

    private final Logger logger = Lobby.getLogger(this.getClass());
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(WorldSettings.class, new WorldSettingsGsonAdapter())
            .registerTypeAdapter(WorldFeatureSettings.class, new WorldFeatureSettingsGsonAdapter())
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private final Sees sees = Sees.getSharedInstance();
    private final InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    private final List<World> worldsFromDisk = new ArrayList<>();
    private final List<World> dynamicWorlds = new ArrayList<>();

    private final HashMap<String, LoadedWorld> loadedWorlds = new HashMap<>();

    private WorldManager() {
    }

    public static WorldManager getInstance() {
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

    public boolean createWorldFolder(String id, String name, WorldSettings settings) {
        Path worldPath = Path.of(EnvStatics.WORLD_DATA_PATH, id);
        File worldDir = worldPath.toFile();

        if (worldDir.exists()) {
            logger.warn("World folder already exists: {}", worldPath);
            return false;
        }

        if (!worldDir.mkdirs()) {
            logger.error("Failed to create world folder: {}", worldPath);
            return false;
        }

        World world = new World(id, name, settings);

        Path regionPath = Path.of(worldPath.toString(), "dimensions", "minecraft", "overworld", "region");
        File regionDir = regionPath.toFile();
        if (!regionDir.mkdirs()) {
            logger.error("Failed to create region folder: {}", regionPath);
            return false;
        }

        try {
            File worldJson = new File(worldDir, "world.json");
            if (!worldJson.createNewFile()) {
                logger.error("Failed to create world.json file in folder: {}", worldPath);
                return false;
            }

            String json = gson.toJson(world);
            Files.writeString(worldJson.toPath(), json);

            logger.info("Created new world folder: {} ({})", name, id);
            worldsFromDisk.add(world);
            return true;
        } catch (Exception e) {
            logger.error("Failed to create world.json file in folder: {}", worldPath, e);
            return false;
        }
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

    public boolean reloadLoadedWorldById(String id) {
        LoadedWorld loadedWorld = loadedWorlds.get(id);
        if (loadedWorld == null) return false;

        unloadWorld(loadedWorld);

        Pair<World, LoadedWorldType> worldTypePair = getWorldAndTypeById(id);
        if (worldTypePair == null) return false;

        loadWorld(worldTypePair.first(), worldTypePair.second());
        return true;
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
        instance.setChunkSupplier(LightingChunk::new);

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

        if (type == LoadedWorldType.DISK) {
            Path worldPath = Path.of(EnvStatics.WORLD_DATA_PATH, world.id());
            instance.setChunkLoader(new AnvilLoader(worldPath, dimType.key()));
        }

        LoadedWorld loadedWorld = new LoadedWorld(type, world, instance);
        loadedWorlds.put(world.id(), loadedWorld);

        return loadedWorld;
    }

    private void unloadWorlds() {
        logger.info("Unloading all worlds...");

        for (LoadedWorld loadedWorld : loadedWorlds.values()) unloadWorld(loadedWorld);

        if (!loadedWorlds.isEmpty()) logger.warn("Some worlds were not unloaded properly: {}", loadedWorlds.keySet());
        else logger.info("Finished unloading world.");
    }

    private void unloadWorld(LoadedWorld loadedWorld) {
        if (loadedWorld == null) return;

        logger.info("Unloading world: {} ({})", loadedWorld.world().name(), loadedWorld.world().id());
        Instance instance = loadedWorld.instance();

        // TODO - Get from config
        boolean isMainWorld = loadedWorld.world().id().equals("main");
        if (isMainWorld) logger.warn("Unloading the main world! Players will be kicked.");

        instance.getPlayers().forEach(player -> {
            if (isMainWorld) player.kick("Main world is unloading, please reconnect in a few seconds.");
            else player.sendMessage("World is unloading, moving you to the main world.");

            LoadedWorld mainWorld = getMainWorld();
            if (mainWorld != null) PlayerUtils.moveWorld(player, mainWorld);
        });
        instanceManager.unregisterInstance(instance);

        loadedWorld.world().settings().melt();
        loadedWorlds.remove(loadedWorld.world().id());
    }

    private void scanWorldFolders() {
        logger.info("Scanning world folders in: {}", EnvStatics.WORLD_DATA_PATH);

        File worldsDir = new File(EnvStatics.WORLD_DATA_PATH);
        List<World> foundWorlds = new ArrayList<>();

        if (!worldsDir.exists() || !worldsDir.isDirectory()) {
            logger.error("Worlds directory does not exist or is not a directory: {}", EnvStatics.WORLD_DATA_PATH);
            return;
        }

        File[] files = worldsDir.listFiles();
        if (files == null) {
            logger.warn("No world folders found in: {}", EnvStatics.WORLD_DATA_PATH);
            return;
        }

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

        worldsFromDisk.clear();
        worldsFromDisk.addAll(foundWorlds);

        logger.info("Finished scanning worlds. Found {} worlds.", foundWorlds.size());
    }

    @Deprecated(since = "Use getFoundWorldsFromDisk() instead")
    public List<World> getFoundWorlds() {
        return getFoundWorldsFromDisk();
    }

    public List<World> getFoundWorldsFromDisk() {
        return new ArrayList<>(worldsFromDisk);
    }

    @Deprecated(since = "Use getLoadedWorlds() instead")
    public List<LoadedWorld> getWorlds() {
        return getLoadedWorlds();
    }

    public List<LoadedWorld> getLoadedWorlds() {
        return new ArrayList<>(loadedWorlds.values());
    }

    public List<String> getLoadedWorldIds() {
        return new ArrayList<>(loadedWorlds.keySet());
    }

    public @Nullable World getWorldById(@Nullable String id) {
        if (id == null) return null;

        List<World> allWorlds = new ArrayList<>();
        allWorlds.addAll(worldsFromDisk);
        allWorlds.addAll(dynamicWorlds);

        for (World world : allWorlds) {
            if (!world.id().equals(id)) continue;
            return world;
        }

        return null;
    }

    public @Nullable Pair<World, LoadedWorldType> getWorldAndTypeById(@Nullable String id) {
        if (id == null) return null;

        for (World world : worldsFromDisk) {
            if (!world.id().equals(id)) continue;
            return new Pair<>(world, LoadedWorldType.DISK);
        }

        for (World world : dynamicWorlds) {
            if (!world.id().equals(id)) continue;
            return new Pair<>(world, LoadedWorldType.DYNAMIC);
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

    public @Nullable LoadedWorld getMainWorld() {
        // TODO - Get from config
        return loadedWorlds.get("main");
    }
}
