package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.ReachedNode;
import org.jetbrains.annotations.NotNull;

public record CreateReachedNodeRequest(
        int nodeIndex,
        long timestamp,
        boolean skipped
) {
    public static CreateReachedNodeRequest of(@NotNull ReachedNode node) {
        return new CreateReachedNodeRequest(
                node.nodeIndex(),
                node.timestamp(),
                node.skipped()
        );
    }
}
