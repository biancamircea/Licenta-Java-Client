package ro.mta.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.metric.MetricService;
import ro.mta.sdk.metric.MetricServiceImpl;
import ro.mta.sdk.repository.FeatureToggleRepository;
import ro.mta.sdk.repository.ToggleRepository;
import ro.mta.sdk.util.ConstraintUtil;

public class ToggleSystemClient {
    private final ToggleSystemConfig toggleSystemConfig;
    private final ToggleSystemContextProvider toggleSystemContextProvider;
    private final ToggleRepository toggleRepository;
    private final MetricService metricService;

    private static ToggleRepository defaultToggleRepository(ToggleSystemConfig systemConfig) {
        return new FeatureToggleRepository(systemConfig);
    }

    public ToggleSystemClient(ToggleSystemConfig systemConfig){
        this(systemConfig,
                defaultToggleRepository(systemConfig));
    }

    public ToggleSystemClient(ToggleSystemConfig systemConfig, ToggleRepository toggleRepository){
        this(systemConfig,
                systemConfig.getToggleSystemContextProvider(),
                toggleRepository,
                new MetricServiceImpl(systemConfig));
    }

    public ToggleSystemClient(ToggleSystemConfig toggleSystemConfig,
                              ToggleSystemContextProvider toggleSystemContextProvider,
                              ToggleRepository toggleRepository,
                              MetricService metricService) {
        this.toggleSystemConfig = toggleSystemConfig;
        this.toggleSystemContextProvider = toggleSystemContextProvider;
        this.toggleRepository = toggleRepository;
        this.metricService = metricService;
        metricService.register();
    }

    public boolean isEnabled(String toggleName){
        return isEnabled(toggleName, false);
    }

    public boolean isEnabled(String toggleName, boolean defaultSetting){
        return isEnabled(toggleName, toggleSystemContextProvider.getContext(), defaultSetting);
    }

    public boolean isEnabled(String toggleName,ToggleSystemContext context , boolean defaultSetting){
        return checkEnabled(toggleName, context, defaultSetting);
    }

    private boolean checkEnabled(String toggleName, ToggleSystemContext context, boolean defaultSetting){
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
}
