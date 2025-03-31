package ro.mta.sdk.evaluator;

import ro.mta.sdk.ToggleSystemContext;

public interface EvaluatorService {
    String remotePayload(String toggleName, Boolean enabled, ToggleSystemContext systemContext, String defaultPayload);
    boolean remoteEvalutionWithZKP(String toggleName, ToggleSystemContext systemContext);
}
