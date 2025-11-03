package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import nl.devpieter.models.YawPitch;

public record ParkourStartNode(
        Instance instance,
        Point point,
        BoundingBox boundingBox,
        YawPitch yawPitch
) {
}
