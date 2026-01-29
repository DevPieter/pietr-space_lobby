package nl.devpieter.features.parkour.requests;

import nl.devpieter.features.parkour.models.parkourTry.ParkourTry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateParkourTryRequest(
        String parkourId,
        UUID playerUuid,

        long startTime,
        long endTime,

        boolean isCancelled,
        int cancelReason,

        List<CreateReachedNodeRequest> reachedNodes,
        List<CreateSkippedNodeRequest> skippedNodes,
        List<CreateFailedNodeRequest> failedNodes
) {

    public static @NotNull CreateParkourTryRequest of(@NotNull ParkourTry parkourTry) {
        List<CreateReachedNodeRequest> reachedNodes = parkourTry.reachedNodes().stream()
                .map(CreateReachedNodeRequest::of)
                .toList();

        List<CreateSkippedNodeRequest> skippedNodes = parkourTry.skippedNodes().stream()
                .map(CreateSkippedNodeRequest::of)
                .toList();

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
                reachedNodes,
                skippedNodes,
                failedNodes
        );
    }
}
