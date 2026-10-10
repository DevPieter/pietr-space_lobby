package nl.devpieter.plugin;

import space.pietr.lobby.api.LobbyPlugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

public class PluginClassLoader extends URLClassLoader {

    private final File pluginFile;

    public PluginClassLoader(File pluginFile) throws MalformedURLException {
        super(new URL[]{pluginFile.toURI().toURL()}, PluginClassLoader.class.getClassLoader());
        this.pluginFile = pluginFile;
    }

    public Class<?> loadPluginClass() throws Exception {
        try (var jarFile = new JarFile(pluginFile)) {
            var entries = jarFile.entries();

            while (entries.hasMoreElements()) {

                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")
                        || entry.getName().startsWith("META-INF/")
                        || entry.getName().equals("module-info.class")
                ) continue;

                var className = entry.getName().substring(0, entry.getName().length() - ".class".length()).replace('/', '.');

                var clazz = loadClass(className);
                if (!clazz.isAnnotationPresent(LobbyPlugin.class)) continue;

                return clazz;
            }
        }

        return null;
    }
}
