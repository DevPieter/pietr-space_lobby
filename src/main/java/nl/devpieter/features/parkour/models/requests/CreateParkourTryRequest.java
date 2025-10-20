package nl.devpieter.features.parkour.models.requests;

import nl.devpieter.features.parkour.models.ParkourTry;
import nl.devpieter.features.parkour.models.ReachedNode;
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
        List<CreateFailedNodeRequest> failedNodes
) {

    public static @NotNull CreateParkourTryRequest of(@NotNull ParkourTry parkourTry) {
        List<CreateFailedNodeRequest> failedNodes = parkourTry.failedNodes().stream()
                .map(CreateFailedNodeRequest::of)
                .toList();

        return new CreateParkourTryRequest(
                parkourTry.parkourId(),
                parkourTry.playerUuid(),
                parkourTry.startTime(),
                parkourTry.endTime(),
                parkourTry.isCancelled(),
                parkourTry.cancelReason().getValue(),
                parkourTry.reachedNodes(),
                failedNodes
        );
    }
}
