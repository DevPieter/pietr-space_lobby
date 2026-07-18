package nl.devpieter.features.parkour.models.parkourTry;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record SkippedNode(
        int nodeIndex,
        int fromNodeIndex,
        int toNodeIndex,
        long timestamp
) {
    @Contract("_, _, _ -> new")
    public static @NonNull SkippedNode create(int nodeIndex, int fromNodeIndex, int toNodeIndex) {
        return new SkippedNode(nodeIndex, fromNodeIndex, toNodeIndex, System.currentTimeMillis());
    }
}
