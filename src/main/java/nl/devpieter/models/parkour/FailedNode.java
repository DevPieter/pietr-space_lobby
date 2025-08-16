package nl.devpieter.models.parkour;

import java.util.UUID;

public record FailedNode(
        UUID parkourNodeId,
        long timestamp
) {
}
