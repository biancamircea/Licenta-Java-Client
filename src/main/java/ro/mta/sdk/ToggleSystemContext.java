package ro.mta.sdk;

import ro.mta.sdk.evaluator.ContextField;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;

public class ToggleSystemContext {
    private final Optional<String> appName;
    private final Optional<String> environment;
    private final Optional<String> userId;
    private final Optional<String> remoteAddress;
    private final Optional<LocalDateTime> currentTime;
    private final Map<String, String> properties;

    private ToggleSystemContext(
            @Nullable String appName,
            @Nullable String environment,
            @Nullable String userId,
            @Nullable String remoteAddress,
            @Nullable LocalDateTime currentTime,
            Map<String, String> properties) {
        this.appName = Optional.ofNullable(appName);
        this.environment = Optional.ofNullable(environment);
        this.userId = Optional.ofNullable(userId);
        this.remoteAddress = Optional.ofNullable(remoteAddress);
        this.currentTime = Optional.ofNullable(currentTime);
        this.properties = properties;
    }

    public Optional<String> getAppName() {
        return appName;
    }

    public Optional<String> getEnvironment() {
        return environment;
    }

    public Optional<String> getUserId() {
        return userId;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public Optional<LocalDateTime> getCurrentTime() {
        return currentTime;
    }

    public Optional<String> getPropertyByName(String contextName) {
        return switch (contextName) {
            case "environment" -> environment;
            case "appName" -> appName;
            case "userId" -> userId;
            case "remoteAddress" -> remoteAddress;
            default -> Optional.ofNullable(properties.get(contextName));
        };
    }

    public List<ContextField> getAllContextFields(){
        List<ContextField> contextFields = new ArrayList<>();

        if(getAppName().isPresent()){
            contextFields.add(new ContextField("appName", getAppName().get()));
        }
        if(getEnvironment().isPresent()){
            contextFields.add(new ContextField("environment", getEnvironment().get()));
        }
        if(getUserId().isPresent()){
            contextFields.add(new ContextField("userId", getUserId().get()));
        }
        if(getRemoteAddress().isPresent()){
            contextFields.add(new ContextField("remoteAddress", getRemoteAddress().get()));
        }
        if(getCurrentTime().isPresent()){
            contextFields.add(new ContextField("currentTime", getCurrentTime().get().toString()));
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            contextFields.add(new ContextField(entry.getKey(), entry.getValue()));
        }
        return contextFields;
    }

    public String getAllContextFieldsToString(){
        List<ContextField> contextFields = getAllContextFields();
        StringBuilder sb = new StringBuilder();
        String delimiter = "#";
        for (ContextField field : contextFields) {
            sb.append(field.getName())
                    .append("=")
                    .append(field.getValue())
                    .append(delimiter);
        }
        return sb.toString();
    }

    public ToggleSystemContext applyStaticFields(ToggleSystemConfig config) {
//        create a CONTEXT from CONFIG, setting environment and appName from CONFIG
        Builder builder = new Builder(this);
        if (this.environment.isEmpty()) {
            builder.environment(config.getEnvironment());
        }
        if (this.appName.isEmpty()) {
            builder.appName(config.getAppName());
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable private  String appName;
        @Nullable private String environment;
        @Nullable private String userId;
        @Nullable private String remoteAddress;
        @Nullable private LocalDateTime currentTime;
        private final Map<String, String> properties= new HashMap<>();

        public Builder appName(@Nullable String appName) {
            this.appName = appName;
            return this;
        }

        public Builder environment(@Nullable String environment) {
            this.environment = environment;
            return this;
        }

        public Builder userId(@Nullable String userId) {
            this.userId = userId;
            return this;
        }

        public Builder remoteAddress(@Nullable String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder currentTime(@Nullable LocalDateTime currentTime) {
            this.currentTime = currentTime;
            return this;
        }

        public Builder() {}

        public Builder(ToggleSystemContext context) {
            context.appName.ifPresent(val -> this.appName = val);
            context.environment.ifPresent(val -> this.environment = val);
            context.userId.ifPresent(val -> this.userId = val);
            context.remoteAddress.ifPresent(val -> this.remoteAddress = val);
            context.currentTime.ifPresent(val -> this.currentTime = val);
            this.properties.putAll(context.properties);
        }

        public Builder now() {
            this.currentTime = LocalDateTime.now();
            return this;
        }

        public Builder addProperty(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public ToggleSystemContext build() {
            return new ToggleSystemContext(
                    appName,
                    environment,
                    userId,
                    remoteAddress,
                    currentTime,
                    properties);
        }

    }

}
