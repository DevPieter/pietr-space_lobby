package nl.devpieter.features.resourcePack.models;

import net.kyori.adventure.resource.ResourcePackStatus;

import java.util.UUID;

public record PackStatus(
        UUID packId,
        ResourcePackStatus status
) {
}
