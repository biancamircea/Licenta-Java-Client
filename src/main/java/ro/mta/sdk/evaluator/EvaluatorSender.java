package ro.mta.sdk.evaluator;

import java.util.List;
import java.util.Map;

public interface EvaluatorSender {
    ConstraintResponse fetchConstraints(String apiToken, String toggleNam);
    FeatureEvaluationResponse sendZKPVerificationRequest(String toggleName, String apiToken,
                                              List<ContextField> contextFields, List<ZKPProof> proofs);
}
