//package ro.mta.sdk;
//
//import ro.mta.sdk.evaluator.ContextField;
//
//import javax.annotation.Nullable;
//import java.time.LocalDateTime;
//import java.util.*;
//
//public class ToggleSystemContext {
//    private final Map<String, String> properties;
//
//    private ToggleSystemContext(
//            Map<String, String> properties) {
//        this.properties = properties;
//    }
//
//    public Optional<String> getPropertyByName(String contextName) {
//        return Optional.ofNullable(properties.get(contextName));
//    }
//
//    public List<ContextField> getAllContextFields(){
//        List<ContextField> contextFields = new ArrayList<>();
//        for (Map.Entry<String, String> entry : properties.entrySet()) {
//            contextFields.add(new ContextField(entry.getKey(), entry.getValue()));
//        }
//        return contextFields;
//    }
//
//    public String getAllContextFieldsToString(){
//        List<ContextField> contextFields = getAllContextFields();
//        StringBuilder sb = new StringBuilder();
//        String delimiter = "#";
//        for (ContextField field : contextFields) {
//            sb.append(field.getName())
//                    .append("=")
//                    .append(field.getValue())
//                    .append(delimiter);
//        }
//        return sb.toString();
//    }
//
//
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    public static class Builder {
//        private final Map<String, String> properties= new HashMap<>();
//
//        public Builder() {}
//
//        public Builder(ToggleSystemContext context) {
//            this.properties.putAll(context.properties);
//        }
//
//        public Builder addContext(String name, String value) {
//            properties.put(name, value);
//            return this;
//        }
//
//        public ToggleSystemContext build() {
//            return new ToggleSystemContext(properties);
//        }
//
//    }
//
//}

package ro.mta.sdk;

import ro.mta.sdk.evaluator.ContextField;

import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

public class ToggleSystemContext {
    private final Map<String, String> properties;
    private final Double locationX;
    private final Double locationY;

    private ToggleSystemContext(
            Map<String, String> properties,
            Double locationX,
            Double locationY) {
        this.properties = properties;
        this.locationX = locationX;
        this.locationY = locationY;
    }

    public Optional<String> getPropertyByName(String contextName) {
        return Optional.ofNullable(properties.get(contextName));
    }

    public Optional<Double> getLocationX() {
        return Optional.ofNullable(locationX);
    }

    public Optional<Double> getLocationY() {
        return Optional.ofNullable(locationY);
    }

    public boolean hasLocation() {
        return locationX != null && locationY != null;
    }

    public List<ContextField> getAllContextFields() {
        List<ContextField> contextFields = new ArrayList<>();

        properties.forEach((key, value) ->
                contextFields.add(new ContextField(key, value))
        );

        if (hasLocation()) {
            contextFields.add(new ContextField("location_x", String.valueOf(locationX)));
            contextFields.add(new ContextField("location_y", String.valueOf(locationY)));
        }

        return contextFields;
    }

    public String getAllContextFieldsToString() {
        return getAllContextFields().stream()
                .map(field -> field.getName() + "=" + field.getValue())
                .collect(Collectors.joining("#"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> properties = new HashMap<>();
        private Double locationX;
        private Double locationY;

        public Builder() {}

        public Builder(ToggleSystemContext context) {
            this.properties.putAll(context.properties);
            this.locationX = context.locationX;
            this.locationY = context.locationY;
        }

        public Builder addContext(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public Builder addLocation(double x_coordinate, double y_coordinate) {
            this.locationX = x_coordinate;
            this.locationY = y_coordinate;
            return this;
        }

        public ToggleSystemContext build() {
            return new ToggleSystemContext(properties, locationX, locationY);
        }
    }
}