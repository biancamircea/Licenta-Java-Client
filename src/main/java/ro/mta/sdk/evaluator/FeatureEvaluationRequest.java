package ro.mta.sdk.evaluator;

import java.util.List;

public class FeatureEvaluationRequest {
    private List<ContextField> contextFields;
    private List<ZKPProof> zkpProofs;

    public FeatureEvaluationRequest( List<ContextField> contextFields, List<ZKPProof> zkpProofs) {
        this.contextFields = contextFields;
        this.zkpProofs = zkpProofs;
    }

    public List<ContextField> getContextFields() {
        return contextFields;
    }

    public void setContextFields(List<ContextField> contextFields) {
        this.contextFields = contextFields;
    }

    public List<ZKPProof> getZkpProofs() {
        return zkpProofs;
    }
    public void setZkpProofs(List<ZKPProof> zkpProofs) {
        this.zkpProofs = zkpProofs;
    }
}
