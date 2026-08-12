package nl.devpieter.features.world.models.settings;

import nl.devpieter.features.world.setting.IClonable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record WorldWeatherSetting(
        String type
) implements IClonable<WorldWeatherSetting> {

    @Contract(" -> new")
    public static @NonNull WorldWeatherSetting defaults() {
        return new WorldWeatherSetting("clear");
    }

    @Override
    public @NonNull WorldWeatherSetting cloneObj() {
        return new WorldWeatherSetting(type);
    }
}
