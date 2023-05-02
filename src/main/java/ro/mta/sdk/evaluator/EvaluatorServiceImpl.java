package ro.mta.sdk.evaluator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.ToggleSystemContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EvaluatorServiceImpl implements EvaluatorService {
    private final ToggleSystemConfig toggleSystemConfig;
    private final Cache<String, Boolean> featureFlagCache;
    private final EvaluatorSender evaluatorSender;

    public EvaluatorServiceImpl(ToggleSystemConfig toggleSystemConfig) {
        this(toggleSystemConfig, new HttpEvaluatorSender(toggleSystemConfig));
    }

    public EvaluatorServiceImpl(ToggleSystemConfig toggleSystemConfig,
                                EvaluatorSender evaluatorSender) {
        this.toggleSystemConfig = toggleSystemConfig;
        this.evaluatorSender = evaluatorSender;
        this.featureFlagCache = Caffeine.newBuilder()
                .expireAfterWrite(toggleSystemConfig.getCacheTimeout(), TimeUnit.SECONDS)
                .maximumSize(1000)
                .build();

    }

    @Override
    public boolean remoteEvalution(String toggleName, ToggleSystemContext systemContext, boolean defaultSetting) {

        String cacheKey = toggleName + "_" + systemContext.getAllContextFieldsToString();
        Boolean cachedValue = featureFlagCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        FeatureEvaluationRequest featureEvaluationRequest = new FeatureEvaluationRequest(toggleName,
                systemContext.getAllContextFields());
        FeatureEvaluationResponse featureEvaluationResponse = evaluatorSender.evaluateToggle(featureEvaluationRequest);
        if(featureEvaluationResponse.getStatus().equals(FeatureEvaluationResponse.Status.SUCCESS)){
            featureFlagCache.put(cacheKey, featureEvaluationResponse.getEnabled());
            return featureEvaluationResponse.getEnabled();
        } else {
            return defaultSetting;
        }
    }
}
