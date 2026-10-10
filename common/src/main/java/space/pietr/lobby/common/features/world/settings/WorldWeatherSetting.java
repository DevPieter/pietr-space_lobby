package space.pietr.lobby.common.features.world.settings;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import space.pietr.lobby.common.types.IClonable;

public record WorldWeatherSetting(
        String type
) implements IClonable<WorldWeatherSetting> {

    @Contract(" -> new")
    public static @NotNull WorldWeatherSetting defaults() {
        return new WorldWeatherSetting("clear");
    }

    @Override
    public @NotNull WorldWeatherSetting cloneObj() {
        return new WorldWeatherSetting(type);
    }
}
