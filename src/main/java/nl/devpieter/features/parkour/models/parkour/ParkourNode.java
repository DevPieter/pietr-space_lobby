package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import nl.devpieter.models.YawPitch;

public record ParkourNode(
        int index,
        boolean isCheckpoint,

        Point point,
        YawPitch yawPitch,
        BoundingBox boundingBox
) {

    public Pos pos() {
        return point.asPos()
                .add(0.5, 0, 0.5)
                .withYaw(yawPitch.yaw())
                .withPitch(yawPitch.pitch());
    }
}
