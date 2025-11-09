package nl.devpieter.models;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Objects;

public record BlockHolder(Instance instance, Point position, Block block) {

    public PointHolder asPointHolder() {
        return new PointHolder(instance, position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockHolder(Instance instance1, Point position1, Block block1))) return false;

        return instance.getUuid().equals(instance1.getUuid())
                && position.samePoint(position1)
                && block.compare(block1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance.getUuid(), position.blockX(), position.blockY(), position.blockZ(), block.id());
    }
}
