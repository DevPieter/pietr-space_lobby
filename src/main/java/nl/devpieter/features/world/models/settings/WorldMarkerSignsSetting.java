package nl.devpieter.features.world.models.settings;

import nl.devpieter.features.world.setting.IClonable;
import nl.devpieter.models.SimpleVec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record WorldMarkerSignsSetting(
        boolean enabled,
        @Nullable SimpleVec center,
        int scanRadius
) implements IClonable<WorldMarkerSignsSetting> {

    @Contract("_, _ -> new")
    public static @NonNull WorldMarkerSignsSetting defaults(SimpleVec center, int scanRadius) {
        return new WorldMarkerSignsSetting(
                true,
                center,
                scanRadius
        );
    }

    @Override
    public WorldMarkerSignsSetting cloneObj() {
        return new WorldMarkerSignsSetting(
                enabled,
                center == null ? null : new SimpleVec(center.x(), center.y(), center.z()),
                scanRadius
        );
    }
}
