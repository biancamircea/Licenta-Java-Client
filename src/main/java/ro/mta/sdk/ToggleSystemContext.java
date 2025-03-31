package ro.mta.sdk;

import ro.mta.sdk.evaluator.ContextField;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;

public class ToggleSystemContext {
    private final Map<String, String> properties;

    private ToggleSystemContext(
            Map<String, String> properties) {
        this.properties = properties;
    }

    public Optional<String> getPropertyByName(String contextName) {
        return Optional.ofNullable(properties.get(contextName));
    }

    public List<ContextField> getAllContextFields(){
        List<ContextField> contextFields = new ArrayList<>();
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


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> properties= new HashMap<>();

        public Builder() {}

        public Builder(ToggleSystemContext context) {
            this.properties.putAll(context.properties);
        }

        public Builder addContext(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public ToggleSystemContext build() {
            return new ToggleSystemContext(properties);
        }

    }

}
