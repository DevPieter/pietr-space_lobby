package nl.devpieter.models;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public record BlockHolder(Instance instance, Point position, Block block) {
}
