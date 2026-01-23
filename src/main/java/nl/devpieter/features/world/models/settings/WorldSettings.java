package nl.devpieter.features.world.models.settings;

import nl.devpieter.models.SimplePoint;

public record WorldSettings(
        SimplePoint spawn,
        WorldMarkerSignsSetting markerSigns,
        WorldBoundsSetting bounds,
        WorldTimeSetting time,
        WorldWeatherSetting weather
) {
}