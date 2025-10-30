package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public record Parkour(
        String id,
        String worldId,

        String name,

        Pos startPos,
        BoundingBox startBox,

        Pos endPos,
        BoundingBox endBox,

        List<ParkourNode> nodes
) {
}
