package nl.devpieter.features.world.models.settings;

import nl.devpieter.features.world.setting.IClonable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record WorldTimeSetting(
        boolean cycle,
        long time
) implements IClonable<WorldTimeSetting> {

    @Contract(" -> new")
    public static @NonNull WorldTimeSetting defaults() {
        return new WorldTimeSetting(false, 0);
    }

    @Override
    public @NonNull WorldTimeSetting cloneObj() {
        return new WorldTimeSetting(cycle, time);
    }
}
