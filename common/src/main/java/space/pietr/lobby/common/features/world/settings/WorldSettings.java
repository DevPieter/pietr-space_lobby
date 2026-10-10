package space.pietr.lobby.common.features.world.settings;

import space.pietr.lobby.common.types.BakedSetting;
import space.pietr.lobby.common.features.world.settings.features.WorldFeatureSettings;
import space.pietr.lobby.common.models.simple.SimplePos;
import space.pietr.lobby.common.types.IFreezable;

import java.util.List;

public class WorldSettings implements IFreezable {

    private final BakedSetting<SimplePos> spawn;
    private final BakedSetting<WorldMarkerSignsSetting> markerSigns;
    private final BakedSetting<WorldBoundsSetting> bounds;
    private final BakedSetting<WorldTimeSetting> time;
    private final BakedSetting<WorldWeatherSetting> weather;

    private WorldFeatureSettings features;

    private boolean isFrozen = false;

    public WorldSettings() {
        spawn = new BakedSetting<>(new SimplePos(0, 64, 0, 0, 0));
        markerSigns = new BakedSetting<>(WorldMarkerSignsSetting.disabled());
        bounds = new BakedSetting<>(WorldBoundsSetting.disabled());
        time = new BakedSetting<>(WorldTimeSetting.defaults());
        weather = new BakedSetting<>(WorldWeatherSetting.defaults());

        features = new WorldFeatureSettings();
    }

    @Override
    public List<IFreezable> getChildren() {
        return List.of(
                spawn,
                markerSigns,
                bounds,
                time,
                weather,
                features
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

    public WorldSettings withSpawn(SimplePos spawn) {
        this.spawn.setValue(spawn);
        return this;
    }

    public SimplePos spawn() {
        return spawn.value();
    }

    public WorldSettings withMarkerSigns(WorldMarkerSignsSetting markerSigns) {
        this.markerSigns.setValue(markerSigns);
        return this;
    }

    public WorldMarkerSignsSetting markerSigns() {
        return markerSigns.value();
    }

    public WorldSettings withBounds(WorldBoundsSetting bounds) {
        this.bounds.setValue(bounds);
        return this;
    }

    public WorldBoundsSetting bounds() {
        return bounds.value();
    }

    public WorldSettings withTime(WorldTimeSetting time) {
        this.time.setValue(time);
        return this;
    }

    public WorldTimeSetting time() {
        return time.value();
    }

    public WorldSettings withWeather(WorldWeatherSetting weather) {
        this.weather.setValue(weather);
        return this;
    }

    public WorldWeatherSetting weather() {
        return weather.value();
    }

    public WorldSettings withFeatures(WorldFeatureSettings features) {
        this.features = features;
        return this;
    }

    public WorldFeatureSettings features() {
        return features;
    }
}
