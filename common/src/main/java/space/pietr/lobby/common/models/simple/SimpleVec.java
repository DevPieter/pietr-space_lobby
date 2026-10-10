package space.pietr.lobby.common.models.simple;

import net.minestom.server.coordinate.Vec;
import space.pietr.lobby.common.types.IClonable;

public record SimpleVec(
        double x,
        double y,
        double z
) implements IClonable<SimpleVec> {

    public static final SimpleVec ZERO = new SimpleVec(0, 0, 0);

    public static SimpleVec of(double x, double y, double z) {
        return new SimpleVec(x, y, z);
    }

    public Vec toVec() {
        return new Vec(x, y, z);
    }

    @Override
    public SimpleVec cloneObj() {
        return new SimpleVec(x, y, z);
    }
}
