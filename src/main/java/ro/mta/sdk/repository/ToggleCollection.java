package ro.mta.sdk.repository;

import ro.mta.sdk.FeatureToggle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ToggleCollection {
    private final Collection<FeatureToggle> features;
    private final transient Map<String, FeatureToggle> cache;

    public ToggleCollection(Collection<FeatureToggle> featureToggles) {
        this.features = ensureNotNull(featureToggles);
        this.cache = new ConcurrentHashMap<>();
        for (FeatureToggle featureToggle : this.features) {
            cache.put(featureToggle.getName(), featureToggle);
        }
    }

    private Collection<FeatureToggle> ensureNotNull(@Nullable Collection<FeatureToggle> features) {
        if (features == null) {
            return Collections.emptyList();
        }
        return features;
    }

    public Collection<FeatureToggle> getFeatureToggles() {
        return features;
    }
    public FeatureToggle getToggle(String name) {
        return cache.get(name);
    }

}
