package nl.devpieter.features.world.gsonAdapters;

import com.google.gson.*;
import space.pietr.lobby.common.features.world.settings.*;
import space.pietr.lobby.common.features.world.settings.features.WorldFeatureSettings;
import space.pietr.lobby.common.models.simple.SimplePos;
import space.pietr.lobby.common.utils.common.JsonUtils;

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

        JsonUtils.setIfPresent(object, "spawn", SimplePos.class, context, settings::withSpawn);
        JsonUtils.setIfPresent(object, "markerSigns", WorldMarkerSignsSetting.class, context, settings::withMarkerSigns);
        JsonUtils.setIfPresent(object, "bounds", WorldBoundsSetting.class, context, settings::withBounds);
        JsonUtils.setIfPresent(object, "time", WorldTimeSetting.class, context, settings::withTime);
        JsonUtils.setIfPresent(object, "weather", WorldWeatherSetting.class, context, settings::withWeather);
        JsonUtils.setIfPresent(object, "features", WorldFeatureSettings.class, context, settings::withFeatures);

        return settings;
    }
}
