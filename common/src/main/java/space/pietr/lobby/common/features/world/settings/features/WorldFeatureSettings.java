package space.pietr.lobby.common.features.world.settings.features;

import space.pietr.lobby.common.types.BakedSetting;
import space.pietr.lobby.common.types.IFreezable;

import java.util.List;

public class WorldFeatureSettings implements IFreezable {

    private final BakedSetting<SittingFeatureSetting> sitting;
    private final BakedSetting<FishingFeatureSetting> fishing;

    private boolean isFrozen = false;

    public WorldFeatureSettings() {
        sitting = new BakedSetting<>(new SittingFeatureSetting(false));
        fishing = new BakedSetting<>(new FishingFeatureSetting(false));
    }

    @Override
    public List<IFreezable> getChildren() {
        return List.of(
                sitting,
                fishing
        );
    }

    @Override
    public void freeze() {
        IFreezable.super.freeze();
        isFrozen = true;
    }

    @Override
    public void melt() {
        IFreezable.super.melt();
        isFrozen = false;
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    public WorldFeatureSettings withSitting(SittingFeatureSetting sitting) {
        this.sitting.setValue(sitting);
        return this;
    }

    public SittingFeatureSetting sitting() {
        return sitting.value();
    }

    public WorldFeatureSettings withFishing(FishingFeatureSetting fishing) {
        this.fishing.setValue(fishing);
        return this;
    }

    public FishingFeatureSetting fishing() {
        return fishing.value();
    }
}
