package nl.devpieter.models;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;

import java.util.Objects;

public record PointHolder(Instance instance, Point position) {

    public static PointHolder of(BlockHolder block) {
        return new PointHolder(block.instance(), block.position());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointHolder(Instance instance1, Point position1))) return false;

        return instance.getUuid().equals(instance1.getUuid())
                && position.samePoint(position1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance.getUuid(), position.blockX(), position.blockY(), position.blockZ());
    }
}

