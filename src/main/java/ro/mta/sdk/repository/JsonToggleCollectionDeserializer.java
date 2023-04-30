package ro.mta.sdk.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ro.mta.sdk.FeatureToggle;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class JsonToggleCollectionDeserializer implements JsonDeserializer<ToggleCollection> {
    private static final Type FEATURE_COLLECTION_TYPE =
            new TypeToken<Collection<FeatureToggle>>() {}.getType();
    @Override
    public ToggleCollection deserialize(JsonElement rootElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (!rootElement.getAsJsonObject().has("features")) {
            return null;
        }

        JsonArray featureArray = rootElement.getAsJsonObject().getAsJsonArray("features");

        Collection<FeatureToggle> featureToggles =
                context.deserialize(featureArray, FEATURE_COLLECTION_TYPE);
        return new ToggleCollection(featureToggles);
    }
}
