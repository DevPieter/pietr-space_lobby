package nl.devpieter.models.requests;

import nl.devpieter.models.parkour.FailedNode;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record CreateFailedNodeRequest(
        UUID parkourNodeId,
        int failReason,
        long timestamp
) {
    public static CreateFailedNodeRequest of(@NotNull FailedNode failedNode) {
        return new CreateFailedNodeRequest(
                failedNode.parkourNodeId(),
                failedNode.failReason().getValue(),
                failedNode.timestamp()
        );
    }
}
