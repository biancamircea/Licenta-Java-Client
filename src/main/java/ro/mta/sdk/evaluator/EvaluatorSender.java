package ro.mta.sdk.evaluator;

public interface EvaluatorSender {
    FeatureEvaluationResponse evaluateToggle(FeatureEvaluationRequest featureEvaluationRequest);
}
