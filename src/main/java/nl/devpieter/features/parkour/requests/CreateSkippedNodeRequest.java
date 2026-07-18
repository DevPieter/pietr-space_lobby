package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.SkippedNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public record CreateSkippedNodeRequest(
        int nodeIndex,
        int fromNodeIndex,
        int toNodeIndex,
        long timestamp
) {
    @Contract("_ -> new")
    public static @NonNull CreateSkippedNodeRequest of(@NotNull SkippedNode node) {
        return new CreateSkippedNodeRequest(
                node.nodeIndex(),
                node.fromNodeIndex(),
                node.toNodeIndex(),
                node.timestamp()
        );
    }
}
