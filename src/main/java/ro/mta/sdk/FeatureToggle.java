package ro.mta.sdk;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class FeatureToggle {
    private final String name;
    private final boolean enabled;
    @Nullable
    private String enabledValue;
    @Nullable
    private String disabledValue;

    private final List<Constraint> constraintsList;

    public FeatureToggle(String name, boolean enabled) {
        this(name, enabled, Collections.emptyList());
    }

    public FeatureToggle(String name, boolean enabled, List<Constraint> constraintsList) {
        this.name = name;
        this.enabled = enabled;
        this.constraintsList = constraintsList;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public String getEnabledValue() {
        return enabledValue;
    }

    @Nullable
    public String getDisabledValue() {
        return disabledValue;
    }

    public List<Constraint> getConstraintsList() {
        return constraintsList;
    }

    @Override
    public String toString() {
        return "FeatureToggle{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", constraintsList=" + constraintsList +
                '}';
    }
}
