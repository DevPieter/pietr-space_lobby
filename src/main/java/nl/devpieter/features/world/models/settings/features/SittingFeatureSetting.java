package nl.devpieter.features.world.models.settings.features;

import nl.devpieter.features.world.setting.IClonable;

public record SittingFeatureSetting(
        boolean enabled
) implements IClonable<SittingFeatureSetting> {

    @Override
    public SittingFeatureSetting cloneObj() {
        return new SittingFeatureSetting(enabled);
    }
}
