package nl.devpieter.models;

import net.minestom.server.coordinate.Vec;

public record SimpleVec(
        double x,
        double y,
        double z
) {

    public Vec toVec() {
        return new Vec(x, y, z);
    }
}
