package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.FailedNode;
import org.jetbrains.annotations.NotNull;

public record CreateFailedNodeRequest(
        int fromNodeIndex,
        int toNodeIndex,
        int failReason,
        long timestamp
) {
    public static CreateFailedNodeRequest of(@NotNull FailedNode node) {
        return new CreateFailedNodeRequest(
                node.fromNodeIndex(),
                node.toNodeIndex(),
                node.failReason().getValue(),
                node.timestamp()
        );
    }
}
