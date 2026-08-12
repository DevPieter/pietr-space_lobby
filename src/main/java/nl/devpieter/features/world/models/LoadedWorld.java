package nl.devpieter.features.world.models;

import net.minestom.server.instance.InstanceContainer;
import nl.devpieter.features.world.enums.LoadedWorldType;

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
}
