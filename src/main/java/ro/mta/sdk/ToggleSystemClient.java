package ro.mta.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.evaluator.EvaluatorService;
import ro.mta.sdk.evaluator.EvaluatorServiceImpl;
import ro.mta.sdk.metric.MetricService;
import ro.mta.sdk.metric.MetricServiceImpl;
import ro.mta.sdk.repository.FeatureToggleRepository;
import ro.mta.sdk.repository.ToggleRepository;

public class ToggleSystemClient {
    private static final Logger LOG = LoggerFactory.getLogger(ToggleSystemClient.class);
    private final ToggleSystemConfig toggleSystemConfig;
    private final ToggleSystemContextProvider toggleSystemContextProvider;
    private final ToggleRepository toggleRepository;
    private final MetricService metricService;
    private final EvaluatorService evaluatorService;

    static {
        TrustAllCertificates.trustAllCertificates();
    }

    private static ToggleRepository defaultToggleRepository(ToggleSystemConfig systemConfig) {
        if(systemConfig.isRemoteEvaluation()){
            return null;
        } else {
            return new FeatureToggleRepository(systemConfig);
        }
    }

    private static EvaluatorService defaultEvaluatorService(ToggleSystemConfig systemConfig) {
        if(systemConfig.isRemoteEvaluation()){
            return new EvaluatorServiceImpl(systemConfig);
        } else {
            return null;
        }
    }

    public ToggleSystemClient(ToggleSystemConfig systemConfig){
        this(systemConfig,
                defaultToggleRepository(systemConfig),
                defaultEvaluatorService(systemConfig));
    }

    public ToggleSystemClient(ToggleSystemConfig systemConfig,
                              ToggleRepository toggleRepository,
                              EvaluatorService evaluatorService){
        this(systemConfig,
                systemConfig.getToggleSystemContextProvider(),
                toggleRepository,
                new MetricServiceImpl(systemConfig),
                evaluatorService);
    }

    public ToggleSystemClient(ToggleSystemConfig toggleSystemConfig,
                              ToggleSystemContextProvider toggleSystemContextProvider,
                              ToggleRepository toggleRepository,
                              MetricService metricService,
                              EvaluatorService evaluatorService) {
        this.toggleSystemConfig = toggleSystemConfig;
        this.toggleSystemContextProvider = toggleSystemContextProvider;
        this.toggleRepository = toggleRepository;
        this.metricService = metricService;
        this.evaluatorService = evaluatorService;
        metricService.register();
    }

    public boolean isEnabled(String toggleName) {
        return isEnabled(toggleName, toggleSystemContextProvider.getContext());
    }

    public boolean isEnabled(String toggleName, ToggleSystemContext context){
        return evaluatorService.remoteEvalutionWithZKP(toggleName, context);
    }


    public String getPayload(String toggleName){
        return getPayload(toggleName, toggleSystemContextProvider.getContext());
    }
    public String getPayload(String toggleName, ToggleSystemContext context){
        boolean enabled = evaluatorService.remoteEvalutionWithZKP(toggleName,context);
        return evaluatorService.remotePayload(toggleName, enabled, context, "default");
    }
}
