package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.ReachedNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record CreateReachedNodeRequest(
        int fromNodeIndex,
        int toNodeIndex,
        long timestamp
) {
    @Contract("_ -> new")
    public static @NonNull CreateReachedNodeRequest of(@NotNull ReachedNode node) {
        return new CreateReachedNodeRequest(
                node.fromNodeIndex(),
                node.toNodeIndex(),
                node.timestamp()
        );
    }
}
