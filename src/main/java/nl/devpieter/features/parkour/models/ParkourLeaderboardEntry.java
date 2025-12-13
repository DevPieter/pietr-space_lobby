package nl.devpieter.features.parkour.models;

import java.util.UUID;

public record ParkourLeaderboardEntry(
        String parkourId,
        UUID playerUuid,
        String playerName,
        long completionTime
) {
}
