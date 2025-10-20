package nl.devpieter.features.parkour.models;

import java.util.UUID;

public record ReachedNode(
        UUID parkourNodeId,
        int reachedAtIndex,
        long timestamp,
        boolean skipped
) {
}
