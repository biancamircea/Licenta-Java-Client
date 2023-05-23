package ro.mta.sdk.evaluator;

import ro.mta.sdk.ToggleSystemContext;

public interface EvaluatorService {
    boolean remoteEvalution(String toggleName, ToggleSystemContext systemContext, boolean defaultSetting);
    String remotePayload(String toggleName, Boolean enabled, ToggleSystemContext systemContext, String defaultPayload);
}
