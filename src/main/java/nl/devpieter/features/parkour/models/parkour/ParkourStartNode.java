package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;

public record ParkourStartNode(
        Instance instance,
        Point point,
        BoundingBox boundingBox,
        SimpleYawPitch yawPitch
) {
}
