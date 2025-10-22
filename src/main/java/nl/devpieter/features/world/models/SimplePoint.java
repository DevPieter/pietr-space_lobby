package nl.devpieter.features.world.models;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

public record SimplePoint(
        double x,
        double y,
        double z
) {

    public Point toPoint() {
        return new Pos(x, y, z);
    }
}
