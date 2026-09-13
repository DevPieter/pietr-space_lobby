package nl.devpieter.features.world.models.settings;

import nl.devpieter.features.world.setting.IClonable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
