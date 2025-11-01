package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import nl.devpieter.models.YawPitch;

public record ParkourEndNode(
        Point point,
        BoundingBox boundingBox,
        YawPitch yawPitch
) {
}
