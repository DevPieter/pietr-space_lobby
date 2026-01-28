package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.ReachedNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CreateReachedNodeRequest(
        int nodeIndex,
        boolean skipped,
        @Nullable Long timestamp
) {
    public static CreateReachedNodeRequest of(@NotNull ReachedNode node) {
        return new CreateReachedNodeRequest(
                node.nodeIndex(),
                node.skipped(),
                node.timestamp()
        );
    }
}
