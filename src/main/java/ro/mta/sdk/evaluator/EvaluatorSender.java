package ro.mta.sdk.evaluator;

public interface EvaluatorSender {
    FeatureEvaluationResponse evaluateToggle(FeatureEvaluationRequest featureEvaluationRequest);
    ConstraintResponse fetchConstraints(String apiToken, String toggleNam);
    boolean sendZKPVerificationRequest(String toggleName, String apiToken, String proof);
}
