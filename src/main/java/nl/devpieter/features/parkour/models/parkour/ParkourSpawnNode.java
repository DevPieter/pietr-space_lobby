package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import nl.devpieter.models.YawPitch;

public record ParkourSpawnNode(
        Instance instance,
        Point point,
        YawPitch yawPitch
) {
}
