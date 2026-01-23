package nl.devpieter.features.parkour.models.parkourTry;

public record ReachedNode(
        int nodeIndex,
        int reachedAtIndex, // TODO - Check if needed
        long timestamp,
        boolean skipped
) {
    public static ReachedNode create(int nodeIndex, int reachedAtIndex, boolean skipped) {
        return new ReachedNode(nodeIndex, reachedAtIndex, System.currentTimeMillis(), skipped);
    }
}
