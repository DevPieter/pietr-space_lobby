package space.pietr.lobby.common.features.world;

import net.minestom.server.instance.InstanceContainer;
import space.pietr.lobby.common.features.world.enums.LoadedWorldType;
import space.pietr.lobby.common.features.world.settings.WorldSettings;
import space.pietr.lobby.common.features.world.settings.features.WorldFeatureSettings;

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
