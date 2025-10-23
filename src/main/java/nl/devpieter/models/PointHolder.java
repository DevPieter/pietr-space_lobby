package nl.devpieter.models;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;

public record PointHolder(Instance instance, Point position) {

    public static PointHolder of(BlockHolder block) {
        return new PointHolder(block.instance(), block.position());
    }
}
