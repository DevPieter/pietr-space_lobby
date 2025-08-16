package nl.devpieter.models.parkour;

import java.util.UUID;

public record ReachedNode(
        UUID parkourNodeId,
        int reachedAtIndex,
        long timestamp,
        boolean skipped
) {
}
