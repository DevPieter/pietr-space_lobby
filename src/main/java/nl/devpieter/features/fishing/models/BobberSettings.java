package nl.devpieter.features.fishing.models;

public record BobberSettings(
        int minBiteTime,
        int maxBiteTime,
        int minLureTime,
        int maxLureTime,
        int minIdleTime,
        int maxIdleTime
) {
}
