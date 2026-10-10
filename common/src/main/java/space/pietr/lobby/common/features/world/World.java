package space.pietr.lobby.common.features.world;

import space.pietr.lobby.common.features.world.settings.WorldSettings;

public record World(
        String id,
        String name,
        WorldSettings settings
) {
}
