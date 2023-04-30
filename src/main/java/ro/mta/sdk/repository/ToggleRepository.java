package ro.mta.sdk.repository;

import ro.mta.sdk.FeatureToggle;

import javax.annotation.Nullable;
import java.util.List;

public interface ToggleRepository {
    @Nullable
    FeatureToggle getToggle(String name);

    List<String> getFeatureNames();
}
