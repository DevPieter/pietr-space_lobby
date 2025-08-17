package nl.devpieter.models.requests;

import nl.devpieter.models.parkour.FailedNode;
import nl.devpieter.models.parkour.ParkourTry;
import nl.devpieter.models.parkour.ReachedNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateParkourTryRequest(
        UUID parkourId,
        UUID playerUuid,

        long startTime,
        long endTime,

        boolean isCancelled,
        int cancelReason,

        List<ReachedNode> reachedNodes,
        List<FailedNode> failedNodes
) {

    public static @NotNull CreateParkourTryRequest of(@NotNull ParkourTry parkourTry) {
        return new CreateParkourTryRequest(
                parkourTry.parkourId(),
                parkourTry.playerUuid(),
                parkourTry.startTime(),
                parkourTry.endTime(),
                parkourTry.isCancelled(),
                parkourTry.cancelReason().getValue(),
                parkourTry.reachedNodes(),
                parkourTry.failedNodes()
        );
    }
}
