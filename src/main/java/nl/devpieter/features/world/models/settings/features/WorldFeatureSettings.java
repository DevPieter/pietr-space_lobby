package nl.devpieter.features.world.models.settings.features;

import nl.devpieter.features.world.setting.IClonable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record WorldFeatureSettings(

) implements IClonable<WorldFeatureSettings> {

    @Contract(" -> new")
    public static @NonNull WorldFeatureSettings defaults() {
        return new WorldFeatureSettings();
    }

    @Override
    public @NonNull WorldFeatureSettings cloneObj() {
        return new WorldFeatureSettings();
    }
}
