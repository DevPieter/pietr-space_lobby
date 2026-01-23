package nl.devpieter.features.world.models.settings;

import nl.devpieter.models.SimpleVec;
import org.jetbrains.annotations.Nullable;

public record WorldMarkerSignsSetting(
        boolean enabled,
        @Nullable SimpleVec center,
        int scanRadius
) {
}
