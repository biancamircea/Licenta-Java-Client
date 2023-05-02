package ro.mta.sdk.evaluator;

import java.util.List;

public class FeatureEvaluationRequest {
    private String toggleName;
    private List<ContextField> contextFields;

    public FeatureEvaluationRequest(String toggleName, List<ContextField> contextFields) {
        this.toggleName = toggleName;
        this.contextFields = contextFields;
    }

    public String getToggleName() {
        return toggleName;
    }

    public void setToggleName(String toggleName) {
        this.toggleName = toggleName;
    }

    public List<ContextField> getContextFields() {
        return contextFields;
    }

    public void setContextFields(List<ContextField> contextFields) {
        this.contextFields = contextFields;
    }
}
