package nl.devpieter.features.world.setting;

import com.google.gson.*;
import nl.devpieter.features.world.models.settings.*;
import nl.devpieter.features.world.models.settings.features.WorldFeatureSettings;
import nl.devpieter.models.SimplePoint;

import java.lang.reflect.Type;
import java.util.function.Consumer;

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

        setIfPresent(object, "spawn", SimplePoint.class, context, settings::withSpawn);
        setIfPresent(object, "markerSigns", WorldMarkerSignsSetting.class, context, settings::withMarkerSigns);
        setIfPresent(object, "bounds", WorldBoundsSetting.class, context, settings::withBounds);
        setIfPresent(object, "time", WorldTimeSetting.class, context, settings::withTime);
        setIfPresent(object, "weather", WorldWeatherSetting.class, context, settings::withWeather);
        setIfPresent(object, "features", WorldFeatureSettings.class, context, settings::withFeatures);

        return settings;
    }

    private static <T> void setIfPresent(
            JsonObject object,
            String property,
            Class<T> type,
            JsonDeserializationContext context,
            Consumer<T> setter
    ) {
        JsonElement value = object.get(property);
        if (value == null || value instanceof JsonNull) return;

        setter.accept(context.deserialize(value, type));
    }
}
