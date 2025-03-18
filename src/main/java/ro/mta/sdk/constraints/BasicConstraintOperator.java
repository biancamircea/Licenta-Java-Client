package ro.mta.sdk.constraints;

import ro.mta.sdk.Constraint;
import ro.mta.sdk.ToggleSystemContext;

import java.util.List;
import java.util.Optional;

public class BasicConstraintOperator implements ConstraintOperator{

    @Override
    public boolean evaluate(Constraint constraint, ToggleSystemContext context) {
        List<String> values = constraint.getValues();
        Optional<String> contextValue = context.getPropertyByName(constraint.getContextName());
        return switch (constraint.getOperator()) {
            case IN -> isIn(values, contextValue);
            case NOT_IN -> !isIn(values, contextValue);
            case GREATER_THAN -> isGreaterThan(values, contextValue);
            case LESS_THAN -> isLessThan(values, contextValue);
            default -> false;
        };
    }

    private boolean isIn(List<String> values, Optional<String> value) {
        return value.map(v ->
            values.stream()
                .anyMatch(v2 -> v2.equals(v)))
            .orElse(false);
    }

    private boolean isGreaterThan(List<String> values, Optional<String> value) {
        return value.filter(v -> !Double.isNaN(tryParseDouble(v)))
                .map(v -> values.stream()
                        .map(this::tryParseDouble)
                        .filter(d -> !Double.isNaN(d))
                        .anyMatch(d -> d < tryParseDouble(v)))
                .orElse(false);
    }

    private boolean isLessThan(List<String> values, Optional<String> value) {
        return value.filter(v -> !Double.isNaN(tryParseDouble(v)))
                .map(v -> values.stream()
                        .map(this::tryParseDouble)
                        .filter(d -> !Double.isNaN(d))
                        .anyMatch(d -> d > tryParseDouble(v)))
                .orElse(false);
    }

    private double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
