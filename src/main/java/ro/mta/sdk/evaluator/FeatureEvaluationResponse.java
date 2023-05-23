package ro.mta.sdk.evaluator;

import javax.annotation.Nullable;

public class FeatureEvaluationResponse {
    public enum Status {
        SUCCESS,
        ERROR
    }
    @Nullable
    private Boolean enabled;
    @Nullable
    private String payload;

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

    @Nullable
    public String getPayload() {
        return payload;
    }

    public void setPayload(@Nullable String payload) {
        this.payload = payload;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
