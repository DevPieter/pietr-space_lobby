package nl.devpieter.features.world.models;

import net.minestom.server.instance.InstanceContainer;

public record LoadedWorld(World world, InstanceContainer instance) {
}
