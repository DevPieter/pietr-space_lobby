package nl.devpieter.features.signs.models;

import nl.devpieter.features.signs.enums.SignMaterial;
import nl.devpieter.features.signs.enums.SignType;
import nl.devpieter.models.BlockHolder;

import java.util.List;

public record SignHolder(
        BlockHolder block,
        SignType type,
        SignMaterial material,
        List<String> lines
) {
}
