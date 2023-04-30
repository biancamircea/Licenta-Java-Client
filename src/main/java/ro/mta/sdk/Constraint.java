package ro.mta.sdk;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class Constraint {
    private final String contextName;
    private final Operator operator;
    @Nullable private final List<String> values;


    public Constraint(String contextName, Operator operator, @Nullable List<String> values) {
        this.contextName = contextName;
        this.operator = operator;
        this.values = values;
    }

    public String getContextName() {
        return contextName;
    }

    public Operator getOperator() {
        return operator;
    }

    @Nullable
    public List<String> getValues() {
        return values != null ? values : Collections.emptyList();
    }
}
