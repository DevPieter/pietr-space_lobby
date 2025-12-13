package nl.devpieter.features.parkour.models.parkourTry;

import nl.devpieter.features.parkour.enums.ParkourFailReason;

public record FailedNode(
        int nodeIndex,
        ParkourFailReason failReason,
        long timestamp
) {
    public static FailedNode create(int nodeIndex, ParkourFailReason reason) {
        return new FailedNode(nodeIndex, reason, System.currentTimeMillis());
    }
}
