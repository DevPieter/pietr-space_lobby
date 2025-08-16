package nl.devpieter.models;

import java.util.UUID;

public record FailedNode(
        UUID nodeId,
        long timestamp
) {
}
