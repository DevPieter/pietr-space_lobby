package nl.devpieter.models.fishing;

public record BobberSettings(
        int minBiteTime,
        int maxBiteTime,
        int minLureTime,
        int maxLureTime,
        int minIdleTime,
        int maxIdleTime
) {
}
