package nl.devpieter.features.world.models;

import nl.devpieter.features.world.models.settings.WorldSettings;

public record World(
        String id,
        String name,
        WorldSettings settings
) {
}
