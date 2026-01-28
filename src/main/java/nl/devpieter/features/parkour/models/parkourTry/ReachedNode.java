package nl.devpieter.features.parkour.models.parkourTry;

import org.jetbrains.annotations.Nullable;

public record ReachedNode(
        int nodeIndex,
        boolean skipped,
        @Nullable Long timestamp
) {
    public static ReachedNode create(int nodeIndex, boolean skipped) {
        return new ReachedNode(nodeIndex, skipped, skipped ? null : System.currentTimeMillis());
    }
}
