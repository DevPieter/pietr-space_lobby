package nl.devpieter.features.world.models.settings;

import nl.devpieter.features.world.setting.IClonable;
import nl.devpieter.models.SimpleRange;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record WorldBoundsSetting(
        @Nullable SimpleRange x,
        @Nullable SimpleRange y,
        @Nullable SimpleRange z
) implements IClonable<WorldBoundsSetting> {

    @Contract("_ -> new")
    public static @NonNull WorldBoundsSetting defaults(int bounds) {
        return new WorldBoundsSetting(
                new SimpleRange(-bounds, bounds),
                new SimpleRange(0, 256),
                new SimpleRange(-bounds, bounds)
        );
    }

    @Override
    public WorldBoundsSetting cloneObj() {
        return new WorldBoundsSetting(
                x == null ? null : x.cloneObj(),
                y == null ? null : y.cloneObj(),
                z == null ? null : z.cloneObj()
        );
    }
}
