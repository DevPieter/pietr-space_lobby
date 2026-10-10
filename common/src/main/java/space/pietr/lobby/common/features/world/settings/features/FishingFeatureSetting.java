package space.pietr.lobby.common.features.world.settings.features;

import space.pietr.lobby.common.types.IClonable;

public record FishingFeatureSetting(
        boolean enabled
) implements IClonable<FishingFeatureSetting> {

    @Override
    public FishingFeatureSetting cloneObj() {
        return new FishingFeatureSetting(enabled);
    }
}
