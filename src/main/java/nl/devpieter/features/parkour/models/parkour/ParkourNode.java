package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;

public record ParkourNode(
        int index,

        Pos pos,
        BoundingBox boundingBox,

        boolean isCheckpoint
) {
}
