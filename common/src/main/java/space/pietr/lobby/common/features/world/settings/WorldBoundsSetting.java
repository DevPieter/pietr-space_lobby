package space.pietr.lobby.common.features.world.settings;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.pietr.lobby.common.models.simple.SimpleRange;
import space.pietr.lobby.common.types.IClonable;

public record WorldBoundsSetting(
        @Nullable SimpleRange x,
        @Nullable SimpleRange y,
        @Nullable SimpleRange z
) implements IClonable<WorldBoundsSetting> {

    @Contract("_ -> new")
    public static @NotNull WorldBoundsSetting defaults(int bounds) {
        return new WorldBoundsSetting(
                new SimpleRange(-bounds, bounds),
                new SimpleRange(-64, 319),
                new SimpleRange(-bounds, bounds)
        );
    }

    @Contract(" -> new")
    public static @NotNull WorldBoundsSetting disabled() {
        return new WorldBoundsSetting(null, null, null);
    }

    @Override
    public @NotNull WorldBoundsSetting cloneObj() {
        return new WorldBoundsSetting(
                x == null ? null : x.cloneObj(),
                y == null ? null : y.cloneObj(),
                z == null ? null : z.cloneObj()
        );
    }
}
