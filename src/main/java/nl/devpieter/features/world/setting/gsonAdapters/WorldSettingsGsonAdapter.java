package nl.devpieter.features.world.setting.gsonAdapters;

import com.google.gson.*;
import nl.devpieter.features.world.models.settings.*;
import nl.devpieter.features.world.models.settings.features.WorldFeatureSettings;
import nl.devpieter.features.world.setting.utils.JsonUtils;
import nl.devpieter.models.SimplePoint;

import java.lang.reflect.Type;

public final class WorldSettingsGsonAdapter implements JsonSerializer<WorldSettings>, JsonDeserializer<WorldSettings> {

    @Override
    public JsonElement serialize(WorldSettings settings, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.add("spawn", context.serialize(settings.spawn()));
        json.add("markerSigns", context.serialize(settings.markerSigns()));
        json.add("bounds", context.serialize(settings.bounds()));
        json.add("time", context.serialize(settings.time()));
        json.add("weather", context.serialize(settings.weather()));
        json.add("features", context.serialize(settings.features()));

        return json;
    }

    @Override
    public WorldSettings deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) throw new JsonParseException("World settings must be a JSON object");

        JsonObject object = json.getAsJsonObject();
        WorldSettings settings = new WorldSettings();

        JsonUtils.setIfPresent(object, "spawn", SimplePoint.class, context, settings::withSpawn);
        JsonUtils.setIfPresent(object, "markerSigns", WorldMarkerSignsSetting.class, context, settings::withMarkerSigns);
        JsonUtils.setIfPresent(object, "bounds", WorldBoundsSetting.class, context, settings::withBounds);
        JsonUtils.setIfPresent(object, "time", WorldTimeSetting.class, context, settings::withTime);
        JsonUtils.setIfPresent(object, "weather", WorldWeatherSetting.class, context, settings::withWeather);
        JsonUtils.setIfPresent(object, "features", WorldFeatureSettings.class, context, settings::withFeatures);

        return settings;
    }
}
