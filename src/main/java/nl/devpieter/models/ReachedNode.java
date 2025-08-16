package nl.devpieter.models;

import java.util.UUID;

public record ReachedNode(
        UUID nodeId,
        int reachedAtIndex,
        long timestamp,
        boolean skipped
) {
}
