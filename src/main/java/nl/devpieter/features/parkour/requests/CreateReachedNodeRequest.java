package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.ReachedNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CreateReachedNodeRequest(
        int fromNodeIndex,
        int toNodeIndex,
        @Nullable Long timestamp
) {
    public static CreateReachedNodeRequest of(@NotNull ReachedNode node) {
        return new CreateReachedNodeRequest(
                node.fromNodeIndex(),
                node.toNodeIndex(),
                node.timestamp()
        );
    }
}
