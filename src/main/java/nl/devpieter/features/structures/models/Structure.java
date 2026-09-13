package nl.devpieter.features.structures.models;

import nl.devpieter.features.structures.models.nbt.StructureNbt;

public record Structure(
        String id,
        String path,
        StructureNbt nbt
) {
}
