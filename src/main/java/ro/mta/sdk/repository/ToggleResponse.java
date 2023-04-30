package ro.mta.sdk.repository;

import ro.mta.sdk.FeatureToggle;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ToggleResponse {

    public enum Status {
        NOT_CHANGED,
        CHANGED,
        UNAVAILABLE
    }

    private final Status status;
    private final int httpStatusCode;
    private final ToggleCollection toggleCollection;
    @Nullable
    private String location;

    public ToggleResponse(Status status, ToggleCollection toggleCollection) {
        this.status = status;
        this.httpStatusCode = 200;
        this.toggleCollection = toggleCollection;
    }

    public ToggleResponse(Status status, int httpStatusCode) {
        this.status = status;
        this.httpStatusCode = httpStatusCode;
        List<FeatureToggle> emptyList = Collections.emptyList();
        this.toggleCollection = new ToggleCollection(emptyList);
    }

    public ToggleResponse(Status status, int httpStatusCode, @Nullable String location) {
        this(status, httpStatusCode);
        this.location = location;
    }

    public Status getStatus() {
        return status;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public ToggleCollection getToggleCollection() {
        return toggleCollection;
    }

    @Nullable
    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "ToggleResponse{" +
                "status=" + status +
                ", httpStatusCode=" + httpStatusCode +
                ", toggleCollection=" + toggleCollection +
                ", location='" + location + '\'' +
                '}';
    }
}
