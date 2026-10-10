package space.pietr.lobby.common.features.world.settings;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.pietr.lobby.common.models.simple.SimpleVec;
import space.pietr.lobby.common.types.IClonable;

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
