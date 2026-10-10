package space.pietr.lobby.common.features.world.settings.features;

import space.pietr.lobby.common.types.IClonable;

public record SittingFeatureSetting(
        boolean enabled
) implements IClonable<SittingFeatureSetting> {

    @Override
    public SittingFeatureSetting cloneObj() {
        return new SittingFeatureSetting(enabled);
    }
}
