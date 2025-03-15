package ro.mta.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.evaluator.EvaluatorService;
import ro.mta.sdk.evaluator.EvaluatorServiceImpl;
import ro.mta.sdk.metric.MetricService;
import ro.mta.sdk.metric.MetricServiceImpl;
import ro.mta.sdk.repository.FeatureToggleRepository;
import ro.mta.sdk.repository.ToggleRepository;
import ro.mta.sdk.util.ConstraintUtil;

public class ToggleSystemClient {
    private static final Logger LOG = LoggerFactory.getLogger(ToggleSystemClient.class);
    private final ToggleSystemConfig toggleSystemConfig;
    private final ToggleSystemContextProvider toggleSystemContextProvider;
    private final ToggleRepository toggleRepository;
    private final MetricService metricService;
    private final EvaluatorService evaluatorService;

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

    public boolean isEnabled(String toggleName){
        return isEnabled(toggleName, toggleSystemContextProvider.getContext());
    }
    public boolean isEnabled(String toggleName, ToggleSystemContext context){
        return isEnabled(toggleName, context, false);
    }
    public boolean isEnabled(String toggleName, boolean defaultSetting){
        return isEnabled(toggleName, toggleSystemContextProvider.getContext(), defaultSetting);
    }
    public boolean isEnabled(String toggleName, ToggleSystemContext context, boolean defaultSetting){
        if(toggleSystemConfig.isRemoteEvaluation()){
            return checkRemote(toggleName, context, defaultSetting);
        } else {
            return checkRepo(toggleName, context, defaultSetting);
        }
    }

    private boolean checkRemote(String toggleName, ToggleSystemContext context, boolean defaultSetting){
        //ToggleSystemContext enhancedContext = context.applyStaticFields(toggleSystemConfig);
        return evaluatorService.remoteEvalution(toggleName, context, defaultSetting);
    }

    private boolean checkRepo(String toggleName, ToggleSystemContext context, boolean defaultSetting){
        boolean enabled = defaultSetting;
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        ToggleSystemContext enhancedContext = context.applyStaticFields(toggleSystemConfig);

        if(featureToggle == null){
            return enabled;
        } else if(!featureToggle.isEnabled()) {
            return false;
        } else if(featureToggle.getConstraintsList().size() == 0){
            return true;
        } else {
            enabled = ConstraintUtil.validate(featureToggle.getConstraintsList(),enhancedContext);
        }
        return enabled;
    }

    public String getPayload(String toggleName){
        return getPayload(toggleName, toggleSystemContextProvider.getContext());
    }
    public String getPayload(String toggleName, ToggleSystemContext context){
        return getPayload(toggleName, context, "default");
    }
    public String getPayload(String toggleName, String defaultPayload){
        return getPayload(toggleName, toggleSystemContextProvider.getContext(), defaultPayload);
    }
    public String getPayload(String toggleName, ToggleSystemContext context, String defaultPayload){
        if(toggleSystemConfig.isRemoteEvaluation()){
            return getPayloadFromRemote(toggleName, context, defaultPayload);
        } else {
            return getPayloadFromRepo(toggleName, context, defaultPayload);
        }
    }

    public String getPayloadFromRepo(String toggleName, ToggleSystemContext context, String defaultPayload){
        boolean enabled = checkRepo(toggleName, context, false);
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        if(featureToggle == null){
            return defaultPayload;
        } else {
            if(featureToggle.getDisabledValue() == null || featureToggle.getEnabledValue() == null){
                return defaultPayload;
            }
            if(enabled){
                return featureToggle.getEnabledValue();
            } else {
                return featureToggle.getDisabledValue();
            }
        }
    }

    public String getPayloadFromRemote(String toggleName, ToggleSystemContext context, String defaultPayload){
        //ToggleSystemContext enhancedContext = context.applyStaticFields(toggleSystemConfig);
        boolean enabled = evaluatorService.remoteEvalution(toggleName,context, false);
        return evaluatorService.remotePayload(toggleName, enabled, context, defaultPayload);
    }
}
