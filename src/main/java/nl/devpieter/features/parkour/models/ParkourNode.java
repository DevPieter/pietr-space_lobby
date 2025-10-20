package nl.devpieter.features.parkour.models;

import net.minestom.server.coordinate.Pos;

import java.util.UUID;

public record ParkourNode(
        UUID id,

        int index,
        Pos position,

        boolean isCheckpoint
) {
}
