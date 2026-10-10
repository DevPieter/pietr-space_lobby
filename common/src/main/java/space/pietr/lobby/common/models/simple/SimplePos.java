package space.pietr.lobby.common.models.simple;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import space.pietr.lobby.common.types.IClonable;

public record SimplePos(
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) implements IClonable<SimplePos> {

    public static final SimplePos ZERO = new SimplePos(0, 0, 0, 0, 0);

    public static SimplePos of(double x, double y, double z) {
        return new SimplePos(x, y, z, 0, 0);
    }

    public static SimplePos of(double x, double y, double z, float yaw, float pitch) {
        return new SimplePos(x, y, z, yaw, pitch);
    }

    public Point toPoint() {
        return new Pos(x, y, z, yaw, pitch);
    }

    public SimpleVec toVec() {
        return new SimpleVec(x, y, z);
    }

    @Override
    public SimplePos cloneObj() {
        return new SimplePos(x, y, z, yaw, pitch);
    }
}
