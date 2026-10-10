package space.pietr.lobby.common.features.world.settings;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import space.pietr.lobby.common.types.IClonable;

public record WorldTimeSetting(
        boolean cycle,
        long time
) implements IClonable<WorldTimeSetting> {

    @Contract(" -> new")
    public static @NotNull WorldTimeSetting defaults() {
        return new WorldTimeSetting(false, 0);
    }

    @Override
    public @NotNull WorldTimeSetting cloneObj() {
        return new WorldTimeSetting(cycle, time);
    }
}
