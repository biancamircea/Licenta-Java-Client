package ro.mta.sdk.evaluator;

import javax.annotation.Nullable;

public class FeatureEvaluationResponse {



    public enum Status {
        SUCCESS,
        ERROR
    }
    @Nullable
    private Boolean enabled;
    private transient Status status;

    public FeatureEvaluationResponse(@Nullable Boolean enabled, Status status) {
        this.enabled = enabled;
        this.status = status;
    }

    @Nullable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(@Nullable Boolean enabled) {
        this.enabled = enabled;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
