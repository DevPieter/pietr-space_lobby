package nl.devpieter.features.world.models.settings;

import nl.devpieter.models.SimpleRange;
import org.jetbrains.annotations.Nullable;

public record WorldBoundsSetting(
        @Nullable SimpleRange x,
        @Nullable SimpleRange y,
        @Nullable SimpleRange z
) {
}
