package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.FailedNode;
import org.jetbrains.annotations.NotNull;

public record CreateFailedNodeRequest(
        int nodeIndex,
        int failReason,
        long timestamp
) {
    public static CreateFailedNodeRequest of(@NotNull FailedNode node) {
        return new CreateFailedNodeRequest(
                node.nodeIndex(),
                node.failReason().getValue(),
                node.timestamp()
        );
    }
}
