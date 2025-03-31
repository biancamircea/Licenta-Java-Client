package ro.mta.sdk.evaluator;

import java.util.Collections;
import java.util.List;

public class ConstraintResponse {
    private List<ConstraintDTO> constraints;

    public List<ConstraintDTO> getConstraints() {
        return constraints != null ? constraints : Collections.emptyList();
    }

    public void setConstraints(List<ConstraintDTO> constraints) {
        this.constraints = constraints;
    }

    public List<String> getValuesForContext(String context) {
        for (ConstraintDTO constraint : constraints) {
            if (constraint.getContextName().equals(context)) {
                return constraint.getValues();
            }
        }
        return null;
    }

    public String getOperatorForContext(String context) {
        for (ConstraintDTO constraint : constraints) {
            if (constraint.getContextName().equals(context)) {
                return constraint.getOperator();
            }
        }
        return null;
    }
}

