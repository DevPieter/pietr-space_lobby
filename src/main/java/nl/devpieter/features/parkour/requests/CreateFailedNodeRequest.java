package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.FailedNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public record CreateFailedNodeRequest(
        int fromNodeIndex,
        int toNodeIndex,
        int failReason,
        long timestamp
) {
    @Contract("_ -> new")
    public static @NonNull CreateFailedNodeRequest of(@NotNull FailedNode node) {
        return new CreateFailedNodeRequest(
                node.fromNodeIndex(),
                node.toNodeIndex(),
                node.failReason().getValue(),
                node.timestamp()
        );
    }
}
