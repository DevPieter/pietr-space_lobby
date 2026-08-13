package nl.devpieter.models;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import nl.devpieter.features.world.setting.IClonable;

public record SimplePoint(
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) implements IClonable<SimplePoint> {

    public static SimplePoint zero() {
        return new SimplePoint(0, 0, 0, 0, 0);
    }

    public Point toPoint() {
        return new Pos(x, y, z, yaw, pitch);
    }

    public SimpleVec toVec() {
        return new SimpleVec(x, y, z);
    }

    @Override
    public SimplePoint cloneObj() {
        return new SimplePoint(x, y, z, yaw, pitch);
    }
}
