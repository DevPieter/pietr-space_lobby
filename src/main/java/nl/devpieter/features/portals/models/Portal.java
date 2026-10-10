package nl.devpieter.features.portals.models;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import space.pietr.lobby.common.features.world.World;

public record Portal(
        Instance instance,
        World destinationWorld,
        Pos destinationPos,
        Point origin,
        BoundingBox boundingBox
) {
}
