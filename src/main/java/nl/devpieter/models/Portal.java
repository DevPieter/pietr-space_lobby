package nl.devpieter.models;

import net.minestom.server.coordinate.Pos;

import java.util.UUID;

public record Portal(
        UUID destinationWorld,
        Pos destinationPos,
        Pos start,
        Pos end
) {
}
