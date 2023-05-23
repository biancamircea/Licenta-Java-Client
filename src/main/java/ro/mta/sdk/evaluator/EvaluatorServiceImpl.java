package ro.mta.sdk.evaluator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.ToggleSystemContext;

import java.util.concurrent.TimeUnit;

public class EvaluatorServiceImpl implements EvaluatorService {
    private final ToggleSystemConfig toggleSystemConfig;
    private final Cache<String, Boolean> featureFlagCache;
    private final Cache<String, String> payloadCache;
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
        this.payloadCache = Caffeine.newBuilder()
                .expireAfterWrite(toggleSystemConfig.getCacheTimeout(), TimeUnit.SECONDS)
                .maximumSize(1000)
                .build();
    }

    private void cacheResponse(String toggleName, ToggleSystemContext systemContext, FeatureEvaluationResponse featureEvaluationResponse){
        String featureFlagCacheKey = toggleName + "_" + systemContext.getAllContextFieldsToString();
        String payloadCacheKey = toggleName + "_" + featureEvaluationResponse.getEnabled().toString();
        featureFlagCache.put(featureFlagCacheKey, featureEvaluationResponse.getEnabled());
        if(featureEvaluationResponse.getPayload() != null){
            payloadCache.put(payloadCacheKey, featureEvaluationResponse.getPayload());
        } else {
            payloadCache.put(payloadCacheKey, "NO_PAYLOAD_DEFINED");
        }
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
            cacheResponse(toggleName, systemContext, featureEvaluationResponse);
            return featureEvaluationResponse.getEnabled();
        } else {
            return defaultSetting;
        }
    }

    @Override
    public String remotePayload(String toggleName, Boolean enabled, ToggleSystemContext systemContext, String defaultPayload) {
        String cacheKey = toggleName + "_" + enabled.toString();
        String cachedValue = payloadCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }
        FeatureEvaluationRequest featureEvaluationRequest = new FeatureEvaluationRequest(toggleName,
                systemContext.getAllContextFields());
        FeatureEvaluationResponse featureEvaluationResponse = evaluatorSender.evaluateToggle(featureEvaluationRequest);
        if(featureEvaluationResponse.getStatus().equals(FeatureEvaluationResponse.Status.SUCCESS)){
            cacheResponse(toggleName, systemContext, featureEvaluationResponse);
            if(featureEvaluationResponse.getPayload() == null){
                return defaultPayload;
            } else {
                return featureEvaluationResponse.getPayload();
            }
        } else {
            return defaultPayload;
        }
    }
}
