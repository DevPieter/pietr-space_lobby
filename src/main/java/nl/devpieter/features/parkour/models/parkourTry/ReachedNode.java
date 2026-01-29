package nl.devpieter.features.parkour.models.parkourTry;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record ReachedNode(
        int fromNodeIndex,
        int toNodeIndex,
        long timestamp
) {
    @Contract("_, _ -> new")
    public static @NonNull ReachedNode create(int fromNodeIndex, int toNodeIndex) {
        return new ReachedNode(fromNodeIndex, toNodeIndex, System.currentTimeMillis());
    }
}
