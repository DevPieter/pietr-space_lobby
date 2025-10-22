package nl.devpieter.features.portals.models;

import net.minestom.server.coordinate.Pos;

import java.util.UUID;

public record Portal(
        String destinationWorld,
        Pos destinationPos,
        Pos start,
        Pos end
) {
}
