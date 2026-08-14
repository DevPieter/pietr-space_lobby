package nl.devpieter.features.world.setting.gsonAdapters;

import com.google.gson.*;
import nl.devpieter.features.world.models.settings.features.FishingFeatureSetting;
import nl.devpieter.features.world.models.settings.features.SittingFeatureSetting;
import nl.devpieter.features.world.models.settings.features.WorldFeatureSettings;
import nl.devpieter.features.world.setting.utils.JsonUtils;

import java.lang.reflect.Type;

public final class WorldFeatureSettingsGsonAdapter implements JsonSerializer<WorldFeatureSettings>, JsonDeserializer<WorldFeatureSettings> {

    @Override
    public JsonElement serialize(WorldFeatureSettings settings, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.add("sitting", context.serialize(settings.sitting()));
        json.add("fishing", context.serialize(settings.fishing()));

        return json;
    }

    @Override
    public WorldFeatureSettings deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) throw new JsonParseException("World settings must be a JSON object");

        JsonObject object = json.getAsJsonObject();
        WorldFeatureSettings settings = new WorldFeatureSettings();

        JsonUtils.setIfPresent(object, "sitting", SittingFeatureSetting.class, context, settings::withSitting);
        JsonUtils.setIfPresent(object, "fishing", FishingFeatureSetting.class, context, settings::withFishing);

        return settings;
    }
}
