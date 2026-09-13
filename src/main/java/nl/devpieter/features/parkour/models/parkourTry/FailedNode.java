package nl.devpieter.features.parkour.models.parkourTry;

import nl.devpieter.features.parkour.enums.ParkourFailReason;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record FailedNode(
        int fromNodeIndex,
        int toNodeIndex,
        ParkourFailReason failReason,
        long timestamp
) {
    @Contract("_, _, _ -> new")
    public static @NonNull FailedNode create(int fromNodeIndex, int toNodeIndex, ParkourFailReason reason) {
        return new FailedNode(fromNodeIndex, toNodeIndex, reason, System.currentTimeMillis());
    }
}
