package nl.devpieter.plugin;

import nl.devpieter.Lobby;
import nl.devpieter.config.EnvStatics;
import org.slf4j.Logger;
import space.pietr.lobby.api.ILobbyPluginInitializer;
import space.pietr.lobby.api.LobbyApi;
import space.pietr.lobby.api.LobbyPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PluginManager {

    private static final PluginManager INSTANCE = new PluginManager();

    private final Logger logger = Lobby.getLogger(this.getClass());
    private final Map<PluginClassLoader, Object> loadedPlugins = new LinkedHashMap<>();

    private PluginManager() {
    }

    public static PluginManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        scanPluginsFolder();
    }

    public void apiAvailable(LobbyApi api) {
        loadedPlugins.values().forEach(plugin -> {
            if (!(plugin instanceof ILobbyPluginInitializer initializer)) return;
            initializer.onApiAvailable(api);
        });
    }

    private void scanPluginsFolder() {
        logger.info("Scanning plugin folders in: {}", EnvStatics.PLUGIN_DATA_PATH);

        var pluginsDir = new File(EnvStatics.PLUGIN_DATA_PATH);
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            logger.warn("Plugin folder does not exist or is not a directory: {}", EnvStatics.PLUGIN_DATA_PATH);
            return;
        }

        var pluginFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (pluginFiles == null || pluginFiles.length == 0) {
            logger.info("No plugin files found in: {}", EnvStatics.PLUGIN_DATA_PATH);
            return;
        }

        for (var pluginFile : pluginFiles) {
            logger.info("Found plugin file: {}", pluginFile.getName());
            loadPlugin(pluginFile);
        }
    }

    private void loadPlugin(File pluginFile) {
        PluginClassLoader classLoader = null;

        try {
            classLoader = new PluginClassLoader(pluginFile);
            var pluginClass = classLoader.loadPluginClass();

            if (pluginClass == null) {
                logger.warn("No class with @LobbyPlugin annotation found in: {}", pluginFile.getName());
                classLoader.close();
                return;
            }

            var pluginAnnotation = pluginClass.getAnnotation(LobbyPlugin.class);
            logger.info("Loading plugin: {} (ID: {}, Version: {})", pluginAnnotation.name(), pluginAnnotation.id(), pluginAnnotation.version());

            // Keep the loader open for classes and resources used after initialization.
            var plugin = pluginClass.getDeclaredConstructor().newInstance();
            loadedPlugins.put(classLoader, plugin);
        } catch (Exception | LinkageError e) {

            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException closeException) {
                    e.addSuppressed(closeException);
                }
            }

            logger.error("Failed to load plugin from file: {}", pluginFile.getName(), e);
        }
    }
}
