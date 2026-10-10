package space.pietr.lobby.common.utils.common;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class JsonUtils {

    public static <T> void setIfPresent(
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
