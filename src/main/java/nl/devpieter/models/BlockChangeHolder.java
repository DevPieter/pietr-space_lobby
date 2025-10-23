package nl.devpieter.models;

import net.minestom.server.instance.Instance;

public record BlockChangeHolder(Instance instance, BlockHolder original, BlockHolder replacement) {
}
