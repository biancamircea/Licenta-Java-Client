package ro.mta.sdk.util;

import ro.mta.sdk.Constraint;
import ro.mta.sdk.Operator;
import ro.mta.sdk.ToggleSystemContext;
import ro.mta.sdk.constraints.BasicConstraintOperator;
import ro.mta.sdk.constraints.ConstraintOperator;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintUtil {
    private static final Map<Operator, ConstraintOperator> operators= new HashMap<>();
    static {
        operators.put(Operator.IN, new BasicConstraintOperator());
        operators.put(Operator.NOT_IN, new BasicConstraintOperator());
    }

    public static boolean validate(@Nullable List<Constraint> constraints, ToggleSystemContext context) {
        if (constraints != null && constraints.size() > 0) {
            return constraints.stream().allMatch(c -> validateConstraint(c, context));
        } else {
            return true;
        }
    }

    private static boolean validateConstraint(Constraint constraint, ToggleSystemContext context) {
        ConstraintOperator operator = operators.get(constraint.getOperator());
        if (operator == null) {
            return false;
        }
        return operator.evaluate(constraint, context);
    }
}
