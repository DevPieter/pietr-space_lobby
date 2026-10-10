package nl.devpieter.features.parkour.models.parkour;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import space.pietr.lobby.common.models.simple.SimpleYawPitch;

public record ParkourNode(
        int index,
        boolean isCheckpoint,

        Instance instance,
        Point point,
        SimpleYawPitch yawPitch,
        BoundingBox boundingBox
) {

    public Pos pos() {
        return point.asPos()
                .add(0.5, 0, 0.5)
                .withYaw(yawPitch.yaw())
                .withPitch(yawPitch.pitch());
    }
}
