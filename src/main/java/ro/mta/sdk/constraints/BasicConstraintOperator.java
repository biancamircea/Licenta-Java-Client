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
            default -> false;
        };
    }

    private boolean isIn(List<String> values, Optional<String> value) {
        return value.map(v ->
            values.stream()
                .anyMatch(v2 -> v2.equals(v)))
            .orElse(false);
    }
}
