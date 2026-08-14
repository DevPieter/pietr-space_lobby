package nl.devpieter.features.world.models.settings.features;

import nl.devpieter.features.world.setting.IClonable;

public record FishingFeatureSetting(
        boolean enabled
) implements IClonable<FishingFeatureSetting> {

    @Override
    public FishingFeatureSetting cloneObj() {
        return new FishingFeatureSetting(enabled);
    }
}
