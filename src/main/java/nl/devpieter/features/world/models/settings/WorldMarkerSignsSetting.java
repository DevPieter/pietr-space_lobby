package nl.devpieter.features.world.models.settings;

import nl.devpieter.features.world.setting.IClonable;
import nl.devpieter.models.SimpleVec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record WorldMarkerSignsSetting(
        boolean enabled,
        @Nullable SimpleVec center,
        int scanRadius
) implements IClonable<WorldMarkerSignsSetting> {

    @Contract(" -> new")
    public static @NotNull WorldMarkerSignsSetting disabled() {
        return new WorldMarkerSignsSetting(
                false,
                null,
                0
        );
    }

    @Override
    public @NotNull WorldMarkerSignsSetting cloneObj() {
        return new WorldMarkerSignsSetting(
                enabled,
                center == null ? null : new SimpleVec(center.x(), center.y(), center.z()),
                scanRadius
        );
    }
}
