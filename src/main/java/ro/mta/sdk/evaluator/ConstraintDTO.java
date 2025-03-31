package ro.mta.sdk.evaluator;

import java.util.List;

class ConstraintDTO {
    private Long id;
    private String contextName;
    private String operator;
    private List<String> values;
    private Long isConfidential;

    public String getContextName() {
        return contextName;
    }

    public void setContext(String context) {
        this.contextName = context;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public Long getIsConfidential() {
        return isConfidential;
    }

    public void setIsConfidential(Long isConfidential) {
        this.isConfidential = isConfidential;
    }
}
