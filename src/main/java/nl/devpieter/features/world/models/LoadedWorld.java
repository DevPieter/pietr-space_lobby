package nl.devpieter.features.world.models;

import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.features.world.enums.LoadedWorldType;
import nl.devpieter.features.world.models.settings.WorldSettings;
import nl.devpieter.features.world.models.settings.features.WorldFeatureSettings;

public record LoadedWorld(
        LoadedWorldType type,
        World world,
        InstanceContainer instance
) {

    public String id() {
        return world.id();
    }

    public String name() {
        return world.name();
    }

    public WorldSettings settings() {
        return world.settings();
    }

    public WorldFeatureSettings features() {
        return world.settings().features();
    }
}
