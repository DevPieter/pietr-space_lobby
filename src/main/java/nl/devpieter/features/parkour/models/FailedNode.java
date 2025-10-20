package nl.devpieter.features.parkour.models;

import nl.devpieter.features.parkour.enums.ParkourFailReason;

import java.util.UUID;

public record FailedNode(
        UUID parkourNodeId,
        ParkourFailReason failReason,
        long timestamp
) {
    public static FailedNode create(UUID parkourNodeId, ParkourFailReason reason) {
        return new FailedNode(parkourNodeId, reason, System.currentTimeMillis());
    }
}
