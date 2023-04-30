package ro.mta.sdk.constraints;

import ro.mta.sdk.Constraint;
import ro.mta.sdk.ToggleSystemContext;

public interface ConstraintOperator {
    boolean evaluate(Constraint constraint, ToggleSystemContext context);
}
